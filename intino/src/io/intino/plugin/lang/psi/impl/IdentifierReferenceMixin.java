package io.intino.plugin.lang.psi.impl;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import org.jetbrains.annotations.NotNull;

public class IdentifierReferenceMixin extends ASTWrapperPsiElement {
	public IdentifierReferenceMixin(@NotNull ASTNode node) {
		super(node);
	}
}
