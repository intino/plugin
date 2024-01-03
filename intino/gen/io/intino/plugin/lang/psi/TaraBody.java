// This is a generated file. Not intended for manual editing.
package io.intino.plugin.lang.psi;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface TaraBody extends Body {

  @NotNull
  List<TaraMogram> getMogramList();

  @NotNull
  List<TaraMogramReference> getMogramReferenceList();

  @NotNull
  List<TaraVarInit> getVarInitList();

  @NotNull
  List<TaraVariable> getVariableList();

}
