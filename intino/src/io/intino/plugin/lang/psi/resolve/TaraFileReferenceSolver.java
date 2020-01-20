package io.intino.plugin.lang.psi.resolve;

import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import io.intino.plugin.IntinoIcons;
import io.intino.plugin.lang.psi.HeaderReference;
import io.intino.plugin.lang.psi.Identifier;
import io.intino.plugin.lang.psi.TaraModel;
import io.intino.plugin.lang.psi.impl.FileVariantsManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class TaraFileReferenceSolver extends TaraReferenceSolver {
	public TaraFileReferenceSolver(HeaderReference element, TextRange range) {
		super(element, range);
	}

	@Override
	protected List<PsiElement> doMultiResolve() {
		return ReferenceManager.resolve((Identifier) myElement.getLastChild());
	}

	@Nullable
	@Override
	public PsiElement resolve() {
		final List<PsiElement> results = ReferenceManager.resolve((Identifier) myElement.getLastChild());
		return results.isEmpty() ? null : results.get(0);
	}

	@NotNull
	@Override
	public Object[] getVariants() {
		final Set<TaraModel> variants = new LinkedHashSet();
		new FileVariantsManager(variants, myElement).resolveVariants();
		return fillVariants(variants);
	}

	private Object[] fillVariants(Collection<TaraModel> variants) {
		List<LookupElement> lookupElements = new ArrayList<>();
		for (final TaraModel model : variants) {
			if (model == null || model.getName().length() == 0) continue;
			lookupElements.add(LookupElementBuilder.create(model.getPresentableName()).withIcon(IntinoIcons.MODEL_16));
		}
		return lookupElements.toArray();
	}
}
