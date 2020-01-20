package io.intino.plugin.annotator.imports;

import com.intellij.codeInsight.FileModificationService;
import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.util.IncorrectOperationException;
import io.intino.plugin.lang.psi.Import;
import io.intino.plugin.lang.psi.TaraPsiElement;
import io.intino.plugin.messages.MessageProvider;
import org.jetbrains.annotations.NotNull;

public class RemoveImportFix implements IntentionAction {
	private final TaraPsiElement identifier;

	public RemoveImportFix(TaraPsiElement identifier) {
		this.identifier = identifier;
	}

	@NotNull
	public String getText() {
		return MessageProvider.message("remove.import.concept.intention.text");
	}

	@NotNull
	public String getFamilyName() {
		return getText();
	}

	public boolean isAvailable(@NotNull Project project, Editor editor, PsiFile file) {
		return file.isValid() && identifier.isValid() && identifier.getManager().isInProject(identifier);
	}

	public void invoke(@NotNull Project project, Editor editor, PsiFile file) throws IncorrectOperationException {
		if (!FileModificationService.getInstance().prepareFileForWrite(file)) return;
		findImport().delete();
	}

	private PsiElement findImport() {
		PsiElement imp = identifier;
		while (!(imp instanceof Import))
			imp = imp.getParent();
		return imp;
	}

	public boolean startInWriteAction() {
		return true;
	}
}
