// This is a generated file. Not intended for manual editing.
package io.intino.plugin.lang.psi;

import com.intellij.openapi.util.Iconable;
import com.intellij.pom.Navigatable;
import io.intino.magritte.lang.model.Node;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface TaraNode extends TaraPsiElement, Node, Iconable, Navigatable {

	@Nullable
	TaraBody getBody();

	@Nullable
	TaraDoc getDoc();

	@NotNull
	TaraSignature getSignature();

	String simpleType();

	void applyAspect(String aspect);

}
