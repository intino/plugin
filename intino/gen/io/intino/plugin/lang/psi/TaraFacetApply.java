// This is a generated file. Not intended for manual editing.
package io.intino.plugin.lang.psi;

import com.intellij.pom.Navigatable;
import io.intino.tara.language.model.Facet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface TaraFacetApply extends TaraPsiElement, Facet, Navigatable {

  @NotNull
  TaraMetaIdentifier getMetaIdentifier();

  @Nullable
  TaraParameters getParameters();

}
