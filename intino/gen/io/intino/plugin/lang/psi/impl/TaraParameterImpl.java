// This is a generated file. Not intended for manual editing.
package io.intino.plugin.lang.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElementVisitor;
import io.intino.plugin.lang.psi.TaraIdentifier;
import io.intino.plugin.lang.psi.TaraParameter;
import io.intino.plugin.lang.psi.TaraValue;
import io.intino.plugin.lang.psi.TaraVisitor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TaraParameterImpl extends ParameterMixin implements TaraParameter {

  public TaraParameterImpl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull TaraVisitor visitor) {
    visitor.visitParameter(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof TaraVisitor) accept((TaraVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public TaraIdentifier getIdentifier() {
    return findChildByClass(TaraIdentifier.class);
  }

  @Override
  @NotNull
  public TaraValue getValue() {
    return findNotNullChildByClass(TaraValue.class);
  }

}
