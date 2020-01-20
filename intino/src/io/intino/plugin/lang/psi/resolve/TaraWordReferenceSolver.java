package io.intino.plugin.lang.psi.resolve;

import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import io.intino.plugin.IntinoIcons;
import io.intino.tara.lang.model.rules.variable.WordRule;
import io.intino.tara.lang.semantics.Constraint;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class TaraWordReferenceSolver extends TaraReferenceSolver {

	private final Constraint.Parameter parameterAllow;

	public TaraWordReferenceSolver(PsiElement element, TextRange range, Constraint.Parameter parameterAllow) {
		super(element, range);
		this.parameterAllow = parameterAllow;
	}

	@Nullable
	@Override
	public PsiElement resolve() {
		return myElement;
	}

	@Override
	protected List<PsiElement> doMultiResolve() {
		return Collections.singletonList(myElement);
	}

	@NotNull
	@Override
	public Object[] getVariants() {
		return ((WordRule) parameterAllow.rule()).words().stream().
				map(node -> LookupElementBuilder.create(node).withIcon(IntinoIcons.MODEL_16).withTypeText("Word")).
				collect(Collectors.toList()).
				toArray();
	}


}
