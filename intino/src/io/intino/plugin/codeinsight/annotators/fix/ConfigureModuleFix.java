package io.intino.plugin.codeinsight.annotators.fix;

import com.intellij.codeInsight.FileModificationService;
import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.util.IncorrectOperationException;
import io.intino.plugin.messages.MessageProvider;
import io.intino.plugin.project.module.IntinoModuleType;
import io.intino.plugin.project.module.ModuleProvider;
import org.jetbrains.annotations.NotNull;

class ConfigureModuleFix implements IntentionAction {

	public ConfigureModuleFix(PsiElement element) {
	}

	@NotNull
	public String getText() {
		return MessageProvider.message("configure.module");
	}

	@NotNull
	public String getFamilyName() {
		return getText();
	}

	public boolean isAvailable(@NotNull Project project, Editor editor, PsiFile file) {
		return file.isValid();
	}

	public void invoke(@NotNull Project project, Editor editor, PsiFile file) throws IncorrectOperationException {
		if (!FileModificationService.getInstance().prepareFileForWrite(file)) return;
		if (!IntinoModuleType.isIntino(ModuleProvider.moduleOf(file))) return;
		//TODO
	}

	public boolean startInWriteAction() {
		return true;
	}
}