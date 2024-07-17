package io.intino.plugin.itrules.highlighting;

import com.intellij.lexer.Lexer;
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase;
import com.intellij.psi.tree.IElementType;
import io.intino.plugin.itrules.lang.lexer.ItrulesLexerAdapter;
import io.intino.plugin.itrules.lang.psi.ItrulesTypes;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

import static com.intellij.openapi.editor.colors.TextAttributesKey.createTextAttributesKey;

public class ItrulesSyntaxHighlighter extends SyntaxHighlighterBase implements ItrulesTypes {
	public static final TextAttributesKey KEYWORD = TextAttributesKey.createTextAttributesKey("Itrules_KEYWORD", DefaultLanguageHighlighterColors.KEYWORD);
	public static final TextAttributesKey IDENTIFIER = TextAttributesKey.createTextAttributesKey("Itrules_IDENTIFIER", DefaultLanguageHighlighterColors.STRING);
	public static final TextAttributesKey FORMATTER = createTextAttributesKey("Itrules_FORMATTER", DefaultLanguageHighlighterColors.INSTANCE_METHOD);
	private static final Map<IElementType, TextAttributesKey> KEYS;

	static {
		KEYS = new HashMap<>();
		KEYS.put(ItrulesTypes.DEFRULE, KEYWORD);
		KEYS.put(ItrulesTypes.PLACEHOLDER, IDENTIFIER);
		KEYS.put(ItrulesTypes.FORMATTER, FORMATTER);
		KEYS.put(ItrulesTypes.MULTIPLE, FORMATTER);
		KEYS.put(ItrulesTypes.SCAPED_CHAR, IDENTIFIER);
		KEYS.put(ItrulesTypes.LEFT_EXPR, KEYWORD);
		KEYS.put(ItrulesTypes.RIGHT_EXPR, KEYWORD);
		KEYS.put(ItrulesTypes.AND, KEYWORD);
		KEYS.put(ItrulesTypes.OR, KEYWORD);
		KEYS.put(ItrulesTypes.NOT, KEYWORD);
	}

	@NotNull
	@Override
	public Lexer getHighlightingLexer() {
		return new ItrulesLexerAdapter();
	}

	@NotNull
	@Override
	public TextAttributesKey @NotNull [] getTokenHighlights(IElementType tokenType) {
		return SyntaxHighlighterBase.pack(KEYS.get(tokenType));
	}
}
