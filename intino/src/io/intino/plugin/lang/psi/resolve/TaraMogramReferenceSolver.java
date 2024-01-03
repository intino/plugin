package io.intino.plugin.lang.psi.resolve;

import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.ResolveResult;
import io.intino.plugin.IntinoIcons;
import io.intino.plugin.lang.psi.*;
import io.intino.tara.language.model.Mogram;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class TaraMogramReferenceSolver extends TaraReferenceSolver {

	public TaraMogramReferenceSolver(@NotNull PsiElement element, TextRange range) {
		super(element, range);
	}

	@Nullable
	@Override
	public PsiElement resolve() {
		ResolveResult[] resolveResults = multiResolve(false);
		return resolveResults.length == 1 ? resolveResults[0].getElement() : null;
	}

	@Override
	protected List<PsiElement> doMultiResolve() {
		return ReferenceManager.resolve((Identifier) myElement);
	}

	@NotNull
	@Override
	public Object[] getVariants() {
		final Set<Mogram> variants = new LinkedHashSet<>();
		if (isNodeReference()) variants.addAll(new VariantsManager((Identifier) myElement).resolveVariants());
		return fillVariants(variants);
	}

	private Object[] fillVariants(Collection<Mogram> variants) {
		List<LookupElement> lookupElements = new ArrayList<>();
		variants.stream()
				.filter(mogram -> mogram != null && mogram.name() != null && !mogram.name().isEmpty())
				.forEach(mogram -> {
					final TaraSignature signature = ((TaraMogram) mogram).getSignature();
					if (signature.getIdentifier() != null)
						lookupElements.add(LookupElementBuilder.create(signature.getIdentifier()).
								withIcon(IntinoIcons.MOGRAM).withTypeText(mogram.type()));
				});
		return lookupElements.toArray();
	}

	private boolean isNodeReference() {
		return myElement.getParent() instanceof IdentifierReference || myElement.getParent() instanceof HeaderReference;
	}
}