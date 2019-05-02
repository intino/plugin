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

public class ItrulesSyntaxHighlighter extends SyntaxHighlighterBase implements ItrulesTypes {
    public static final TextAttributesKey KEYWORD = TextAttributesKey.createTextAttributesKey("Itrules_KEYWORD", DefaultLanguageHighlighterColors.KEYWORD);
    public static final TextAttributesKey IDENTIFIER = TextAttributesKey.createTextAttributesKey("Itrules_IDENTIFIER", DefaultLanguageHighlighterColors.STRING);
    private static final Map<IElementType, TextAttributesKey> KEYS;

    static {
        KEYS = new HashMap<>();
        KEYS.put(ItrulesTypes.DEFRULE, KEYWORD);
        KEYS.put(ItrulesTypes.ENDRULE, KEYWORD);
        KEYS.put(ItrulesTypes.MARK, IDENTIFIER);
        KEYS.put(ItrulesTypes.SCAPED_CHAR, IDENTIFIER);
        KEYS.put(ItrulesTypes.LEFT_SQUARE, KEYWORD);
        KEYS.put(ItrulesTypes.RIGHT_SQUARE, KEYWORD);
    }

    @NotNull
    @Override
    public Lexer getHighlightingLexer() {
        return new ItrulesLexerAdapter();
    }

    @NotNull
    @Override
    public TextAttributesKey[] getTokenHighlights(IElementType tokenType) {
        return SyntaxHighlighterBase.pack(KEYS.get(tokenType));
    }
}
