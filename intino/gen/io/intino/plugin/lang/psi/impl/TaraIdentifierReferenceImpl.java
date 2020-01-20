// This is a generated file. Not intended for manual editing.
package io.intino.plugin.lang.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import io.intino.plugin.lang.psi.TaraIdentifier;
import io.intino.plugin.lang.psi.TaraIdentifierReference;
import io.intino.plugin.lang.psi.TaraVisitor;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class TaraIdentifierReferenceImpl extends IdentifierReferenceMixin implements TaraIdentifierReference {

	public TaraIdentifierReferenceImpl(@NotNull ASTNode node) {
		super(node);
	}

	public void accept(@NotNull TaraVisitor visitor) {
		visitor.visitIdentifierReference(this);
	}

	public void accept(@NotNull PsiElementVisitor visitor) {
		if (visitor instanceof TaraVisitor) accept((TaraVisitor) visitor);
		else super.accept(visitor);
	}

	@Override
	@NotNull
	public List<TaraIdentifier> getIdentifierList() {
		return PsiTreeUtil.getChildrenOfTypeAsList(this, TaraIdentifier.class);
	}

}
