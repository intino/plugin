// This is a generated file. Not intended for manual editing.
package io.intino.plugin.lang.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElementVisitor;
import io.intino.plugin.lang.psi.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TaraNodeImpl extends NodeMixin implements TaraNode {

	public TaraNodeImpl(@NotNull ASTNode node) {
		super(node);
	}

	public void accept(@NotNull TaraVisitor visitor) {
		visitor.visitNode(this);
	}

	public void accept(@NotNull PsiElementVisitor visitor) {
		if (visitor instanceof TaraVisitor) accept((TaraVisitor) visitor);
		else super.accept(visitor);
	}

	@Override
	@Nullable
	public TaraBody getBody() {
		return findChildByClass(TaraBody.class);
	}

	@Override
	@Nullable
	public TaraDoc getDoc() {
		return findChildByClass(TaraDoc.class);
	}

	@Override
	@NotNull
	public TaraSignature getSignature() {
		return findNotNullChildByClass(TaraSignature.class);
	}

}
