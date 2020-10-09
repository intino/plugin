package io.intino.plugin.archetype.lang.antlr;

import java.util.Arrays;

public class BlockManager {

	private int level;
	private final int tabSize;
	private Token[] tokens;

	public BlockManager() {
		this.tokens = new Token[]{};
		this.level = 0;
		this.tabSize = 4;
	}

	public void reset() {
		this.tokens = new Token[]{};
		this.level = 0;
	}

	public void newlineAndSpaces(String text) {
		int newLevel = spacesLength(text) / this.tabSize;
		this.tokens = indentationTokens(newLevel - level, true);
		this.level = newLevel;
	}

	private int spacesLength(String text) {
		int value = 0;
		for (int i = 0; i < text.length(); i++)
			value += text.charAt(i) == '\t' ? this.tabSize : 1;
		return value;
	}

	private Token[] indentationTokens(int size, boolean addLastNewline) {
		if (size > 0)
			return create(Token.NEWLINE_INDENT);
		else {
			int length = !addLastNewline ? Math.abs(size * 2) : Math.abs(size * 2) + 1;
			return createDedents(length);
		}
	}

	private Token[] createDedents(int size) {
		Token[] actions = new Token[size];
		for (int i = 0; i < actions.length; i++)
			actions[i] = i % 2 == 0 ? Token.NEWLINE : Token.DEDENT;
		return actions;
	}

	private Token[] create(Token token) {
		return new Token[]{token};
	}

	public Token[] actions() {
		return Arrays.copyOf(tokens, tokens.length);
	}

	public void openBracket(int size) {
		this.tokens = indentationTokens(size, false);
		this.level += size;
	}

	public void semicolon(int size) {
		if (size == 1)
			this.tokens = create(Token.NEWLINE);
		else
			this.tokens = create(Token.ERROR);
	}

	public void eof() {
		this.tokens = indentationTokens(-level, false);
		this.level--;
	}

	public enum Token {
		NEWLINE_INDENT, DEDENT, NEWLINE, ERROR
	}
}
