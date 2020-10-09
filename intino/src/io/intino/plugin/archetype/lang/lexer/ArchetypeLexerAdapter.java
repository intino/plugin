package io.intino.plugin.archetype.lang.lexer;

import com.intellij.lexer.FlexAdapter;

public class ArchetypeLexerAdapter extends FlexAdapter {
	public ArchetypeLexerAdapter() {
		super(new ArchetypeLexer(null));
	}


}
