// This is a generated file. Not intended for manual editing.
package io.intino.plugin.archetype.lang.psi.impl;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import static io.intino.plugin.archetype.lang.psi.ArchetypeTypes.*;
import com.intellij.extapi.psi.ASTWrapperPsiElement;
import io.intino.plugin.archetype.lang.psi.*;

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
