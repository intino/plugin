// This is a generated file. Not intended for manual editing.
package io.intino.plugin.lang.psi.impl;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import io.intino.plugin.lang.psi.TaraAspectApply;
import io.intino.plugin.lang.psi.TaraAspects;
import io.intino.plugin.lang.psi.TaraVisitor;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class TaraAspectsImpl extends ASTWrapperPsiElement implements TaraAspects {

	public TaraAspectsImpl(@NotNull ASTNode node) {
		super(node);
	}

	public void accept(@NotNull TaraVisitor visitor) {
		visitor.visitAspects(this);
	}

	public void accept(@NotNull PsiElementVisitor visitor) {
		if (visitor instanceof TaraVisitor) accept((TaraVisitor) visitor);
		else super.accept(visitor);
	}

	@Override
	@NotNull
	public List<TaraAspectApply> getAspectApplyList() {
		return PsiTreeUtil.getChildrenOfTypeAsList(this, TaraAspectApply.class);
	}

}
