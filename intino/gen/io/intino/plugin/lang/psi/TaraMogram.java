// This is a generated file. Not intended for manual editing.
package io.intino.plugin.lang.psi;

import com.intellij.openapi.util.Iconable;
import com.intellij.pom.Navigatable;
import io.intino.tara.language.model.Mogram;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface TaraMogram extends TaraPsiElement, Mogram, Iconable, Navigatable {

  @Nullable
  TaraBody getBody();

  @Nullable
  TaraDoc getDoc();

  @NotNull
  TaraSignature getSignature();

}
