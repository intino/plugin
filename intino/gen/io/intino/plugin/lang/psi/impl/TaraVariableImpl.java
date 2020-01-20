// This is a generated file. Not intended for manual editing.
package io.intino.plugin.lang.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElementVisitor;
import io.intino.plugin.lang.psi.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TaraVariableImpl extends VariableMixin implements TaraVariable {

	public TaraVariableImpl(@NotNull ASTNode node) {
		super(node);
	}

	public void accept(@NotNull TaraVisitor visitor) {
		visitor.visitVariable(this);
	}

	public void accept(@NotNull PsiElementVisitor visitor) {
		if (visitor instanceof TaraVisitor) accept((TaraVisitor) visitor);
		else super.accept(visitor);
	}

	@Override
	@Nullable
	public TaraBodyValue getBodyValue() {
		return findChildByClass(TaraBodyValue.class);
	}

	@Override
	@Nullable
	public TaraDoc getDoc() {
		return findChildByClass(TaraDoc.class);
	}

	@Override
	@Nullable
	public TaraFlags getFlags() {
		return findChildByClass(TaraFlags.class);
	}

	@Override
	@Nullable
	public TaraIdentifier getIdentifier() {
		return findChildByClass(TaraIdentifier.class);
	}

	@Override
	@Nullable
	public TaraRuleContainer getRuleContainer() {
		return findChildByClass(TaraRuleContainer.class);
	}

	@Override
	@Nullable
	public TaraSizeRange getSizeRange() {
		return findChildByClass(TaraSizeRange.class);
	}

	@Override
	@Nullable
	public TaraValue getValue() {
		return findChildByClass(TaraValue.class);
	}

	@Override
	@Nullable
	public TaraVariableType getVariableType() {
		return findChildByClass(TaraVariableType.class);
	}

}
