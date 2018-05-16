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
import com.intellij.util.IncorrectOperationException;
import io.intino.tara.lang.model.Node;
import io.intino.tara.plugin.annotator.fix.WithLiveTemplateFix;
import io.intino.tara.plugin.codeinsight.livetemplates.TaraTemplateContext;
import io.intino.tara.plugin.lang.psi.TaraNode;
import io.intino.tara.plugin.lang.psi.impl.TaraPsiImplUtil;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static io.intino.plugin.codeinsight.annotators.fix.FixUtils.addNewLine;

public class AddArgumentFix extends WithLiveTemplateFix implements IntentionAction {

	private final Node node;
	private final List<String> requiredParameters;

	public AddArgumentFix(PsiElement element, List<String> requiredParameters) {
		this.node = element instanceof Node ? (Node) element : (Node) TaraPsiImplUtil.getContainerOf(element);
		this.requiredParameters = requiredParameters;
	}

	@Nls
	@NotNull
	@Override
	public String getText() {
		return "Add required arguments";
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

	@SuppressWarnings("Duplicates")
	private void createLiveTemplateFor(PsiFile file, Editor editor) {
		if (!FileModificationService.getInstance().prepareFileForWrite(file)) return;
		IdeDocumentHistory.getInstance(file.getProject()).includeCurrentPlaceAsChangePlace();
		commit(file, editor);
		final Editor componentEditor = positionCursorAtBegining(file.getProject(), file, editor.getDocument().getLineNumber(((TaraNode) node).getLastChild().getTextRange().getEndOffset()) + 1);
		TemplateManager.getInstance(file.getProject()).startTemplate(componentEditor, createTemplate(file));
		commit(file, componentEditor);
		commit(file, editor);
		addNewLine((TaraNode) node);
		commit(file, editor);
	}

	private void commit(PsiFile file, Editor editor) {
		PsiDocumentManager.getInstance(file.getProject()).doPostponedOperationsAndUnblockDocument(editor.getDocument());
	}

	private Template createTemplate(PsiFile file) {
		final Template template = TemplateManager.getInstance(file.getProject()).createTemplate("var", "Tara", createTemplateText(TaraPsiImplUtil.getIndentation((PsiElement) node) + 1));
		addComponents(template);
		((TemplateImpl) template).getTemplateContext().setEnabled(contextType(TaraTemplateContext.class), true);
		return template;
	}

	private void addComponents(Template template) {
		int i = 0;
		for (String parameter : requiredParameters)
			template.addVariable("VALUE" + i++, "", "", true);
	}

	private String createTemplateText(int indents) {
		StringBuilder text = new StringBuilder(buildIndentation(indents));
		int i = 0;
		for (String parameter : requiredParameters)
			text.append("Argument(name = \"").append(parameter).append("\", value = \"$VALUE").append(i++).append("$\")\n");
		return text.toString().substring(0, text.length() - 1);
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
