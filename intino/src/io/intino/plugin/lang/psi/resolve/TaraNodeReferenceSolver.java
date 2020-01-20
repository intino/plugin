package io.intino.plugin.lang.psi.resolve;

import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.ResolveResult;
import io.intino.plugin.IntinoIcons;
import io.intino.plugin.lang.psi.*;
import io.intino.tara.lang.model.Node;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class TaraNodeReferenceSolver extends TaraReferenceSolver {

	public TaraNodeReferenceSolver(@NotNull PsiElement element, TextRange range) {
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
		final Set<Node> variants = new LinkedHashSet();
		if (isNodeReference()) variants.addAll(new VariantsManager((Identifier) myElement).resolveVariants());
		return fillVariants(variants);
	}

	private Object[] fillVariants(Collection<Node> variants) {
		List<LookupElement> lookupElements = new ArrayList<>();
		for (final Node node : variants) {
			if (node == null || node.name() == null || node.name().length() == 0) continue;
			final TaraSignature signature = ((TaraNode) node).getSignature();
			if (signature.getIdentifier() != null)
				lookupElements.add(LookupElementBuilder.create(signature.getIdentifier()).
						withIcon(IntinoIcons.NODE).withTypeText(node.type()));
		}
		return lookupElements.toArray();
	}

	private boolean isNodeReference() {
		return myElement.getParent() instanceof IdentifierReference || myElement.getParent() instanceof HeaderReference;
	}
}