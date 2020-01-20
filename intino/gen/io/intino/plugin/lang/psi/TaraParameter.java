// This is a generated file. Not intended for manual editing.
package io.intino.plugin.lang.psi;

import com.intellij.pom.Navigatable;
import io.intino.tara.lang.model.Parameter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface TaraParameter extends Valued, Parameter, Navigatable {

	@Nullable
	TaraIdentifier getIdentifier();

	@NotNull
	TaraValue getValue();

}
