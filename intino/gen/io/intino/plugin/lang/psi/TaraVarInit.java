// This is a generated file. Not intended for manual editing.
package io.intino.plugin.lang.psi;

import io.intino.tara.language.model.Parameter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface TaraVarInit extends Valued, Parameter {

  @Nullable
  TaraBodyValue getBodyValue();

  @NotNull
  TaraIdentifier getIdentifier();

  @Nullable
  TaraValue getValue();

}
