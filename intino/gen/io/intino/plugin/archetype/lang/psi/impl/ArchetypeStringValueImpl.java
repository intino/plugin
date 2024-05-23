// This is a generated file. Not intended for manual editing.
package io.intino.plugin.archetype.lang.psi.impl;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElementVisitor;
import io.intino.plugin.archetype.lang.psi.ArchetypeStringValue;
import io.intino.plugin.archetype.lang.psi.ArchetypeVisitor;
import org.jetbrains.annotations.NotNull;

public class ArchetypeStringValueImpl extends ASTWrapperPsiElement implements ArchetypeStringValue {

  public ArchetypeStringValueImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull ArchetypeVisitor visitor) {
    visitor.visitStringValue(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof ArchetypeVisitor) accept((ArchetypeVisitor)visitor);
    else super.accept(visitor);
  }

}
