// This is a generated file. Not intended for manual editing.
package io.intino.plugin.lang.psi.impl;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import io.intino.plugin.lang.psi.TaraConstraint;
import io.intino.plugin.lang.psi.TaraIdentifierReference;
import io.intino.plugin.lang.psi.TaraVisitor;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class TaraConstraintImpl extends ASTWrapperPsiElement implements TaraConstraint {

	public TaraConstraintImpl(@NotNull ASTNode node) {
		super(node);
	}

	public void accept(@NotNull TaraVisitor visitor) {
		visitor.visitConstraint(this);
	}

	public void accept(@NotNull PsiElementVisitor visitor) {
		if (visitor instanceof TaraVisitor) accept((TaraVisitor) visitor);
		else super.accept(visitor);
	}

	@Override
	@NotNull
	public List<TaraIdentifierReference> getIdentifierReferenceList() {
		return PsiTreeUtil.getChildrenOfTypeAsList(this, TaraIdentifierReference.class);
	}

}
