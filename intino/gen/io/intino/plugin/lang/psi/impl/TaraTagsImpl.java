// This is a generated file. Not intended for manual editing.
package io.intino.plugin.lang.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElementVisitor;
import io.intino.plugin.lang.psi.TaraAnnotations;
import io.intino.plugin.lang.psi.TaraFlags;
import io.intino.plugin.lang.psi.TaraTags;
import io.intino.plugin.lang.psi.TaraVisitor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TaraTagsImpl extends TagsMixin implements TaraTags {

	public TaraTagsImpl(ASTNode node) {
		super(node);
	}

	public void accept(@NotNull TaraVisitor visitor) {
		visitor.visitTags(this);
	}

	public void accept(@NotNull PsiElementVisitor visitor) {
		if (visitor instanceof TaraVisitor) accept((TaraVisitor) visitor);
		else super.accept(visitor);
	}

	@Override
	@Nullable
	public TaraAnnotations getAnnotations() {
		return findChildByClass(TaraAnnotations.class);
	}

	@Override
	@Nullable
	public TaraFlags getFlags() {
		return findChildByClass(TaraFlags.class);
	}

}
