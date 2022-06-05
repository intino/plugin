// This is a generated file. Not intended for manual editing.
package io.intino.plugin.lang.psi;

import com.intellij.pom.Navigatable;
import io.intino.magritte.lang.model.Aspect;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface TaraAspectApply extends TaraPsiElement, Aspect, Navigatable {

	@NotNull
	TaraMetaIdentifier getMetaIdentifier();

	@Nullable
	TaraParameters getParameters();

	void addParameter(String name, int position, List<Object> values);
}
