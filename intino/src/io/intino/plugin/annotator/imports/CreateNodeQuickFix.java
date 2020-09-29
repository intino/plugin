package io.intino.plugin.annotator.imports;

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
import io.intino.plugin.annotator.fix.WithLiveTemplateFix;
import io.intino.plugin.codeinsight.livetemplates.TaraTemplateContext;
import io.intino.plugin.lang.psi.TaraElementFactory;
import io.intino.plugin.lang.psi.TaraModel;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

public class CreateNodeQuickFix extends WithLiveTemplateFix implements IntentionAction {
	private final String name;
	private final String type = "Element";

	public CreateNodeQuickFix(String name, TaraModel file) {
		this.name = name;
	}

	@Nls
	@NotNull
	@Override
	public String getText() {
		return "Create " + type + " " + name;
	}

	@NotNull
	@Override
	public String getFamilyName() {
		return "Create " + name;
	}

	@Override
	public boolean isAvailable(@NotNull Project project, Editor editor, PsiFile file) {
		return file.isValid();
	}

	@Override
	public void invoke(@NotNull Project project, Editor editor, PsiFile file) throws IncorrectOperationException {
		createLiveTemplateFor(file, editor);
	}

	@Override
	public boolean startInWriteAction() {
		return true;
	}

	private void createLiveTemplateFor(PsiFile file, Editor editor) {
		if (!FileModificationService.getInstance().prepareFileForWrite(file)) return;
		IdeDocumentHistory.getInstance(file.getProject()).includeCurrentPlaceAsChangePlace();
		PsiDocumentManager.getInstance(file.getProject()).doPostponedOperationsAndUnblockDocument(editor.getDocument());
		int line = editor.getDocument().getLineCount() - 1;
		if (editor.getDocument().getLineEndOffset(line) != editor.getDocument().getLineStartOffset(line)) {
			addNewLine(editor, file);
			line++;
		}
		final Editor componentEditor = positionCursorAtBegining(file.getProject(), file, line);
		if (componentEditor == null) return;
		TemplateManager.getInstance(file.getProject()).startTemplate(componentEditor, createTemplate(file));
		PsiDocumentManager.getInstance(file.getProject()).doPostponedOperationsAndUnblockDocument(editor.getDocument());
	}

	private void addNewLine(Editor editor, PsiFile file) {
		final TaraElementFactory factory = TaraElementFactory.getInstance(file.getProject());
		final PsiElement newLine = factory.createNewLine();
		file.add(newLine.copy());
		file.add(newLine.copy());
		PsiDocumentManager.getInstance(file.getProject()).doPostponedOperationsAndUnblockDocument(editor.getDocument());
	}

	public Template createTemplate(PsiFile file) {
		final Template template = TemplateManager.getInstance(file.getProject()).createTemplate("var", "Tara", createTemplateText());
		template.addVariable("VALUE", "", "", true);
		((TemplateImpl) template).getTemplateContext().setEnabled(contextType(TaraTemplateContext.class), true);
		return template;
	}

	public String createTemplateText() {
		return "$VALUE$" + name;
	}

}
