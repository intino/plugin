// Generated from /Users/oroncal/workspace/intino/intino/src/io/intino/plugin/actions/archetype/ArchetypeGrammar.g4 by ANTLR 4.8
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
		RuntimeMetaData.checkVersion("4.8", RuntimeMetaData.VERSION);
	}

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
			new PredictionContextCache();
	public static final int
			IN = 1, WITH = 2, SPLITTED = 3, LEFT_PARENTHESIS = 4, LEFT_SQUARE = 5, RIGHT_PARENTHESIS = 6,
			RIGHT_SQUARE = 7, COMMA = 8, MINUS = 9, PLUS = 10, STAR = 11, AS = 12, REGEX = 13, TIMETAG = 14,
			COLON = 15, LINE_COMMENT = 16, LABEL = 17, IDENTIFIER = 18, NEWLINE = 19, SPACES = 20,
			SP = 21, NL = 22, NEW_LINE_INDENT = 23, DEDENT = 24, UNKNOWN_TOKEN = 25, QUOTE_BEGIN = 26,
			QUOTE_END = 27, EXPRESSION_BEGIN = 28, EXPRESSION_END = 29;
	public static final int
			RULE_root = 0, RULE_node = 1, RULE_declaration = 2, RULE_starting = 3,
			RULE_parameters = 4, RULE_splitted = 5, RULE_parameter = 6, RULE_body = 7,
			RULE_type = 8;

	private static String[] makeRuleNames() {
		return new String[]{
				"root", "node", "declaration", "starting", "parameters", "splitted",
				"parameter", "body", "type"
		};
	}

	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[]{
				null, "'in'", "'with'", "'splitted'", "'('", "'['", "')'", "']'", "','",
				"'-'", "'+'", "'*'", "'as'", "'regex'", "'timetag'", "':'", null, null,
				null, null, null, null, null, "'indent'", "'dedent'", null, "'%QUOTE_BEGIN%'",
				"'%QUOTE_END%'", "'%EXPRESSION_BEGIN%'", "'%EXPRESSION_END%'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[]{
				null, "IN", "WITH", "SPLITTED", "LEFT_PARENTHESIS", "LEFT_SQUARE", "RIGHT_PARENTHESIS",
				"RIGHT_SQUARE", "COMMA", "MINUS", "PLUS", "STAR", "AS", "REGEX", "TIMETAG",
				"COLON", "LINE_COMMENT", "LABEL", "IDENTIFIER", "NEWLINE", "SPACES",
				"SP", "NL", "NEW_LINE_INDENT", "DEDENT", "UNKNOWN_TOKEN", "QUOTE_BEGIN",
				"QUOTE_END", "EXPRESSION_BEGIN", "EXPRESSION_END"
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
				setState(21);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la == NEWLINE) {
					{
						{
							setState(18);
							match(NEWLINE);
						}
					}
					setState(23);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(33);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << MINUS) | (1L << PLUS) | (1L << STAR))) != 0)) {
					{
						{
							setState(24);
							node();
							setState(28);
							_errHandler.sync(this);
							_la = _input.LA(1);
							while (_la == NEWLINE) {
								{
									{
										setState(25);
										match(NEWLINE);
									}
								}
								setState(30);
								_errHandler.sync(this);
								_la = _input.LA(1);
							}
						}
					}
					setState(35);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(36);
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
				setState(38);
				declaration();
				setState(40);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la == NEW_LINE_INDENT) {
					{
						setState(39);
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

		public SplittedContext splitted() {
			return getRuleContext(SplittedContext.class, 0);
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
				setState(42);
				starting();
				setState(43);
				match(IDENTIFIER);
				setState(46);
				_errHandler.sync(this);
				switch (_input.LA(1)) {
					case LEFT_PARENTHESIS: {
						setState(44);
						parameters();
					}
					break;
					case SPLITTED: {
						setState(45);
						splitted();
					}
					break;
					case EOF:
					case IN:
					case WITH:
					case MINUS:
					case PLUS:
					case STAR:
					case NEWLINE:
					case NEW_LINE_INDENT:
						break;
					default:
						break;
				}
				setState(50);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la == IN) {
					{
						setState(48);
						match(IN);
						setState(49);
						match(LABEL);
					}
				}

				setState(57);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la == WITH) {
					{
						setState(52);
						match(WITH);
						setState(53);
						match(LABEL);
						setState(55);
						_errHandler.sync(this);
						_la = _input.LA(1);
						if (_la == AS) {
							{
								setState(54);
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
				setState(59);
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
				setState(61);
				match(LEFT_PARENTHESIS);
				setState(70);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la == IDENTIFIER) {
					{
						setState(62);
						parameter();
						setState(67);
						_errHandler.sync(this);
						_la = _input.LA(1);
						while (_la == COMMA) {
							{
								{
									setState(63);
									match(COMMA);
									setState(64);
									parameter();
								}
							}
							setState(69);
							_errHandler.sync(this);
							_la = _input.LA(1);
						}
					}
				}

				setState(72);
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

	public static class SplittedContext extends ParserRuleContext {
		public TerminalNode SPLITTED() {
			return getToken(ArchetypeGrammar.SPLITTED, 0);
		}

		public TerminalNode LEFT_SQUARE() {
			return getToken(ArchetypeGrammar.LEFT_SQUARE, 0);
		}

		public TerminalNode RIGHT_SQUARE() {
			return getToken(ArchetypeGrammar.RIGHT_SQUARE, 0);
		}

		public List<TerminalNode> IDENTIFIER() {
			return getTokens(ArchetypeGrammar.IDENTIFIER);
		}

		public TerminalNode IDENTIFIER(int i) {
			return getToken(ArchetypeGrammar.IDENTIFIER, i);
		}

		public List<TerminalNode> COMMA() {
			return getTokens(ArchetypeGrammar.COMMA);
		}

		public TerminalNode COMMA(int i) {
			return getToken(ArchetypeGrammar.COMMA, i);
		}

		public SplittedContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}

		@Override
		public int getRuleIndex() {
			return RULE_splitted;
		}

		@Override
		public void enterRule(ParseTreeListener listener) {
			if (listener instanceof ArchetypeGrammarListener) ((ArchetypeGrammarListener) listener).enterSplitted(this);
		}

		@Override
		public void exitRule(ParseTreeListener listener) {
			if (listener instanceof ArchetypeGrammarListener) ((ArchetypeGrammarListener) listener).exitSplitted(this);
		}

		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if (visitor instanceof ArchetypeGrammarVisitor)
				return ((ArchetypeGrammarVisitor<? extends T>) visitor).visitSplitted(this);
			else return visitor.visitChildren(this);
		}
	}

	public final SplittedContext splitted() throws RecognitionException {
		SplittedContext _localctx = new SplittedContext(_ctx, getState());
		enterRule(_localctx, 10, RULE_splitted);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
				setState(74);
				match(SPLITTED);
				setState(75);
				match(LEFT_SQUARE);
				setState(84);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la == IDENTIFIER) {
					{
						setState(76);
						match(IDENTIFIER);
						setState(81);
						_errHandler.sync(this);
						_la = _input.LA(1);
						while (_la == COMMA) {
							{
								{
									setState(77);
									match(COMMA);
									setState(78);
									match(IDENTIFIER);
								}
							}
							setState(83);
							_errHandler.sync(this);
							_la = _input.LA(1);
						}
					}
				}

				setState(86);
				match(RIGHT_SQUARE);
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
		enterRule(_localctx, 12, RULE_parameter);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
				setState(88);
				match(IDENTIFIER);
				setState(90);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la == AS) {
					{
						setState(89);
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
		enterRule(_localctx, 14, RULE_body);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
				setState(92);
				match(NEW_LINE_INDENT);
				setState(99);
				_errHandler.sync(this);
				_la = _input.LA(1);
				do {
					{
						{
							setState(93);
							node();
							setState(95);
							_errHandler.sync(this);
							_la = _input.LA(1);
							do {
								{
									{
										setState(94);
										match(NEWLINE);
									}
								}
								setState(97);
								_errHandler.sync(this);
								_la = _input.LA(1);
							} while (_la == NEWLINE);
						}
					}
					setState(101);
					_errHandler.sync(this);
					_la = _input.LA(1);
				} while ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << MINUS) | (1L << PLUS) | (1L << STAR))) != 0));
				setState(103);
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
		enterRule(_localctx, 16, RULE_type);
		try {
			enterOuterAlt(_localctx, 1);
			{
				setState(105);
				match(AS);
				setState(110);
				_errHandler.sync(this);
				switch (_input.LA(1)) {
					case REGEX: {
						setState(106);
						match(REGEX);
					}
					break;
					case TIMETAG: {
						{
							setState(107);
							match(TIMETAG);
							setState(108);
							match(COLON);
							setState(109);
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
			"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\3\37s\4\2\t\2\4\3\t" +
					"\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\3\2\7\2\26" +
					"\n\2\f\2\16\2\31\13\2\3\2\3\2\7\2\35\n\2\f\2\16\2 \13\2\7\2\"\n\2\f\2" +
					"\16\2%\13\2\3\2\3\2\3\3\3\3\5\3+\n\3\3\4\3\4\3\4\3\4\5\4\61\n\4\3\4\3" +
					"\4\5\4\65\n\4\3\4\3\4\3\4\5\4:\n\4\5\4<\n\4\3\5\3\5\3\6\3\6\3\6\3\6\7" +
					"\6D\n\6\f\6\16\6G\13\6\5\6I\n\6\3\6\3\6\3\7\3\7\3\7\3\7\3\7\7\7R\n\7\f" +
					"\7\16\7U\13\7\5\7W\n\7\3\7\3\7\3\b\3\b\5\b]\n\b\3\t\3\t\3\t\6\tb\n\t\r" +
					"\t\16\tc\6\tf\n\t\r\t\16\tg\3\t\3\t\3\n\3\n\3\n\3\n\3\n\5\nq\n\n\3\n\2" +
					"\2\13\2\4\6\b\n\f\16\20\22\2\3\3\2\13\r\2z\2\27\3\2\2\2\4(\3\2\2\2\6," +
					"\3\2\2\2\b=\3\2\2\2\n?\3\2\2\2\fL\3\2\2\2\16Z\3\2\2\2\20^\3\2\2\2\22k" +
					"\3\2\2\2\24\26\7\25\2\2\25\24\3\2\2\2\26\31\3\2\2\2\27\25\3\2\2\2\27\30" +
					"\3\2\2\2\30#\3\2\2\2\31\27\3\2\2\2\32\36\5\4\3\2\33\35\7\25\2\2\34\33" +
					"\3\2\2\2\35 \3\2\2\2\36\34\3\2\2\2\36\37\3\2\2\2\37\"\3\2\2\2 \36\3\2" +
					"\2\2!\32\3\2\2\2\"%\3\2\2\2#!\3\2\2\2#$\3\2\2\2$&\3\2\2\2%#\3\2\2\2&\'" +
					"\7\2\2\3\'\3\3\2\2\2(*\5\6\4\2)+\5\20\t\2*)\3\2\2\2*+\3\2\2\2+\5\3\2\2" +
					"\2,-\5\b\5\2-\60\7\24\2\2.\61\5\n\6\2/\61\5\f\7\2\60.\3\2\2\2\60/\3\2" +
					"\2\2\60\61\3\2\2\2\61\64\3\2\2\2\62\63\7\3\2\2\63\65\7\23\2\2\64\62\3" +
					"\2\2\2\64\65\3\2\2\2\65;\3\2\2\2\66\67\7\4\2\2\679\7\23\2\28:\5\22\n\2" +
					"98\3\2\2\29:\3\2\2\2:<\3\2\2\2;\66\3\2\2\2;<\3\2\2\2<\7\3\2\2\2=>\t\2" +
					"\2\2>\t\3\2\2\2?H\7\6\2\2@E\5\16\b\2AB\7\n\2\2BD\5\16\b\2CA\3\2\2\2DG" +
					"\3\2\2\2EC\3\2\2\2EF\3\2\2\2FI\3\2\2\2GE\3\2\2\2H@\3\2\2\2HI\3\2\2\2I" +
					"J\3\2\2\2JK\7\b\2\2K\13\3\2\2\2LM\7\5\2\2MV\7\7\2\2NS\7\24\2\2OP\7\n\2" +
					"\2PR\7\24\2\2QO\3\2\2\2RU\3\2\2\2SQ\3\2\2\2ST\3\2\2\2TW\3\2\2\2US\3\2" +
					"\2\2VN\3\2\2\2VW\3\2\2\2WX\3\2\2\2XY\7\t\2\2Y\r\3\2\2\2Z\\\7\24\2\2[]" +
					"\5\22\n\2\\[\3\2\2\2\\]\3\2\2\2]\17\3\2\2\2^e\7\31\2\2_a\5\4\3\2`b\7\25" +
					"\2\2a`\3\2\2\2bc\3\2\2\2ca\3\2\2\2cd\3\2\2\2df\3\2\2\2e_\3\2\2\2fg\3\2" +
					"\2\2ge\3\2\2\2gh\3\2\2\2hi\3\2\2\2ij\7\32\2\2j\21\3\2\2\2kp\7\16\2\2l" +
					"q\7\17\2\2mn\7\20\2\2no\7\21\2\2oq\7\24\2\2pl\3\2\2\2pm\3\2\2\2q\23\3" +
					"\2\2\2\22\27\36#*\60\649;EHSV\\cgp";
	public static final ATN _ATN =
			new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}