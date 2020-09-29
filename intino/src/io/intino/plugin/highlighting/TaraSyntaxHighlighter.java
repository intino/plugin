package io.intino.plugin.highlighting;

import com.intellij.ide.DataManager;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.psi.TokenType;
import com.intellij.psi.tree.IElementType;
import com.intellij.ui.JBColor;
import gnu.trove.THashMap;
import io.intino.plugin.lang.psi.TaraTypes;
import io.intino.plugin.messages.MessageProvider;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.Map;

import static com.intellij.openapi.editor.colors.TextAttributesKey.createTextAttributesKey;


public class TaraSyntaxHighlighter extends SyntaxHighlighterBase implements TaraTypes {

	static final TextAttributesKey META_IDENTIFIER = createTextAttributesKey("Tara_METAIDENTIFIER", DefaultLanguageHighlighterColors.INSTANCE_METHOD);
	static final TextAttributesKey IDENTIFIER = createTextAttributesKey("Tara_IDENTIFIER", DefaultLanguageHighlighterColors.CLASS_NAME);
	static final TextAttributesKey OPERATOR = createTextAttributesKey("Tara_OPERATOR", DefaultLanguageHighlighterColors.OPERATION_SIGN);
	static final TextAttributesKey STRING = createTextAttributesKey("Tara_STRING", DefaultLanguageHighlighterColors.NUMBER);
	//	static final TextAttributesKey NATIVE = createTextAttributesKey("Tara_NATIVE", DefaultLanguageHighlighterColors.NUMBER);
	static final TextAttributesKey ANCHORS = createTextAttributesKey("Tara_ANCHORS", DefaultLanguageHighlighterColors.BLOCK_COMMENT);
	static final TextAttributesKey PRIMITIVE = createTextAttributesKey("Tara_PRIMITIVE", DefaultLanguageHighlighterColors.INSTANCE_FIELD);
	static final TextAttributesKey ANNOTATION = createTextAttributesKey("Tara_ANNOTATION", DefaultLanguageHighlighterColors.METADATA);
	static final TextAttributesKey NUMBER = createTextAttributesKey("Tara_NUMBER", DefaultLanguageHighlighterColors.NUMBER);
	static final TextAttributesKey BAD_CHARACTER = createTextAttributesKey("Tara_BAD_CHARACTER");
	private static final TextAttributesKey KEYWORD = createTextAttributesKey("Tara_KEYWORD", DefaultLanguageHighlighterColors.KEYWORD);
	private static final TextAttributesKey BRACKETS = createTextAttributesKey("Tara_BRACKETS", DefaultLanguageHighlighterColors.BRACKETS);
	private static final TextAttributesKey SEMICOLON_KEY = createTextAttributesKey("Tara_SEMICOLON", DefaultLanguageHighlighterColors.SEMICOLON);
	private static final TextAttributesKey LINE_COMMENT = createTextAttributesKey("Tara_TARA_COMMENT", DefaultLanguageHighlighterColors.LINE_COMMENT);
	public static final TextAttributesKey UNRESOLVED_ACCESS = createTextAttributesKey("Tara_UNRESOLVED_ACCESS", referenceNotFoundTextAttributes());
	public static final TextAttributesKey DOCUMENTATION = createTextAttributesKey("Tara_COMMENT", DefaultLanguageHighlighterColors.DOC_COMMENT);

	private static final Map<TextAttributesKey, Pair<String, HighlightSeverity>> DISPLAY_NAMES = new THashMap<>();
	private static final Map<IElementType, TextAttributesKey> KEYS;

	static {
		DISPLAY_NAMES.put(IDENTIFIER, new Pair<>(MessageProvider.message("options.tara.concept.identifier"), null));
		DISPLAY_NAMES.put(KEYWORD, new Pair<>(MessageProvider.message("options.tara.concept.keyword"), null));
		DISPLAY_NAMES.put(PRIMITIVE, new Pair<>(MessageProvider.message("options.tara.concept.primitive"), null));
		DISPLAY_NAMES.put(STRING, new Pair<>(MessageProvider.message("options.tara.types.string"), null));
		DISPLAY_NAMES.put(DOCUMENTATION, new Pair<>(MessageProvider.message("options.tara.concept.comment"), null));
		DISPLAY_NAMES.put(SEMICOLON_KEY, new Pair<>(MessageProvider.message("options.tara.concept.semicolon"), null));
		DISPLAY_NAMES.put(OPERATOR, new Pair<>(MessageProvider.message("options.tara.concept.operator"), null));
		DISPLAY_NAMES.put(ANNOTATION, new Pair<>(MessageProvider.message("options.tara.concept.annotation"), null));
		DISPLAY_NAMES.put(NUMBER, new Pair<>(MessageProvider.message("options.tara.number"), null));
		DISPLAY_NAMES.put(BRACKETS, new Pair<>(MessageProvider.message("options.tara.concept.annotation"), null));
		DISPLAY_NAMES.put(BAD_CHARACTER, Pair.create(MessageProvider.message("invalid.tara.concept.character"), HighlightSeverity.ERROR));
	}

