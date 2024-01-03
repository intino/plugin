package io.intino.plugin.lang.psi.impl;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import io.intino.plugin.lang.psi.TaraFacetApply;
import io.intino.plugin.lang.psi.TaraParameter;
import io.intino.tara.language.model.Mogram;
import io.intino.tara.language.model.Parameter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class ParametersMixin extends ASTWrapperPsiElement {
	public ParametersMixin(@NotNull ASTNode node) {
		super(node);
	}

	@NotNull
	public List<Parameter> getParameters() {
		List<Parameter> parameters = new ArrayList<>();
		final Parameter[] params = PsiTreeUtil.getChildrenOfType(this, TaraParameter.class);
		if (params != null) Collections.addAll(parameters, params);
		return parameters;
	}

	public boolean areExplicit() {
		Collection<Parameter> parameters = getParameters();
		return !parameters.isEmpty() && ((TaraParameter) parameters.iterator().next()).getIdentifier() != null;
	}

	public TaraFacetApply isInFacet() {
		PsiElement aElement = this;
		while (!(aElement.getParent() instanceof Mogram) && !(aElement.getParent() instanceof TaraFacetApply))
			aElement = aElement.getParent();
		return (aElement.getParent() instanceof TaraFacetApply) ? (TaraFacetApply) aElement.getParent() : null;
	}
}
