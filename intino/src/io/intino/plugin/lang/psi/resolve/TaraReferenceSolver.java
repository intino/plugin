package io.intino.plugin.lang.psi.resolve;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public abstract class TaraReferenceSolver extends PsiReferenceBase<PsiElement> implements PsiPolyVariantReference {

	public TaraReferenceSolver(@NotNull PsiElement element, TextRange textRange) {
		super(element, textRange);
	}

	@NotNull
	@Override
	public ResolveResult[] multiResolve(boolean incompleteCode) {
		List<PsiElement> resolve = doMultiResolve();
		if (resolve.isEmpty() || resolve.get(0) == null) return ResolveResult.EMPTY_ARRAY;
		return resolve.stream().map(PsiElementResolveResult::new).toArray(ResolveResult[]::new);
	}

	protected abstract List<PsiElement> doMultiResolve();

	@Override
	public PsiElement bindToElement(@NotNull PsiElement element) throws IncorrectOperationException {
		return element;
	}
}