	static {
		KEYS = new THashMap<>();

		KEYS.put(METAIDENTIFIER_KEY, META_IDENTIFIER);
		KEYS.put(DSL, KEYWORD);
		KEYS.put(EMPTY_REF, KEYWORD);
		KEYS.put(HAS, KEYWORD);
		KEYS.put(EXTENDS, KEYWORD);
		KEYS.put(AT, KEYWORD);

		KEYS.put(COMPONENT, ANNOTATION);
		KEYS.put(PRIVATE, ANNOTATION);
		KEYS.put(FEATURE, ANNOTATION);
		KEYS.put(ENCLOSED, ANNOTATION);
		KEYS.put(REACTIVE, ANNOTATION);
		KEYS.put(CONCEPT, ANNOTATION);
		KEYS.put(ABSTRACT, ANNOTATION);
		KEYS.put(VOLATILE, ANNOTATION);
		KEYS.put(TERMINAL, ANNOTATION);
		KEYS.put(DECORABLE, ANNOTATION);
		KEYS.put(REQUIRED, ANNOTATION);

		KEYS.put(FINAL, ANNOTATION);

		KEYS.put(NEW_LINE_INDENT, KEYWORD);
		KEYS.put(NEWLINE, SEMICOLON_KEY);
		KEYS.put(COMMENT, LINE_COMMENT);

		KEYS.put(IDENTIFIER_KEY, IDENTIFIER);
		KEYS.put(SUB, KEYWORD);
		KEYS.put(USE, KEYWORD);
		KEYS.put(IS, KEYWORD);
		KEYS.put(INTO, KEYWORD);
		KEYS.put(VAR, KEYWORD);
		KEYS.put(AS, KEYWORD);
		KEYS.put(WITH, KEYWORD);
		KEYS.put(COLON, OPERATOR);
		KEYS.put(EQUALS, OPERATOR);
		KEYS.put(LEFT_SQUARE, OPERATOR);
		KEYS.put(RIGHT_SQUARE, OPERATOR);
		KEYS.put(LEFT_CURLY, OPERATOR);
		KEYS.put(RIGHT_CURLY, OPERATOR);

		KEYS.put(WORD_TYPE, PRIMITIVE);
		KEYS.put(STRING_TYPE, PRIMITIVE);
		KEYS.put(DOUBLE_TYPE, PRIMITIVE);
		KEYS.put(FUNCTION_TYPE, PRIMITIVE);
		KEYS.put(RESOURCE_TYPE, PRIMITIVE);
		KEYS.put(OBJECT_TYPE, PRIMITIVE);
		KEYS.put(INT_TYPE, PRIMITIVE);
		KEYS.put(LONG_TYPE, PRIMITIVE);
		KEYS.put(DATE_TYPE, PRIMITIVE);
		KEYS.put(INSTANT_TYPE, PRIMITIVE);
		KEYS.put(TIME_TYPE, PRIMITIVE);
		KEYS.put(BOOLEAN_TYPE, PRIMITIVE);

		KEYS.put(DOC_LINE, DOCUMENTATION);

		KEYS.put(DOUBLE_VALUE_KEY, NUMBER);
		KEYS.put(NATURAL_VALUE_KEY, NUMBER);
		KEYS.put(NEGATIVE_VALUE_KEY, NUMBER);
		KEYS.put(BOOLEAN_VALUE_KEY, KEYWORD);
		KEYS.put(QUOTE_BEGIN, STRING);
		KEYS.put(QUOTE_END, STRING);
		KEYS.put(CHARACTER, STRING);
		KEYS.put(TokenType.WHITE_SPACE, null);
		KEYS.put(TokenType.BAD_CHARACTER, BAD_CHARACTER);
	}

	public TaraSyntaxHighlighter() {
	}

	private static TextAttributes referenceNotFoundTextAttributes() {
		return new TextAttributes(JBColor.RED, null, null, null, Font.BOLD);
	}

	private static Project project;

	public static void setProject(Project project) {
		TaraSyntaxHighlighter.project = project;
	}

	@NotNull
	@Override
	public Lexer getHighlightingLexer() {
		if (project == null) {
			final DataContext[] result = {context()};
			if (result[0] == null)
				if (!EventQueue.isDispatchThread()) {
					try {
						final Application application = ApplicationManager.getApplication();
//						application.acquireReadActionLock().close();
						application.invokeAndWait(() -> result[0] = syncContext());
					} catch (Throwable ignored) {
					}
				} else result[0] = syncContext();
			project = result[0] != null ? result[0].getData(LangDataKeys.PROJECT) : WindowManager.getInstance().getAllProjectFrames()[0].getProject();
		}

		return new TaraHighlighterLexAdapter(project);
	}

	private DataContext syncContext() {
		try {
			return DataManager.getInstance().getDataContextFromFocusAsync().blockingGet(100);
		} catch (Throwable e) {
			return null;
		}
	}

	private DataContext context() {
		try {
			return io.intino.plugin.project.DataContext.getContext();
		} catch (Throwable e) {
			return null;
		}
	}

	@NotNull
	@Override
	public TextAttributesKey[] getTokenHighlights(IElementType tokenType) {
		return pack(KEYS.get(tokenType));
	}

}