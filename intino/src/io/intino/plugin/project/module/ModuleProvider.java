package io.intino.plugin.project.module;

import com.intellij.openapi.module.ModuleUtil;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;

import static com.intellij.openapi.module.ModuleUtilCore.findModuleForFile;

public class ModuleProvider {

	private ModuleProvider() {
	}

	public static com.intellij.openapi.module.Module moduleOf(PsiElement element) {
		if (element == null || element.getProject().isDisposed() || (!(element instanceof PsiDirectory) && element.getContainingFile().getVirtualFile() == null && element.getContainingFile().getOriginalFile().getVirtualFile() == null))
			return null;
		return ModuleUtil.findModuleForPsiElement(element);
	}

	public static com.intellij.openapi.module.Module moduleOf(PsiFile element) {
		return findModuleForFile(element.getOriginalFile().getVirtualFile(), element.getProject());
	}
}
