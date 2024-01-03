package io.intino.plugin.refactoring;

import com.intellij.lang.refactoring.RefactoringSupportProvider;
import com.intellij.psi.PsiElement;
import io.intino.plugin.lang.psi.TaraIdentifier;
import io.intino.tara.language.model.Mogram;
import org.jetbrains.annotations.NotNull;

public class TaraRefactoringSupportProvider extends RefactoringSupportProvider {
	@Override
	public boolean isMemberInplaceRenameAvailable(@NotNull PsiElement element, PsiElement context) {
		return element instanceof TaraIdentifier;
	}

	@Override
	public boolean isSafeDeleteAvailable(@NotNull PsiElement element) {
		return element instanceof Mogram;
	}
}