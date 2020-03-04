package io.intino.plugin.lang.psi.impl;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import io.intino.plugin.lang.psi.TaraModel;
import io.intino.plugin.project.module.ModuleProvider;

import java.util.Set;
import java.util.stream.Collectors;

public class FileVariantsManager {

	private final Set<TaraModel> variants;
	private final PsiElement myElement;

	public FileVariantsManager(Set<TaraModel> variants, PsiElement myElement) {
		this.variants = variants;
		this.myElement = myElement;
	}

	public void resolveVariants() {
		final PsiFile file = myElement.getContainingFile().getOriginalFile();
		variants.addAll(IntinoUtil.getFilesOfModuleByFileType(ModuleProvider.moduleOf(file), file.getFileType()).stream().collect(Collectors.toList()));
	}
}
