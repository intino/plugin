// This is a generated file. Not intended for manual editing.
package io.intino.plugin.lang.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElementVisitor;
import io.intino.plugin.lang.psi.TaraIdentifier;
import io.intino.plugin.lang.psi.TaraVisitor;
import org.jetbrains.annotations.NotNull;

public class TaraIdentifierImpl extends IdentifierMixin implements TaraIdentifier {

	public TaraIdentifierImpl(@NotNull ASTNode node) {
		super(node);
	}

	public void accept(@NotNull TaraVisitor visitor) {
		visitor.visitIdentifier(this);
	}

	public void accept(@NotNull PsiElementVisitor visitor) {
		if (visitor instanceof TaraVisitor) accept((TaraVisitor) visitor);
		else super.accept(visitor);
	}

}
