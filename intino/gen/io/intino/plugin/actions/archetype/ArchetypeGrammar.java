// Generated from /Users/oroncal/workspace/intino/intino/src/io/intino/plugin/actions/archetype/ArchetypeGrammar.g4 by ANTLR 4.7.2
package io.intino.plugin.actions.archetype;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.ATN;
import org.antlr.v4.runtime.atn.ATNDeserializer;
import org.antlr.v4.runtime.atn.ParserATNSimulator;
import org.antlr.v4.runtime.atn.PredictionContextCache;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.tree.ParseTreeListener;
import org.antlr.v4.runtime.tree.ParseTreeVisitor;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.List;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class ArchetypeGrammar extends Parser {
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
	public static final int
			RULE_root = 0, RULE_node = 1, RULE_declaration = 2, RULE_starting = 3,
			RULE_parameters = 4, RULE_parameter = 5, RULE_body = 6, RULE_type = 7;

	private static String[] makeRuleNames() {
		return new String[]{
				"root", "node", "declaration", "starting", "parameters", "parameter",
				"body", "type"
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

	@Override
	public String getGrammarFileName() {
		return "ArchetypeGrammar.g4";
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
	public ATN getATN() {
		return _ATN;
	}

	public ArchetypeGrammar(TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this, _ATN, _decisionToDFA, _sharedContextCache);
	}

	public static class RootContext extends ParserRuleContext {
		public TerminalNode EOF() {
			return getToken(ArchetypeGrammar.EOF, 0);
		}

		public List<TerminalNode> NEWLINE() {
			return getTokens(ArchetypeGrammar.NEWLINE);
		}

		public TerminalNode NEWLINE(int i) {
			return getToken(ArchetypeGrammar.NEWLINE, i);
		}

		public List<NodeContext> node() {
			return getRuleContexts(NodeContext.class);
		}

		public NodeContext node(int i) {
			return getRuleContext(NodeContext.class, i);
		}

		public RootContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}

		@Override
		public int getRuleIndex() {
			return RULE_root;
		}

		@Override
		public void enterRule(ParseTreeListener listener) {
			if (listener instanceof ArchetypeGrammarListener) ((ArchetypeGrammarListener) listener).enterRoot(this);
		}

		@Override
		public void exitRule(ParseTreeListener listener) {
			if (listener instanceof ArchetypeGrammarListener) ((ArchetypeGrammarListener) listener).exitRoot(this);
		}

		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if (visitor instanceof ArchetypeGrammarVisitor)
				return ((ArchetypeGrammarVisitor<? extends T>) visitor).visitRoot(this);
			else return visitor.visitChildren(this);
		}
	}

	public final RootContext root() throws RecognitionException {
		RootContext _localctx = new RootContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_root);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
				setState(19);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la == NEWLINE) {
					{
						{
							setState(16);
							match(NEWLINE);
						}
					}
					setState(21);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(31);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << MINUS) | (1L << PLUS) | (1L << STAR))) != 0)) {
					{
						{
							setState(22);
							node();
							setState(26);
							_errHandler.sync(this);
							_la = _input.LA(1);
							while (_la == NEWLINE) {
								{
									{
										setState(23);
										match(NEWLINE);
									}
								}
								setState(28);
								_errHandler.sync(this);
								_la = _input.LA(1);
							}
						}
					}
					setState(33);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(34);
				match(EOF);
			}
		} catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		} finally {
			exitRule();
		}
		return _localctx;
	}

	public static class NodeContext extends ParserRuleContext {
		public DeclarationContext declaration() {
			return getRuleContext(DeclarationContext.class, 0);
		}

		public BodyContext body() {
			return getRuleContext(BodyContext.class, 0);
		}

		public NodeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}

		@Override
		public int getRuleIndex() {
			return RULE_node;
		}

		@Override
		public void enterRule(ParseTreeListener listener) {
			if (listener instanceof ArchetypeGrammarListener) ((ArchetypeGrammarListener) listener).enterNode(this);
		}

		@Override
		public void exitRule(ParseTreeListener listener) {
			if (listener instanceof ArchetypeGrammarListener) ((ArchetypeGrammarListener) listener).exitNode(this);
		}

		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if (visitor instanceof ArchetypeGrammarVisitor)
				return ((ArchetypeGrammarVisitor<? extends T>) visitor).visitNode(this);
			else return visitor.visitChildren(this);
		}
	}

	public final NodeContext node() throws RecognitionException {
		NodeContext _localctx = new NodeContext(_ctx, getState());
		enterRule(_localctx, 2, RULE_node);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
				setState(36);
				declaration();
				setState(38);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la == NEW_LINE_INDENT) {
					{
						setState(37);
						body();
					}
				}

			}
		} catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		} finally {
			exitRule();
		}
		return _localctx;
	}

	public static class DeclarationContext extends ParserRuleContext {
		public StartingContext starting() {
			return getRuleContext(StartingContext.class, 0);
		}

		public TerminalNode IDENTIFIER() {
			return getToken(ArchetypeGrammar.IDENTIFIER, 0);
		}

		public ParametersContext parameters() {
			return getRuleContext(ParametersContext.class, 0);
		}

		public TerminalNode IN() {
			return getToken(ArchetypeGrammar.IN, 0);
		}

		public List<TerminalNode> LABEL() {
			return getTokens(ArchetypeGrammar.LABEL);
		}

		public TerminalNode LABEL(int i) {
			return getToken(ArchetypeGrammar.LABEL, i);
		}

		public TerminalNode WITH() {
			return getToken(ArchetypeGrammar.WITH, 0);
		}

		public TypeContext type() {
			return getRuleContext(TypeContext.class, 0);
		}

		public DeclarationContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}

		@Override
		public int getRuleIndex() {
			return RULE_declaration;
		}

		@Override
		public void enterRule(ParseTreeListener listener) {
			if (listener instanceof ArchetypeGrammarListener)
				((ArchetypeGrammarListener) listener).enterDeclaration(this);
		}

		@Override
		public void exitRule(ParseTreeListener listener) {
			if (listener instanceof ArchetypeGrammarListener)
				((ArchetypeGrammarListener) listener).exitDeclaration(this);
		}

		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if (visitor instanceof ArchetypeGrammarVisitor)
				return ((ArchetypeGrammarVisitor<? extends T>) visitor).visitDeclaration(this);
			else return visitor.visitChildren(this);
		}
	}

	public final DeclarationContext declaration() throws RecognitionException {
		DeclarationContext _localctx = new DeclarationContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_declaration);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
				setState(40);
				starting();
				setState(41);
				match(IDENTIFIER);
				setState(43);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la == LEFT_PARENTHESIS) {
					{
						setState(42);
						parameters();
					}
				}

				setState(47);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la == IN) {
					{
						setState(45);
						match(IN);
						setState(46);
						match(LABEL);
					}
				}

				setState(54);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la == WITH) {
					{
						setState(49);
						match(WITH);
						setState(50);
						match(LABEL);
						setState(52);
						_errHandler.sync(this);
						_la = _input.LA(1);
						if (_la == AS) {
							{
								setState(51);
								type();
							}
						}

					}
				}

			}
		} catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		} finally {
			exitRule();
		}
		return _localctx;
	}

	public static class StartingContext extends ParserRuleContext {
		public TerminalNode MINUS() {
			return getToken(ArchetypeGrammar.MINUS, 0);
		}

		public TerminalNode PLUS() {
			return getToken(ArchetypeGrammar.PLUS, 0);
		}

		public TerminalNode STAR() {
			return getToken(ArchetypeGrammar.STAR, 0);
		}

		public StartingContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}

		@Override
		public int getRuleIndex() {
			return RULE_starting;
		}

		@Override
		public void enterRule(ParseTreeListener listener) {
			if (listener instanceof ArchetypeGrammarListener) ((ArchetypeGrammarListener) listener).enterStarting(this);
		}

		@Override
		public void exitRule(ParseTreeListener listener) {
			if (listener instanceof ArchetypeGrammarListener) ((ArchetypeGrammarListener) listener).exitStarting(this);
		}

		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if (visitor instanceof ArchetypeGrammarVisitor)
				return ((ArchetypeGrammarVisitor<? extends T>) visitor).visitStarting(this);
			else return visitor.visitChildren(this);
		}
	}

	public final StartingContext starting() throws RecognitionException {
		StartingContext _localctx = new StartingContext(_ctx, getState());
		enterRule(_localctx, 6, RULE_starting);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
				setState(56);
				_la = _input.LA(1);
				if (!((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << MINUS) | (1L << PLUS) | (1L << STAR))) != 0))) {
					_errHandler.recoverInline(this);
				} else {
					if (_input.LA(1) == Token.EOF) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
			}
		} catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		} finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ParametersContext extends ParserRuleContext {
		public TerminalNode LEFT_PARENTHESIS() {
			return getToken(ArchetypeGrammar.LEFT_PARENTHESIS, 0);
		}

		public TerminalNode RIGHT_PARENTHESIS() {
			return getToken(ArchetypeGrammar.RIGHT_PARENTHESIS, 0);
		}

		public List<ParameterContext> parameter() {
			return getRuleContexts(ParameterContext.class);
		}

		public ParameterContext parameter(int i) {
			return getRuleContext(ParameterContext.class, i);
		}

		public List<TerminalNode> COMMA() {
			return getTokens(ArchetypeGrammar.COMMA);
		}

		public TerminalNode COMMA(int i) {
			return getToken(ArchetypeGrammar.COMMA, i);
		}

		public ParametersContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}

		@Override
		public int getRuleIndex() {
			return RULE_parameters;
		}

		@Override
		public void enterRule(ParseTreeListener listener) {
			if (listener instanceof ArchetypeGrammarListener)
				((ArchetypeGrammarListener) listener).enterParameters(this);
		}

		@Override
		public void exitRule(ParseTreeListener listener) {
			if (listener instanceof ArchetypeGrammarListener)
				((ArchetypeGrammarListener) listener).exitParameters(this);
		}

		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if (visitor instanceof ArchetypeGrammarVisitor)
				return ((ArchetypeGrammarVisitor<? extends T>) visitor).visitParameters(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ParametersContext parameters() throws RecognitionException {
		ParametersContext _localctx = new ParametersContext(_ctx, getState());
		enterRule(_localctx, 8, RULE_parameters);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
				setState(58);
				match(LEFT_PARENTHESIS);
				setState(67);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la == IDENTIFIER) {
					{
						setState(59);
						parameter();
						setState(64);
						_errHandler.sync(this);
						_la = _input.LA(1);
						while (_la == COMMA) {
							{
								{
									setState(60);
									match(COMMA);
									setState(61);
									parameter();
								}
							}
							setState(66);
							_errHandler.sync(this);
							_la = _input.LA(1);
						}
					}
				}

				setState(69);
				match(RIGHT_PARENTHESIS);
			}
		} catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		} finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ParameterContext extends ParserRuleContext {
		public TerminalNode IDENTIFIER() {
			return getToken(ArchetypeGrammar.IDENTIFIER, 0);
		}

		public TypeContext type() {
			return getRuleContext(TypeContext.class, 0);
		}

		public ParameterContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}

		@Override
		public int getRuleIndex() {
			return RULE_parameter;
		}

		@Override
		public void enterRule(ParseTreeListener listener) {
			if (listener instanceof ArchetypeGrammarListener)
				((ArchetypeGrammarListener) listener).enterParameter(this);
		}

		@Override
		public void exitRule(ParseTreeListener listener) {
			if (listener instanceof ArchetypeGrammarListener) ((ArchetypeGrammarListener) listener).exitParameter(this);
		}

		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if (visitor instanceof ArchetypeGrammarVisitor)
				return ((ArchetypeGrammarVisitor<? extends T>) visitor).visitParameter(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ParameterContext parameter() throws RecognitionException {
		ParameterContext _localctx = new ParameterContext(_ctx, getState());
		enterRule(_localctx, 10, RULE_parameter);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
				setState(71);
				match(IDENTIFIER);
				setState(73);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la == AS) {
					{
						setState(72);
						type();
					}
				}

			}
		} catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		} finally {
			exitRule();
		}
		return _localctx;
	}

	public static class BodyContext extends ParserRuleContext {
		public TerminalNode NEW_LINE_INDENT() {
			return getToken(ArchetypeGrammar.NEW_LINE_INDENT, 0);
		}

		public TerminalNode DEDENT() {
			return getToken(ArchetypeGrammar.DEDENT, 0);
		}

		public List<NodeContext> node() {
			return getRuleContexts(NodeContext.class);
		}

		public NodeContext node(int i) {
			return getRuleContext(NodeContext.class, i);
		}

		public List<TerminalNode> NEWLINE() {
			return getTokens(ArchetypeGrammar.NEWLINE);
		}

		public TerminalNode NEWLINE(int i) {
			return getToken(ArchetypeGrammar.NEWLINE, i);
		}

		public BodyContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}

		@Override
		public int getRuleIndex() {
			return RULE_body;
		}

		@Override
		public void enterRule(ParseTreeListener listener) {
			if (listener instanceof ArchetypeGrammarListener) ((ArchetypeGrammarListener) listener).enterBody(this);
		}

		@Override
		public void exitRule(ParseTreeListener listener) {
			if (listener instanceof ArchetypeGrammarListener) ((ArchetypeGrammarListener) listener).exitBody(this);
		}

		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if (visitor instanceof ArchetypeGrammarVisitor)
				return ((ArchetypeGrammarVisitor<? extends T>) visitor).visitBody(this);
			else return visitor.visitChildren(this);
		}
	}

	public final BodyContext body() throws RecognitionException {
		BodyContext _localctx = new BodyContext(_ctx, getState());
		enterRule(_localctx, 12, RULE_body);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
				setState(75);
				match(NEW_LINE_INDENT);
				setState(82);
				_errHandler.sync(this);
				_la = _input.LA(1);
				do {
					{
						{
							setState(76);
							node();
							setState(78);
							_errHandler.sync(this);
							_la = _input.LA(1);
							do {
								{
									{
										setState(77);
										match(NEWLINE);
									}
								}
								setState(80);
								_errHandler.sync(this);
								_la = _input.LA(1);
							} while (_la == NEWLINE);
						}
					}
					setState(84);
					_errHandler.sync(this);
					_la = _input.LA(1);
				} while ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << MINUS) | (1L << PLUS) | (1L << STAR))) != 0));
				setState(86);
				match(DEDENT);
			}
		} catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		} finally {
			exitRule();
		}
		return _localctx;
	}

	public static class TypeContext extends ParserRuleContext {
		public TerminalNode AS() {
			return getToken(ArchetypeGrammar.AS, 0);
		}

		public TerminalNode REGEX() {
			return getToken(ArchetypeGrammar.REGEX, 0);
		}

		public TerminalNode TIMETAG() {
			return getToken(ArchetypeGrammar.TIMETAG, 0);
		}

		public TerminalNode COLON() {
			return getToken(ArchetypeGrammar.COLON, 0);
		}

		public TerminalNode IDENTIFIER() {
			return getToken(ArchetypeGrammar.IDENTIFIER, 0);
		}

		public TypeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}

		@Override
		public int getRuleIndex() {
			return RULE_type;
		}

		@Override
		public void enterRule(ParseTreeListener listener) {
			if (listener instanceof ArchetypeGrammarListener) ((ArchetypeGrammarListener) listener).enterType(this);
		}

		@Override
		public void exitRule(ParseTreeListener listener) {
			if (listener instanceof ArchetypeGrammarListener) ((ArchetypeGrammarListener) listener).exitType(this);
		}

		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if (visitor instanceof ArchetypeGrammarVisitor)
				return ((ArchetypeGrammarVisitor<? extends T>) visitor).visitType(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TypeContext type() throws RecognitionException {
		TypeContext _localctx = new TypeContext(_ctx, getState());
		enterRule(_localctx, 14, RULE_type);
		try {
			enterOuterAlt(_localctx, 1);
			{
				setState(88);
				match(AS);
				setState(93);
				_errHandler.sync(this);
				switch (_input.LA(1)) {
					case REGEX: {
						setState(89);
						match(REGEX);
					}
					break;
					case TIMETAG: {
						{
							setState(90);
							match(TIMETAG);
							setState(91);
							match(COLON);
							setState(92);
							match(IDENTIFIER);
						}
					}
					break;
					default:
						throw new NoViableAltException(this);
				}
			}
		} catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		} finally {
			exitRule();
		}
		return _localctx;
	}

	public static final String _serializedATN =
			"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\3\33b\4\2\t\2\4\3\t" +
					"\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\3\2\7\2\24\n\2\f\2" +
					"\16\2\27\13\2\3\2\3\2\7\2\33\n\2\f\2\16\2\36\13\2\7\2 \n\2\f\2\16\2#\13" +
					"\2\3\2\3\2\3\3\3\3\5\3)\n\3\3\4\3\4\3\4\5\4.\n\4\3\4\3\4\5\4\62\n\4\3" +
					"\4\3\4\3\4\5\4\67\n\4\5\49\n\4\3\5\3\5\3\6\3\6\3\6\3\6\7\6A\n\6\f\6\16" +
					"\6D\13\6\5\6F\n\6\3\6\3\6\3\7\3\7\5\7L\n\7\3\b\3\b\3\b\6\bQ\n\b\r\b\16" +
					"\bR\6\bU\n\b\r\b\16\bV\3\b\3\b\3\t\3\t\3\t\3\t\3\t\5\t`\n\t\3\t\2\2\n" +
					"\2\4\6\b\n\f\16\20\2\3\3\2\b\n\2g\2\25\3\2\2\2\4&\3\2\2\2\6*\3\2\2\2\b" +
					":\3\2\2\2\n<\3\2\2\2\fI\3\2\2\2\16M\3\2\2\2\20Z\3\2\2\2\22\24\7\21\2\2" +
					"\23\22\3\2\2\2\24\27\3\2\2\2\25\23\3\2\2\2\25\26\3\2\2\2\26!\3\2\2\2\27" +
					"\25\3\2\2\2\30\34\5\4\3\2\31\33\7\21\2\2\32\31\3\2\2\2\33\36\3\2\2\2\34" +
					"\32\3\2\2\2\34\35\3\2\2\2\35 \3\2\2\2\36\34\3\2\2\2\37\30\3\2\2\2 #\3" +
					"\2\2\2!\37\3\2\2\2!\"\3\2\2\2\"$\3\2\2\2#!\3\2\2\2$%\7\2\2\3%\3\3\2\2" +
					"\2&(\5\6\4\2\')\5\16\b\2(\'\3\2\2\2()\3\2\2\2)\5\3\2\2\2*+\5\b\5\2+-\7" +
					"\20\2\2,.\5\n\6\2-,\3\2\2\2-.\3\2\2\2.\61\3\2\2\2/\60\7\3\2\2\60\62\7" +
					"\17\2\2\61/\3\2\2\2\61\62\3\2\2\2\628\3\2\2\2\63\64\7\4\2\2\64\66\7\17" +
					"\2\2\65\67\5\20\t\2\66\65\3\2\2\2\66\67\3\2\2\2\679\3\2\2\28\63\3\2\2" +
					"\289\3\2\2\29\7\3\2\2\2:;\t\2\2\2;\t\3\2\2\2<E\7\5\2\2=B\5\f\7\2>?\7\7" +
					"\2\2?A\5\f\7\2@>\3\2\2\2AD\3\2\2\2B@\3\2\2\2BC\3\2\2\2CF\3\2\2\2DB\3\2" +
					"\2\2E=\3\2\2\2EF\3\2\2\2FG\3\2\2\2GH\7\6\2\2H\13\3\2\2\2IK\7\20\2\2JL" +
					"\5\20\t\2KJ\3\2\2\2KL\3\2\2\2L\r\3\2\2\2MT\7\25\2\2NP\5\4\3\2OQ\7\21\2" +
					"\2PO\3\2\2\2QR\3\2\2\2RP\3\2\2\2RS\3\2\2\2SU\3\2\2\2TN\3\2\2\2UV\3\2\2" +
					"\2VT\3\2\2\2VW\3\2\2\2WX\3\2\2\2XY\7\26\2\2Y\17\3\2\2\2Z_\7\13\2\2[`\7" +
					"\f\2\2\\]\7\r\2\2]^\7\16\2\2^`\7\20\2\2_[\3\2\2\2_\\\3\2\2\2`\21\3\2\2" +
					"\2\20\25\34!(-\61\668BEKRV_";
	public static final ATN _ATN =
			new ATNDeserializer().deserialize(_serializedATN.toCharArray());

	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}