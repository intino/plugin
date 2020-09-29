package io.intino.plugin.lang.psi.impl;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import io.intino.magritte.lang.model.Node;
import io.intino.magritte.lang.model.Parameter;
import io.intino.plugin.lang.psi.TaraAspectApply;
import io.intino.plugin.lang.psi.TaraParameter;
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

	public TaraAspectApply isInFacet() {
		PsiElement aElement = this;
		while (!(aElement.getParent() instanceof Node) && !(aElement.getParent() instanceof TaraAspectApply))
			aElement = aElement.getParent();
		return (aElement.getParent() instanceof TaraAspectApply) ? (TaraAspectApply) aElement.getParent() : null;
	}
}
