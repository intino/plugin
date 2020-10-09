package io.intino.plugin.archetype.lang.psi;

import com.intellij.psi.tree.IElementType;
import io.intino.plugin.archetype.lang.antlr.ArchetypeLanguage;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

public class ArchetypeTokenType extends IElementType {
	public ArchetypeTokenType(@NotNull @NonNls String debugName) {
		super(debugName, ArchetypeLanguage.INSTANCE);
	}

	@Override
	public String toString() {
		return "ArchetypeTokenType." + super.toString();
	}
}