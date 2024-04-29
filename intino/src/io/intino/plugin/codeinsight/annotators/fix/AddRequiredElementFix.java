package io.intino.plugin.codeinsight.annotators.fix;

import com.intellij.codeInsight.FileModificationService;
import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.codeInsight.template.Template;
import com.intellij.codeInsight.template.TemplateManager;
import com.intellij.codeInsight.template.impl.TemplateImpl;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.ex.IdeDocumentHistory;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.impl.CheckUtil;
import com.intellij.util.IncorrectOperationException;
import io.intino.plugin.codeinsight.livetemplates.TaraTemplateContext;
import io.intino.plugin.lang.psi.TaraElementFactory;
import io.intino.plugin.lang.psi.TaraMogram;
import io.intino.plugin.lang.psi.impl.IntinoUtil;
import io.intino.plugin.lang.psi.impl.TaraPsiUtil;
import io.intino.tara.language.model.Facet;
import io.intino.tara.language.model.Mogram;
import io.intino.tara.language.model.Rule;
import io.intino.tara.language.model.rules.Size;
import io.intino.tara.language.semantics.Constraint;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AddRequiredElementFix extends WithLiveTemplateFix implements IntentionAction {
	@SafeFieldForPreview
	private Mogram mogram;

	public AddRequiredElementFix(PsiElement element) {
		try {
			this.mogram = element instanceof Mogram ? (Mogram) element : TaraPsiUtil.getContainerNodeOf(element);
		} catch (Throwable e) {
			this.mogram = null;
		}
	}

	@Nls
	@NotNull
	@Override
	public String getText() {
		return "Add required element";
	}

	@Nls
	@NotNull
	@Override
	public String getFamilyName() {
		return getText();
	}

	@Override
	public boolean isAvailable(@NotNull Project project, Editor editor, PsiFile file) {
		boolean writable = false;
		try {
			CheckUtil.checkWritable(file);
			writable = true;
		} catch (IncorrectOperationException ignored) {
		}
		return file.isValid() && mogram != null && writable;
	}

	@Override
	public void invoke(@NotNull Project project, Editor editor, PsiFile file) throws IncorrectOperationException {
		List<Constraint.Component> requires = findConstraints().stream().
				filter(c -> c instanceof Constraint.Component && isRequired((Constraint.Component) c)).
				map(c -> (Constraint.Component) c).toList();
		filterPresentElements(requires);
		createLiveTemplateFor(requires, file, editor);
		PsiDocumentManager.getInstance(file.getProject()).doPostponedOperationsAndUnblockDocument(editor.getDocument());
	}

	private boolean isRequired(Constraint.Component component) {
		List<Rule> sizes = component.rules().stream().filter(r -> r instanceof Size).toList();
		return !sizes.isEmpty() && sizes.stream().allMatch(r -> ((Size) r).isRequired());
	}

	private List<Constraint> findConstraints() {
		List<Constraint> constraints = IntinoUtil.constraintsOf(mogram);
		if (constraints == null) return Collections.emptyList();
		constraints = new ArrayList<>(constraints);
		for (Facet aspect : mogram.appliedFacets()) {
			List<Constraint> collection = IntinoUtil.constraintsOf(aspect);
			if (collection != null) constraints.addAll(collection);
		}
		return constraints;
	}

	private void filterPresentElements(List<Constraint.Component> requires) {
		for (Mogram mogram : this.mogram.components()) {
			Constraint.Component require = findInConstraints(requires, mogram.type());
			if (require != null) requires.remove(require);
		}
	}

	private void createLiveTemplateFor(List<Constraint.Component> requires, PsiFile file, Editor editor) {
		if (!FileModificationService.getInstance().prepareFileForWrite(file)) return;
		IdeDocumentHistory.getInstance(file.getProject()).includeCurrentPlaceAsChangePlace();
		PsiDocumentManager.getInstance(file.getProject()).doPostponedOperationsAndUnblockDocument(editor.getDocument());
		addNewLine(editor, (TaraMogram) mogram);
		final Editor componentEditor = positionCursorAtBegining(file.getProject(), file, editor.getDocument().getLineNumber(((TaraMogram) mogram).getTextOffset()) + 1);
		TemplateManager.getInstance(file.getProject()).startTemplate(componentEditor, createTemplate(requires, file));
		addNewLine(editor, (TaraMogram) mogram);
		PsiDocumentManager.getInstance(file.getProject()).doPostponedOperationsAndUnblockDocument(editor.getDocument());
	}

	private void addNewLine(Editor editor, TaraMogram node) {
		final TaraElementFactory factory = TaraElementFactory.getInstance(node.getProject());
		final PsiElement newLine = factory.createNewLine();
		node.add(newLine.copy());
		node.add(newLine.copy());
		PsiDocumentManager.getInstance(node.getProject()).doPostponedOperationsAndUnblockDocument(editor.getDocument());
	}

	private Template createTemplate(List<Constraint.Component> requires, PsiFile file) {
		final Template template = TemplateManager.getInstance(file.getProject()).createTemplate("var", "Tara", createTemplateText(requires, TaraPsiUtil.getIndentation((PsiElement) mogram) + 1));
		addComponents(template, requires);
		((TemplateImpl) template).getTemplateContext().setEnabled(contextType(TaraTemplateContext.class), true);
		return template;
	}

	private void addComponents(Template template, List<Constraint.Component> requires) {
		for (int i = 0; i < requires.size(); i++) template.addVariable("VALUE" + i, "", "", true);
	}

	private String createTemplateText(List<Constraint.Component> requires, int indents) {
		String text = buildIndentation(indents);
		for (int i = 0; i < requires.size(); i++) text += shortType(requires, i) + " $VALUE" + i + "$\n";
		return text;
	}

	private String buildIndentation(int indents) {
		String indentation = "";
		for (int i = 0; i < indents; i++) indentation += "\t";
		return indentation;
	}

	private String shortType(List<Constraint.Component> requires, int i) {
		final String[] type = requires.get(i).type().split("\\.");
		return type[type.length - 1];
	}

	@Nullable
	private Constraint.Component findInConstraints(List<Constraint.Component> constraints, String type) {
		for (Constraint.Component require : constraints) if (require.type().equals(type)) return require;
		return null;
	}

	@Override
	public boolean startInWriteAction() {
		return true;
	}
}