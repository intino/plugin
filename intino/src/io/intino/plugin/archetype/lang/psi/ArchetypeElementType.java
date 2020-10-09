package io.intino.plugin.archetype.lang.psi;

import com.intellij.psi.tree.IElementType;
import io.intino.plugin.archetype.lang.antlr.ArchetypeLanguage;
import io.intino.plugin.itrules.lang.ItrulesLanguage;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

public class ArchetypeElementType extends IElementType {

	public ArchetypeElementType(@NotNull @NonNls String name) {
		super(name, ArchetypeLanguage.INSTANCE);
	}
}
