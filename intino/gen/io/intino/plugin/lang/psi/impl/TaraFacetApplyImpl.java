// This is a generated file. Not intended for manual editing.
package io.intino.plugin.lang.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElementVisitor;
import io.intino.plugin.lang.psi.TaraFacetApply;
import io.intino.plugin.lang.psi.TaraMetaIdentifier;
import io.intino.plugin.lang.psi.TaraParameters;
import io.intino.plugin.lang.psi.TaraVisitor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TaraFacetApplyImpl extends FacetApplyMixin implements TaraFacetApply {

  public TaraFacetApplyImpl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull TaraVisitor visitor) {
    visitor.visitFacetApply(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof TaraVisitor) accept((TaraVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public TaraMetaIdentifier getMetaIdentifier() {
    return findNotNullChildByClass(TaraMetaIdentifier.class);
  }

  @Override
  @Nullable
  public TaraParameters getParameters() {
    return findChildByClass(TaraParameters.class);
  }

}
