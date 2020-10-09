package io.intino.plugin.archetype.highlighting;

import com.intellij.lexer.Lexer;
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase;
import com.intellij.psi.tree.IElementType;
import io.intino.plugin.archetype.lang.psi.ArchetypeTypes;
import io.intino.plugin.archetype.lang.lexer.ArchetypeLexerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

import static com.intellij.openapi.editor.colors.TextAttributesKey.createTextAttributesKey;

public class ArchetypeSyntaxHighlighter extends SyntaxHighlighterBase implements ArchetypeTypes {
	public static final TextAttributesKey KEYWORD = TextAttributesKey.createTextAttributesKey("Archetype_KEYWORD", DefaultLanguageHighlighterColors.KEYWORD);
	public static final TextAttributesKey IDENTIFIER = TextAttributesKey.createTextAttributesKey("Archetype_IDENTIFIER", DefaultLanguageHighlighterColors.CONSTANT);
	public static final TextAttributesKey STRING = TextAttributesKey.createTextAttributesKey("Archetype_STRING", DefaultLanguageHighlighterColors.NUMBER);
	static final TextAttributesKey ANNOTATION = createTextAttributesKey("Archetype_ANNOTATION", DefaultLanguageHighlighterColors.METADATA);

	private static final Map<IElementType, TextAttributesKey> KEYS;

	static {
		KEYS = new HashMap<>();
		KEYS.put(ArchetypeTypes.KEYWORD, KEYWORD);
		KEYS.put(ArchetypeTypes.PLUS, KEYWORD);
		KEYS.put(ArchetypeTypes.MINUS, KEYWORD);
		KEYS.put(ArchetypeTypes.STAR, KEYWORD);

		KEYS.put(ArchetypeTypes.ANNOTATION, ANNOTATION);

		KEYS.put(ArchetypeTypes.IDENTIFIER_KEY, IDENTIFIER);
		KEYS.put(QUOTE_BEGIN, STRING);
		KEYS.put(QUOTE_END, STRING);
		KEYS.put(CHARACTER, STRING);
	}

	@NotNull
	@Override
	public Lexer getHighlightingLexer() {
		return new ArchetypeLexerAdapter();
	}

	@NotNull
	@Override
	public TextAttributesKey[] getTokenHighlights(IElementType tokenType) {
		return SyntaxHighlighterBase.pack(KEYS.get(tokenType));
	}
}
