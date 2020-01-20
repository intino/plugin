// Generated from /Users/oroncal/workspace/intino/intino/src/io/intino/plugin/actions/archetype/ArchetypeLexer.g4 by ANTLR 4.7.2
package io.intino.plugin.actions.archetype;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.ATN;
import org.antlr.v4.runtime.atn.ATNDeserializer;
import org.antlr.v4.runtime.atn.LexerATNSimulator;
import org.antlr.v4.runtime.atn.PredictionContextCache;
import org.antlr.v4.runtime.dfa.DFA;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class ArchetypeLexer extends Lexer {
	static {
		RuntimeMetaData.checkVersion("4.7.2", RuntimeMetaData.VERSION);
	}

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
			new PredictionContextCache();
	public static final int
			IN = 1, WITH = 2, LEFT_PARENTHESIS = 3, RIGHT_PARENTHESIS = 4, COMMA = 5, MINUS = 6,
			PLUS = 7, STAR = 8, AS = 9, REGEX = 10, TIMETAG = 11, COLON = 12, LABEL = 13, IDENTIFIER = 14,
			NEWLINE = 15, SPACES = 16, SP = 17, NL = 18, NEW_LINE_INDENT = 19, DEDENT = 20, UNKNOWN_TOKEN = 21,
			QUOTE_BEGIN = 22, QUOTE_END = 23, EXPRESSION_BEGIN = 24, EXPRESSION_END = 25;
	public static String[] channelNames = {
			"DEFAULT_TOKEN_CHANNEL", "HIDDEN"
	};

	public static String[] modeNames = {
			"DEFAULT_MODE"
	};

	private static String[] makeRuleNames() {
		return new String[]{
				"IN", "WITH", "LEFT_PARENTHESIS", "RIGHT_PARENTHESIS", "COMMA", "MINUS",
				"PLUS", "STAR", "AS", "REGEX", "TIMETAG", "COLON", "LABEL", "IDENTIFIER",
				"NEWLINE", "SPACES", "SP", "NL", "NEW_LINE_INDENT", "DEDENT", "UNKNOWN_TOKEN",
				"QUOTE_BEGIN", "QUOTE_END", "EXPRESSION_BEGIN", "EXPRESSION_END", "DOLLAR",
				"EURO", "PERCENTAGE", "GRADE", "BY", "DIVIDED_BY", "DASH", "UNDERDASH",
				"DOT", "DIGIT", "LETTER"
		};
	}

	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[]{
				null, "'in'", "'with'", "'('", "')'", "','", "'-'", "'+'", "'*'", "'as'",
				"'regex'", "'timetag'", "':'", null, null, null, null, null, null, "'indent'",
				"'dedent'", null, "'%QUOTE_BEGIN%'", "'%QUOTE_END%'", "'%EXPRESSION_BEGIN%'",
				"'%EXPRESSION_END%'"
		};
	}

	private static final String[] _LITERAL_NAMES = makeLiteralNames();

	private static String[] makeSymbolicNames() {
		return new String[]{
				null, "IN", "WITH", "LEFT_PARENTHESIS", "RIGHT_PARENTHESIS", "COMMA",
				"MINUS", "PLUS", "STAR", "AS", "REGEX", "TIMETAG", "COLON", "LABEL",
				"IDENTIFIER", "NEWLINE", "SPACES", "SP", "NL", "NEW_LINE_INDENT", "DEDENT",
				"UNKNOWN_TOKEN", "QUOTE_BEGIN", "QUOTE_END", "EXPRESSION_BEGIN", "EXPRESSION_END"
		};
	}

	private static final String[] _SYMBOLIC_NAMES = makeSymbolicNames();
	public static final Vocabulary VOCABULARY = new VocabularyImpl(_LITERAL_NAMES, _SYMBOLIC_NAMES);

	/**
	 * @deprecated Use {@link #VOCABULARY} instead.
	 */
	@Deprecated
	public static final String[] tokenNames;

	static {
		tokenNames = new String[_SYMBOLIC_NAMES.length];
		for (int i = 0; i < tokenNames.length; i++) {
			tokenNames[i] = VOCABULARY.getLiteralName(i);
			if (tokenNames[i] == null) {
				tokenNames[i] = VOCABULARY.getSymbolicName(i);
			}

			if (tokenNames[i] == null) {
				tokenNames[i] = "<INVALID>";
			}
		}
	}

	@Override
	@Deprecated
	public String[] getTokenNames() {
		return tokenNames;
	}

	@Override

	public Vocabulary getVocabulary() {
		return VOCABULARY;
	}


	BlockManager blockManager = new BlockManager();
	private static java.util.Queue<Token> queue = new java.util.LinkedList<>();

	@Override
	public void reset() {
		super.reset();
		queue.clear();
		blockManager.reset();
	}

	@Override
	public void emit(Token token) {
		if (token.getType() == EOF) eof();
		queue.offer(token);
		setToken(token);
	}

	@Override
	public Token nextToken() {
		super.nextToken();
		return queue.isEmpty() ? emitEOF() : queue.poll();
	}

	private void emitToken(int ttype) {
		setType(ttype);
		emit();
	}

	private boolean isWhiteLineOrEOF() {
		int character = _input.LA(1);
		return (character == -1 || (char) character == '\n');
	}

	private void newlinesAndSpaces() {
		if (!isWhiteLineOrEOF()) {
			blockManager.newlineAndSpaces(getTextSpaces(getText()));
			sendTokens();
		} else skip();
	}

	private String getTextSpaces(String text) {
		int index = (text.indexOf(' ') == -1) ? text.indexOf('\t') : text.indexOf(' ');
		return (index == -1) ? "" : text.substring(index);
	}

	private void inline() {
		blockManager.openBracket(getText().length());
		sendTokens();
	}

	private void semicolon() {
		blockManager.semicolon(getText().length());
		sendTokens();
	}

	private void eof() {
		blockManager.eof();
		sendTokens();
	}

	private void sendTokens() {
		blockManager.actions();
		for (BlockManager.Token token : blockManager.actions())
			emitToken(translate(token));
	}

	private int translate(BlockManager.Token token) {
		if (token.toString().equals("NEWLINE")) return NEWLINE;
		if (token.toString().equals("DEDENT")) return DEDENT;
		if (token.toString().equals("NEWLINE_INDENT")) return NEW_LINE_INDENT;
		return UNKNOWN_TOKEN;
	}


	public ArchetypeLexer(CharStream input) {
		super(input);
		_interp = new LexerATNSimulator(this, _ATN, _decisionToDFA, _sharedContextCache);
	}

	@Override
	public String getGrammarFileName() {
		return "ArchetypeLexer.g4";
	}

	@Override
	public String[] getRuleNames() {
		return ruleNames;
	}

	@Override
	public String getSerializedATN() {
		return _serializedATN;
	}

	@Override
	public String[] getChannelNames() {
		return channelNames;
	}

	@Override
	public String[] getModeNames() {
		return modeNames;
	}

	@Override
	public ATN getATN() {
		return _ATN;
	}

	@Override
	public void action(RuleContext _localctx, int ruleIndex, int actionIndex) {
		switch (ruleIndex) {
			case 14:
				NEWLINE_action((RuleContext) _localctx, actionIndex);
				break;
		}
	}

	private void NEWLINE_action(RuleContext _localctx, int actionIndex) {
		switch (actionIndex) {
			case 0:
				newlinesAndSpaces();
				break;
		}
	}

	public static final String _serializedATN =
			"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\2\33\u010f\b\1\4\2" +
					"\t\2\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4" +
					"\13\t\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22" +
					"\t\22\4\23\t\23\4\24\t\24\4\25\t\25\4\26\t\26\4\27\t\27\4\30\t\30\4\31" +
					"\t\31\4\32\t\32\4\33\t\33\4\34\t\34\4\35\t\35\4\36\t\36\4\37\t\37\4 \t" +
					" \4!\t!\4\"\t\"\4#\t#\4$\t$\4%\t%\3\2\3\2\3\2\3\3\3\3\3\3\3\3\3\3\3\4" +
					"\3\4\3\5\3\5\3\6\3\6\3\7\3\7\3\b\3\b\3\t\3\t\3\n\3\n\3\n\3\13\3\13\3\13" +
					"\3\13\3\13\3\13\3\f\3\f\3\f\3\f\3\f\3\f\3\f\3\f\3\r\3\r\3\16\3\16\3\16" +
					"\3\16\7\16w\n\16\f\16\16\16z\13\16\3\16\3\16\3\17\3\17\5\17\u0080\n\17" +
					"\3\17\3\17\3\17\3\17\3\17\7\17\u0087\n\17\f\17\16\17\u008a\13\17\3\20" +
					"\6\20\u008d\n\20\r\20\16\20\u008e\3\20\7\20\u0092\n\20\f\20\16\20\u0095" +
					"\13\20\3\20\3\20\3\21\6\21\u009a\n\21\r\21\16\21\u009b\3\21\5\21\u009f" +
					"\n\21\3\21\3\21\3\22\3\22\3\23\5\23\u00a6\n\23\3\23\3\23\5\23\u00aa\n" +
					"\23\3\24\3\24\3\24\3\24\3\24\3\24\3\24\3\25\3\25\3\25\3\25\3\25\3\25\3" +
					"\25\3\26\3\26\3\27\3\27\3\27\3\27\3\27\3\27\3\27\3\27\3\27\3\27\3\27\3" +
					"\27\3\27\3\27\3\30\3\30\3\30\3\30\3\30\3\30\3\30\3\30\3\30\3\30\3\30\3" +
					"\30\3\31\3\31\3\31\3\31\3\31\3\31\3\31\3\31\3\31\3\31\3\31\3\31\3\31\3" +
					"\31\3\31\3\31\3\31\3\31\3\31\3\32\3\32\3\32\3\32\3\32\3\32\3\32\3\32\3" +
					"\32\3\32\3\32\3\32\3\32\3\32\3\32\3\32\3\32\3\33\3\33\3\34\3\34\3\35\3" +
					"\35\3\36\3\36\3\37\3\37\3 \3 \3!\3!\3\"\3\"\3#\3#\3$\3$\3%\3%\2\2&\3\3" +
					"\5\4\7\5\t\6\13\7\r\b\17\t\21\n\23\13\25\f\27\r\31\16\33\17\35\20\37\21" +
					"!\22#\23%\24\'\25)\26+\27-\30/\31\61\32\63\33\65\2\67\29\2;\2=\2?\2A\2" +
					"C\2E\2G\2I\2\3\2\7\4\2$$^^\4\2\13\13\"\"\4\2\u00b2\u00b2\u00bc\u00bc\3" +
					"\2\62;\20\2C\\c|\u00c3\u00c3\u00cb\u00cb\u00cf\u00cf\u00d3\u00d3\u00d5" +
					"\u00d5\u00dc\u00dc\u00e3\u00e3\u00eb\u00eb\u00ef\u00ef\u00f3\u00f3\u00f5" +
					"\u00f5\u00fc\u00fc\2\u0111\2\3\3\2\2\2\2\5\3\2\2\2\2\7\3\2\2\2\2\t\3\2" +
					"\2\2\2\13\3\2\2\2\2\r\3\2\2\2\2\17\3\2\2\2\2\21\3\2\2\2\2\23\3\2\2\2\2" +
					"\25\3\2\2\2\2\27\3\2\2\2\2\31\3\2\2\2\2\33\3\2\2\2\2\35\3\2\2\2\2\37\3" +
					"\2\2\2\2!\3\2\2\2\2#\3\2\2\2\2%\3\2\2\2\2\'\3\2\2\2\2)\3\2\2\2\2+\3\2" +
					"\2\2\2-\3\2\2\2\2/\3\2\2\2\2\61\3\2\2\2\2\63\3\2\2\2\3K\3\2\2\2\5N\3\2" +
					"\2\2\7S\3\2\2\2\tU\3\2\2\2\13W\3\2\2\2\rY\3\2\2\2\17[\3\2\2\2\21]\3\2" +
					"\2\2\23_\3\2\2\2\25b\3\2\2\2\27h\3\2\2\2\31p\3\2\2\2\33r\3\2\2\2\35\177" +
					"\3\2\2\2\37\u008c\3\2\2\2!\u0099\3\2\2\2#\u00a2\3\2\2\2%\u00a9\3\2\2\2" +
					"\'\u00ab\3\2\2\2)\u00b2\3\2\2\2+\u00b9\3\2\2\2-\u00bb\3\2\2\2/\u00c9\3" +
					"\2\2\2\61\u00d5\3\2\2\2\63\u00e8\3\2\2\2\65\u00f9\3\2\2\2\67\u00fb\3\2" +
					"\2\29\u00fd\3\2\2\2;\u00ff\3\2\2\2=\u0101\3\2\2\2?\u0103\3\2\2\2A\u0105" +
					"\3\2\2\2C\u0107\3\2\2\2E\u0109\3\2\2\2G\u010b\3\2\2\2I\u010d\3\2\2\2K" +
					"L\7k\2\2LM\7p\2\2M\4\3\2\2\2NO\7y\2\2OP\7k\2\2PQ\7v\2\2QR\7j\2\2R\6\3" +
					"\2\2\2ST\7*\2\2T\b\3\2\2\2UV\7+\2\2V\n\3\2\2\2WX\7.\2\2X\f\3\2\2\2YZ\7" +
					"/\2\2Z\16\3\2\2\2[\\\7-\2\2\\\20\3\2\2\2]^\7,\2\2^\22\3\2\2\2_`\7c\2\2" +
					"`a\7u\2\2a\24\3\2\2\2bc\7t\2\2cd\7g\2\2de\7i\2\2ef\7g\2\2fg\7z\2\2g\26" +
					"\3\2\2\2hi\7v\2\2ij\7k\2\2jk\7o\2\2kl\7g\2\2lm\7v\2\2mn\7c\2\2no\7i\2" +
					"\2o\30\3\2\2\2pq\7<\2\2q\32\3\2\2\2rx\7$\2\2sw\n\2\2\2tu\7^\2\2uw\t\2" +
					"\2\2vs\3\2\2\2vt\3\2\2\2wz\3\2\2\2xv\3\2\2\2xy\3\2\2\2y{\3\2\2\2zx\3\2" +
					"\2\2{|\7$\2\2|\34\3\2\2\2}\u0080\5I%\2~\u0080\5C\"\2\177}\3\2\2\2\177" +
					"~\3\2\2\2\u0080\u0088\3\2\2\2\u0081\u0087\5G$\2\u0082\u0087\5I%\2\u0083" +
					"\u0087\5A!\2\u0084\u0087\5C\"\2\u0085\u0087\5E#\2\u0086\u0081\3\2\2\2" +
					"\u0086\u0082\3\2\2\2\u0086\u0083\3\2\2\2\u0086\u0084\3\2\2\2\u0086\u0085" +
					"\3\2\2\2\u0087\u008a\3\2\2\2\u0088\u0086\3\2\2\2\u0088\u0089\3\2\2\2\u0089" +
					"\36\3\2\2\2\u008a\u0088\3\2\2\2\u008b\u008d\5%\23\2\u008c\u008b\3\2\2" +
					"\2\u008d\u008e\3\2\2\2\u008e\u008c\3\2\2\2\u008e\u008f\3\2\2\2\u008f\u0093" +
					"\3\2\2\2\u0090\u0092\5#\22\2\u0091\u0090\3\2\2\2\u0092\u0095\3\2\2\2\u0093" +
					"\u0091\3\2\2\2\u0093\u0094\3\2\2\2\u0094\u0096\3\2\2\2\u0095\u0093\3\2" +
					"\2\2\u0096\u0097\b\20\2\2\u0097 \3\2\2\2\u0098\u009a\5#\22\2\u0099\u0098" +
					"\3\2\2\2\u009a\u009b\3\2\2\2\u009b\u0099\3\2\2\2\u009b\u009c\3\2\2\2\u009c" +
					"\u009e\3\2\2\2\u009d\u009f\7\2\2\3\u009e\u009d\3\2\2\2\u009e\u009f\3\2" +
					"\2\2\u009f\u00a0\3\2\2\2\u00a0\u00a1\b\21\3\2\u00a1\"\3\2\2\2\u00a2\u00a3" +
					"\t\3\2\2\u00a3$\3\2\2\2\u00a4\u00a6\7\17\2\2\u00a5\u00a4\3\2\2\2\u00a5" +
					"\u00a6\3\2\2\2\u00a6\u00a7\3\2\2\2\u00a7\u00aa\7\f\2\2\u00a8\u00aa\7\17" +
					"\2\2\u00a9\u00a5\3\2\2\2\u00a9\u00a8\3\2\2\2\u00aa&\3\2\2\2\u00ab\u00ac" +
					"\7k\2\2\u00ac\u00ad\7p\2\2\u00ad\u00ae\7f\2\2\u00ae\u00af\7g\2\2\u00af" +
					"\u00b0\7p\2\2\u00b0\u00b1\7v\2\2\u00b1(\3\2\2\2\u00b2\u00b3\7f\2\2\u00b3" +
					"\u00b4\7g\2\2\u00b4\u00b5\7f\2\2\u00b5\u00b6\7g\2\2\u00b6\u00b7\7p\2\2" +
					"\u00b7\u00b8\7v\2\2\u00b8*\3\2\2\2\u00b9\u00ba\13\2\2\2\u00ba,\3\2\2\2" +
					"\u00bb\u00bc\7\'\2\2\u00bc\u00bd\7S\2\2\u00bd\u00be\7W\2\2\u00be\u00bf" +
					"\7Q\2\2\u00bf\u00c0\7V\2\2\u00c0\u00c1\7G\2\2\u00c1\u00c2\7a\2\2\u00c2" +
					"\u00c3\7D\2\2\u00c3\u00c4\7G\2\2\u00c4\u00c5\7I\2\2\u00c5\u00c6\7K\2\2" +
					"\u00c6\u00c7\7P\2\2\u00c7\u00c8\7\'\2\2\u00c8.\3\2\2\2\u00c9\u00ca\7\'" +
					"\2\2\u00ca\u00cb\7S\2\2\u00cb\u00cc\7W\2\2\u00cc\u00cd\7Q\2\2\u00cd\u00ce" +
					"\7V\2\2\u00ce\u00cf\7G\2\2\u00cf\u00d0\7a\2\2\u00d0\u00d1\7G\2\2\u00d1" +
					"\u00d2\7P\2\2\u00d2\u00d3\7F\2\2\u00d3\u00d4\7\'\2\2\u00d4\60\3\2\2\2" +
					"\u00d5\u00d6\7\'\2\2\u00d6\u00d7\7G\2\2\u00d7\u00d8\7Z\2\2\u00d8\u00d9" +
					"\7R\2\2\u00d9\u00da\7T\2\2\u00da\u00db\7G\2\2\u00db\u00dc\7U\2\2\u00dc" +
					"\u00dd\7U\2\2\u00dd\u00de\7K\2\2\u00de\u00df\7Q\2\2\u00df\u00e0\7P\2\2" +
					"\u00e0\u00e1\7a\2\2\u00e1\u00e2\7D\2\2\u00e2\u00e3\7G\2\2\u00e3\u00e4" +
					"\7I\2\2\u00e4\u00e5\7K\2\2\u00e5\u00e6\7P\2\2\u00e6\u00e7\7\'\2\2\u00e7" +
					"\62\3\2\2\2\u00e8\u00e9\7\'\2\2\u00e9\u00ea\7G\2\2\u00ea\u00eb\7Z\2\2" +
					"\u00eb\u00ec\7R\2\2\u00ec\u00ed\7T\2\2\u00ed\u00ee\7G\2\2\u00ee\u00ef" +
					"\7U\2\2\u00ef\u00f0\7U\2\2\u00f0\u00f1\7K\2\2\u00f1\u00f2\7Q\2\2\u00f2" +
					"\u00f3\7P\2\2\u00f3\u00f4\7a\2\2\u00f4\u00f5\7G\2\2\u00f5\u00f6\7P\2\2" +
					"\u00f6\u00f7\7F\2\2\u00f7\u00f8\7\'\2\2\u00f8\64\3\2\2\2\u00f9\u00fa\7" +
					"&\2\2\u00fa\66\3\2\2\2\u00fb\u00fc\7\u20ae\2\2\u00fc8\3\2\2\2\u00fd\u00fe" +
					"\7\'\2\2\u00fe:\3\2\2\2\u00ff\u0100\t\4\2\2\u0100<\3\2\2\2\u0101\u0102" +
					"\7\u00b9\2\2\u0102>\3\2\2\2\u0103\u0104\7\61\2\2\u0104@\3\2\2\2\u0105" +
					"\u0106\7/\2\2\u0106B\3\2\2\2\u0107\u0108\7a\2\2\u0108D\3\2\2\2\u0109\u010a" +
					"\7\60\2\2\u010aF\3\2\2\2\u010b\u010c\t\5\2\2\u010cH\3\2\2\2\u010d\u010e" +
					"\t\6\2\2\u010eJ\3\2\2\2\16\2vx\177\u0086\u0088\u008e\u0093\u009b\u009e" +
					"\u00a5\u00a9\4\3\20\2\2\3\2";
	public static final ATN _ATN =
			new ATNDeserializer().deserialize(_serializedATN.toCharArray());

	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}