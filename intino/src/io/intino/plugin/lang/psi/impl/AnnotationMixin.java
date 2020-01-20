package io.intino.plugin.lang.psi.impl;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;

public class AnnotationMixin extends ASTWrapperPsiElement {

	public AnnotationMixin(ASTNode node) {
		super(node);
	}

	@Override
	public String toString() {
		return getText();
	}
}

