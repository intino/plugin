package io.intino.plugin.lang.psi.impl;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;

public class FlagMixin extends ASTWrapperPsiElement {

	public FlagMixin(ASTNode node) {
		super(node);
	}
}
