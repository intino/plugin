// This is a generated file. Not intended for manual editing.
package io.intino.plugin.lang.psi.impl;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElementVisitor;
import io.intino.plugin.lang.psi.TaraDoubleValue;
import io.intino.plugin.lang.psi.TaraStringValue;
import io.intino.plugin.lang.psi.TaraTupleValue;
import io.intino.plugin.lang.psi.TaraVisitor;
import org.jetbrains.annotations.NotNull;

public class TaraTupleValueImpl extends ASTWrapperPsiElement implements TaraTupleValue {

  public TaraTupleValueImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull TaraVisitor visitor) {
    visitor.visitTupleValue(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof TaraVisitor) accept((TaraVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public TaraDoubleValue getDoubleValue() {
    return findNotNullChildByClass(TaraDoubleValue.class);
  }

  @Override
  @NotNull
  public TaraStringValue getStringValue() {
    return findNotNullChildByClass(TaraStringValue.class);
  }

}
