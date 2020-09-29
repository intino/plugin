package io.intino.plugin.lang.lexer;

import com.intellij.lexer.FlexAdapter;

public class TaraLexerAdapter extends FlexAdapter {

	public TaraLexerAdapter() {
		super(new TaraLexer(null));
	}
}