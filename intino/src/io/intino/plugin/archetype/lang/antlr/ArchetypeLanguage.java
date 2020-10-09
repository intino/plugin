package io.intino.plugin.archetype.lang.antlr;

import com.intellij.lang.Language;

public class ArchetypeLanguage extends Language {

	public static final ArchetypeLanguage INSTANCE = new ArchetypeLanguage();

	private ArchetypeLanguage() {
		super("Archetype");
	}
}
