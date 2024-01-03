// This is a generated file. Not intended for manual editing.
package io.intino.plugin.lang.psi.impl;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import io.intino.plugin.lang.psi.TaraDoubleValue;
import io.intino.plugin.lang.psi.TaraIntegerValue;
import io.intino.plugin.lang.psi.TaraRange;
import io.intino.plugin.lang.psi.TaraVisitor;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class TaraRangeImpl extends ASTWrapperPsiElement implements TaraRange {

  public TaraRangeImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull TaraVisitor visitor) {
    visitor.visitRange(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof TaraVisitor) accept((TaraVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public List<TaraDoubleValue> getDoubleValueList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, TaraDoubleValue.class);
  }

  @Override
  @NotNull
  public List<TaraIntegerValue> getIntegerValueList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, TaraIntegerValue.class);
  }

}
