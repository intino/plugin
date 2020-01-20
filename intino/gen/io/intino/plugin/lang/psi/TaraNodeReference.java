// This is a generated file. Not intended for manual editing.
package io.intino.plugin.lang.psi;

import com.intellij.pom.Navigatable;
import io.intino.tara.lang.model.Node;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface TaraNodeReference extends TaraPsiElement, Node, Navigatable {

	@Nullable
	TaraIdentifierReference getIdentifierReference();

	@NotNull
	List<TaraRuleContainer> getRuleContainerList();

	@Nullable
	TaraTags getTags();

}
