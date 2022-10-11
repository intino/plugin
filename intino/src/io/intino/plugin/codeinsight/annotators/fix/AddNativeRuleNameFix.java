package io.intino.plugin.codeinsight.annotators.fix;

import com.intellij.codeInsight.FileModificationService;
import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.codeInsight.template.Template;
import com.intellij.codeInsight.template.TemplateManager;
import com.intellij.codeInsight.template.impl.TemplateImpl;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.ex.IdeDocumentHistory;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.util.IncorrectOperationException;
import io.intino.plugin.codeinsight.livetemplates.TaraTemplateContext;
import io.intino.plugin.lang.psi.TaraVariable;
import io.intino.plugin.lang.psi.impl.TaraPsiUtil;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

public class AddNativeRuleNameFix extends WithLiveTemplateFix implements IntentionAction {


	private final TaraVariable variable;

	public AddNativeRuleNameFix(PsiElement element) {
		this.variable = TaraPsiUtil.getContainerByType(element, TaraVariable.class);
	}

	@Nls
	@NotNull
	@Override
	public String getText() {
		return "Set native interface name";
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
		if (variable == null || variable.getVariableType() == null) return;
		createLiveTemplateFor(file, editor);
		PsiDocumentManager.getInstance(file.getProject()).doPostponedOperationsAndUnblockDocument(editor.getDocument());
	}

	@Override
	public boolean startInWriteAction() {
		return true;
	}

	private void createLiveTemplateFor(PsiFile file, Editor editor) {
		if (!FileModificationService.getInstance().prepareFileForWrite(file)) return;
		IdeDocumentHistory.getInstance(file.getProject()).includeCurrentPlaceAsChangePlace();
		ApplicationManager.getApplication().runWriteAction(() -> {
			PsiDocumentManager.getInstance(file.getProject()).doPostponedOperationsAndUnblockDocument(editor.getDocument());
			final Editor ruleEditor = positionCursor(file.getProject(), file, variable.getVariableType());
			TemplateManager.getInstance(file.getProject()).startTemplate(ruleEditor, createTemplate(file));
			PsiDocumentManager.getInstance(file.getProject()).doPostponedOperationsAndUnblockDocument(ruleEditor.getDocument());
		});
	}

	private Template createTemplate(PsiFile file) {
		final Template template = TemplateManager.getInstance(file.getProject()).createTemplate("var", "Tara", ":$VALUE$");
		((TemplateImpl) template).getTemplateContext().setEnabled(contextType(TaraTemplateContext.class), true);
		return template;
	}


}
