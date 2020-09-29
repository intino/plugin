package io.intino.plugin.lang.psi.impl;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import io.intino.plugin.lang.psi.TaraTypes;

public class RuleMixin extends ASTWrapperPsiElement {

	public RuleMixin(ASTNode node) {
		super(node);
	}


	public boolean isLambda() {
		return this.getFirstChild().getNode().getElementType().equals(TaraTypes.LEFT_CURLY);
	}

	public boolean accept(Object value) {
		return false;
	}

	@Override
	public String toString() {
		return getText();
	}
}