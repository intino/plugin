package io.intino.plugin.codeinsight.annotators.legio.fix;


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
import com.intellij.util.IncorrectOperationException;
import io.intino.plugin.codeinsight.annotators.fix.WithLiveTemplateFix;
import io.intino.plugin.codeinsight.livetemplates.TaraTemplateContext;
import io.intino.plugin.lang.psi.TaraMogram;
import io.intino.plugin.lang.psi.impl.TaraPsiUtil;
import io.intino.tara.language.model.Mogram;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

import static io.intino.plugin.codeinsight.annotators.legio.fix.FixUtils.addNewLine;

public class AddParameterFix extends WithLiveTemplateFix implements IntentionAction {

	private final Mogram node;
	private final Map<String, String> requiredParameters;

	public AddParameterFix(PsiElement element, Map<String, String> requiredParameters) {
		this.node = element instanceof Mogram ? (Mogram) element : (Mogram) TaraPsiUtil.getContainerOf(element);
		this.requiredParameters = requiredParameters;
	}

	@Nls
	@NotNull
	@Override
	public String getText() {
		return "Add required parameters";
	}

	@Nls
	@NotNull
	@Override
	public String getFamilyName() {
		return getText();
	}

	@Override
	public boolean isAvailable(@NotNull Project project, Editor editor, PsiFile file) {
		return file.isValid();
	}

	@Override
	public void invoke(@NotNull Project project, Editor editor, PsiFile file) throws IncorrectOperationException {
		createLiveTemplateFor(file, editor);
		commit(file, editor);
	}

	private void createLiveTemplateFor(PsiFile file, Editor editor) {
		if (!FileModificationService.getInstance().prepareFileForWrite(file)) return;
		IdeDocumentHistory.getInstance(file.getProject()).includeCurrentPlaceAsChangePlace();
		commit(file, editor);
		addNewLine((TaraMogram) node);
		commit(file, editor);
		final Editor componentEditor = positionCursorAtBegining(file.getProject(), file, editor.getDocument().getLineNumber(((TaraMogram) node).getTextOffset()) + 1);
		TemplateManager.getInstance(file.getProject()).startTemplate(componentEditor, createTemplate(file));
		commit(file, componentEditor);
		commit(file, editor);
		addNewLine((TaraMogram) node);
		commit(file, editor);
	}

	private void commit(PsiFile file, Editor editor) {
		PsiDocumentManager.getInstance(file.getProject()).doPostponedOperationsAndUnblockDocument(editor.getDocument());
	}

	private Template createTemplate(PsiFile file) {
		final Template template = TemplateManager.getInstance(file.getProject()).createTemplate("var", "Tara", createTemplateText(TaraPsiUtil.getIndentation((PsiElement) node) + 1));
		addComponents(template);
		((TemplateImpl) template).getTemplateContext().setEnabled(contextType(TaraTemplateContext.class), true);
		return template;
	}

	private void addComponents(Template template) {
		int i = 0;
		for (String parameter : requiredParameters.keySet())
			template.addVariable("VALUE" + i++, requiredParameters.get(parameter), requiredParameters.get(parameter), true);
	}

	private String createTemplateText(int indents) {
		StringBuilder text = new StringBuilder(buildIndentation(indents));
		int i = 0;
		for (String parameter : requiredParameters.keySet())
			text.append("Parameter(name = \"").append(parameter).append("\", defaultValue = \"$VALUE").append(i++).append("$\")\n\t");
		return text.toString();
	}

	private String buildIndentation(int indents) {
		String indentation = "";
		for (int i = 0; i < indents; i++) indentation += "\t";
		return indentation;
	}

	@Override
	public boolean startInWriteAction() {
		return true;
	}
}
