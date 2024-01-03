// This is a generated file. Not intended for manual editing.
package io.intino.plugin.lang.psi;

import com.intellij.pom.Navigatable;
import io.intino.tara.language.model.Mogram;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface TaraMogramReference extends TaraPsiElement, Mogram, Navigatable {

  @Nullable
  TaraIdentifierReference getIdentifierReference();

  @NotNull
  List<TaraRuleContainer> getRuleContainerList();

  @Nullable
  TaraTags getTags();

}
