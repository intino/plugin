package io.intino.plugin.lang.lexer;

import com.intellij.psi.TokenType;
import com.intellij.psi.tree.IElementType;
import io.intino.plugin.lang.psi.TaraTypes;

import java.util.Arrays;

public class BlockManager {

	private int level;
	private int tabSize;
	private IElementType[] tokens;

	public BlockManager() {
		this.tokens = new IElementType[]{};
		this.level = 0;
		this.tabSize = 4;
	}

	public void spaces(String text) {
		int newLevel = spacesLength(text) / this.tabSize;
		this.tokens = spacesIndentTokens(newLevel - level);
		this.level = newLevel;
	}

	private int spacesLength(String text) {
		int value = 0;
		for (int i = 0; i < text.length(); i++)
			value += (text.charAt(i) == ('\t')) ? this.tabSize : 1;
		return value;
	}

	private IElementType[] spacesIndentTokens(int size) {
		int length = (size > 0) ? size : Math.abs(size * 2) + 1;
		IElementType[] actions = new IElementType[length];
		if (size > 0) return new IElementType[]{TokenType.NEW_LINE_INDENT};
		else
			for (int i = 0; i < actions.length; i++)
				actions[i] = (i % 2 == 0) ? TaraTypes.NEWLINE : TaraTypes.DEDENT;
		return actions;
	}

	private IElementType[] indentTokens(int size) {
		IElementType[] actions = new IElementType[Math.abs(size * 2)];
		if (size > 0) return new IElementType[]{TokenType.NEW_LINE_INDENT};
		else
			for (int i = 0; i < actions.length; i++)
				actions[i] = (i % 2 == 0) ? TaraTypes.NEWLINE : TaraTypes.DEDENT;
		return actions;
	}

	private IElementType[] create(IElementType token) {
		return new IElementType[]{token};
	}

	public IElementType[] actions() {
		return Arrays.copyOf(tokens, tokens.length);
	}

	public void openBracket(int size) {
		this.tokens = indentTokens(size);
		this.level += size;
	}

	public void semicolon(int size) {
		if (size == 1)
			this.tokens = create(TaraTypes.NEWLINE);
		else
			this.tokens = create(TokenType.BAD_CHARACTER);
	}

	public void eof() {
		this.tokens = indentTokens(-level);
		this.level--;
	}
}
