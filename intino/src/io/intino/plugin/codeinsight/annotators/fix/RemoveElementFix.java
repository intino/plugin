package io.intino.plugin.codeinsight.annotators.fix;

import com.intellij.codeInsight.FileModificationService;
import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.util.IncorrectOperationException;
import io.intino.plugin.messages.MessageProvider;
import org.jetbrains.annotations.NotNull;

public class RemoveElementFix implements IntentionAction {
	@SafeFieldForPreview
	private final PsiElement element;

	public RemoveElementFix(@NotNull final PsiElement origNode) {
		element = origNode;
	}

	@NotNull
	public String getText() {
		return MessageProvider.message("remove.element.intention");
	}

	@NotNull
	public String getFamilyName() {
		return getText();
	}

	public boolean isAvailable(@NotNull Project project, Editor editor, PsiFile file) {
		return file.isValid() && element.isValid() && element.getManager().isInProject(element);
	}

	public void invoke(@NotNull Project project, Editor editor, PsiFile file) throws IncorrectOperationException {
		if (!FileModificationService.getInstance().prepareFileForWrite(file)) return;
		element.delete();
	}

	public boolean startInWriteAction() {
		return true;
	}
}