// This is a generated file. Not intended for manual editing.
package io.intino.plugin.lang.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import io.intino.plugin.lang.psi.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class TaraSignatureImpl extends SignatureMixin implements TaraSignature {

	public TaraSignatureImpl(@NotNull ASTNode node) {
		super(node);
	}

	public void accept(@NotNull TaraVisitor visitor) {
		visitor.visitSignature(this);
	}

	public void accept(@NotNull PsiElementVisitor visitor) {
		if (visitor instanceof TaraVisitor) accept((TaraVisitor) visitor);
		else super.accept(visitor);
	}

	@Override
	@Nullable
	public TaraAspects getAspects() {
		return findChildByClass(TaraAspects.class);
	}

	@Override
	@Nullable
	public TaraConstraint getConstraint() {
		return findChildByClass(TaraConstraint.class);
	}

	@Override
	@Nullable
	public TaraIdentifier getIdentifier() {
		return findChildByClass(TaraIdentifier.class);
	}

	@Override
	@Nullable
	public TaraIdentifierReference getIdentifierReference() {
		return findChildByClass(TaraIdentifierReference.class);
	}

	@Override
	@Nullable
	public TaraMetaIdentifier getMetaIdentifier() {
		return findChildByClass(TaraMetaIdentifier.class);
	}

	@Override
	@Nullable
	public TaraParameters getParameters() {
		return findChildByClass(TaraParameters.class);
	}

	@Override
	@NotNull
	public List<TaraRuleContainer> getRuleContainerList() {
		return PsiTreeUtil.getChildrenOfTypeAsList(this, TaraRuleContainer.class);
	}

	@Override
	@Nullable
	public TaraTags getTags() {
		return findChildByClass(TaraTags.class);
	}

}
