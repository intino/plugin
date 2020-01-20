package io.intino.plugin.lang.psi;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import org.jetbrains.annotations.NotNull;

public interface TaraPsiElement extends PsiElement {

	@NotNull
	ASTNode getNode();

	void accept(@NotNull PsiElementVisitor visitor);

}
