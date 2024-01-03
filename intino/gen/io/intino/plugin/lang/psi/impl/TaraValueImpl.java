// This is a generated file. Not intended for manual editing.
package io.intino.plugin.lang.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import io.intino.plugin.lang.psi.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class TaraValueImpl extends ValueMixin implements TaraValue {

  public TaraValueImpl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull TaraVisitor visitor) {
    visitor.visitValue(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof TaraVisitor) accept((TaraVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public List<TaraBooleanValue> getBooleanValueList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, TaraBooleanValue.class);
  }

  @Override
  @NotNull
  public List<TaraDoubleValue> getDoubleValueList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, TaraDoubleValue.class);
  }

  @Override
  @Nullable
  public TaraEmptyField getEmptyField() {
    return findChildByClass(TaraEmptyField.class);
  }

  @Override
  @NotNull
  public List<TaraExpression> getExpressionList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, TaraExpression.class);
  }

  @Override
  @NotNull
  public List<TaraIdentifierReference> getIdentifierReferenceList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, TaraIdentifierReference.class);
  }

  @Override
  @NotNull
  public List<TaraIntegerValue> getIntegerValueList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, TaraIntegerValue.class);
  }

  @Override
  @NotNull
  public List<TaraMethodReference> getMethodReferenceList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, TaraMethodReference.class);
  }

  @Override
  @Nullable
  public TaraMetric getMetric() {
    return findChildByClass(TaraMetric.class);
  }

  @Override
  @NotNull
  public List<TaraStringValue> getStringValueList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, TaraStringValue.class);
  }

  @Override
  @NotNull
  public List<TaraTupleValue> getTupleValueList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, TaraTupleValue.class);
  }

}
