package io.intino.plugin.refactoring.rename;

import com.intellij.openapi.module.Module;
import com.intellij.psi.PsiElement;
import com.intellij.refactoring.listeners.RefactoringElementListener;
import com.intellij.refactoring.rename.RenamePsiElementProcessor;
import io.intino.plugin.codeinsight.languageinjection.imports.Imports;
import io.intino.plugin.lang.psi.Identifier;
import io.intino.plugin.lang.psi.Signature;
import io.intino.plugin.lang.psi.TaraIdentifier;
import io.intino.plugin.lang.psi.TaraModel;
import io.intino.plugin.project.module.ModuleProvider;
import io.intino.tara.language.model.Mogram;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TaraRenamePsiElementProcessor extends RenamePsiElementProcessor {

	@Override
	public boolean canProcessElement(@NotNull PsiElement element) {
		return element instanceof TaraModel || element instanceof Identifier || element instanceof Module;
	}


	@Nullable
	@Override
	public Runnable getPostRenameCallback(PsiElement element, String newName, RefactoringElementListener elementListener) {
		return updateImports(element, newName);
	}

	private Runnable updateImports(PsiElement element, String newName) {
		if (!(element instanceof TaraIdentifier) || element.getParent() == null || !(element.getParent() instanceof Signature))
			return null;
		final String old = oldQn(element);
		final String module = ModuleProvider.moduleOf(element.getContainingFile()).getName();
		return () -> {
			Imports imports = new Imports(element.getProject());
			imports.refactor(module, old, newQn(old, newName));
		};
	}

	private String newQn(String oldQn, String newName) {
		final String[] split = oldQn.split("\\.");
		final String[] ts = Arrays.copyOf(split, split.length - 1);
		final List<String> elements = new ArrayList<>(Arrays.asList(ts));
		elements.add(newName);
		return String.join(".", elements);
	}

	private String oldQn(PsiElement element) {
		final Mogram mogram = getContainerByType(element.getOriginalElement(), Mogram.class);
		return mogram == null ? "" : mogram.qualifiedName();
	}

	public static <T> T getContainerByType(PsiElement element, Class<T> tClass) {
		PsiElement parent = element;
		while (parent != null)
			if (tClass.isInstance(parent)) return (T) parent;
			else parent = parent.getContext();
		return null;
	}
}
