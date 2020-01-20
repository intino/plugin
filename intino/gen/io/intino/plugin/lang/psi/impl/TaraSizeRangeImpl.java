// This is a generated file. Not intended for manual editing.
package io.intino.plugin.lang.psi.impl;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElementVisitor;
import io.intino.plugin.lang.psi.TaraSize;
import io.intino.plugin.lang.psi.TaraSizeRange;
import io.intino.plugin.lang.psi.TaraVisitor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TaraSizeRangeImpl extends ASTWrapperPsiElement implements TaraSizeRange {

	public TaraSizeRangeImpl(@NotNull ASTNode node) {
		super(node);
	}

	public void accept(@NotNull TaraVisitor visitor) {
		visitor.visitSizeRange(this);
	}

	public void accept(@NotNull PsiElementVisitor visitor) {
		if (visitor instanceof TaraVisitor) accept((TaraVisitor) visitor);
		else super.accept(visitor);
	}

	@Override
	@Nullable
	public TaraSize getSize() {
		return findChildByClass(TaraSize.class);
	}

}
