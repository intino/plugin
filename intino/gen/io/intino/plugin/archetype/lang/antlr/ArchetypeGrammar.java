// Generated from /Users/oroncal/workspace/intino/intino/src/io/intino/plugin/archetype/lang/antlr/ArchetypeGrammar.g4 by ANTLR 4.8
package io.intino.plugin.archetype.lang.antlr;

import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.*;
import org.antlr.v4.runtime.tree.*;

import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class ArchetypeGrammar extends Parser {
	static {
		RuntimeMetaData.checkVersion("4.8", RuntimeMetaData.VERSION);
	}

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
			new PredictionContextCache();
	public static final int
			IN = 1, WITH = 2, SPLITTED = 3, LEFT_PARENTHESIS = 4, RIGHT_PARENTHESIS = 5, LEFT_SQUARE = 6,
			RIGHT_SQUARE = 7, COMMA = 8, MINUS = 9, PLUS = 10, STAR = 11, AS = 12, REGEX = 13, OWNER = 14,
			CONSUMER = 15, TIMETAG = 16, COLON = 17, LINE_COMMENT = 18, LABEL = 19, IDENTIFIER = 20,
			NEWLINE = 21, SPACES = 22, SP = 23, NL = 24, NEW_LINE_INDENT = 25, DEDENT = 26, UNKNOWN_TOKEN = 27,
			QUOTE_BEGIN = 28, QUOTE_END = 29, EXPRESSION_BEGIN = 30, EXPRESSION_END = 31;
	public static final int
			RULE_root = 0, RULE_node = 1, RULE_declaration = 2, RULE_starting = 3,
			RULE_parameters = 4, RULE_splitted = 5, RULE_parameter = 6, RULE_body = 7,
			RULE_type = 8, RULE_ownerAndConsumer = 9, RULE_uses = 10;

	private static String[] makeRuleNames() {
		return new String[]{
				"root", "node", "declaration", "starting", "parameters", "splitted",
				"parameter", "body", "type", "ownerAndConsumer", "uses"
		};
	}

	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[]{
				null, "'in'", "'with'", "'splitted'", "'('", "')'", "'['", "']'", "','",
				"'-'", "'+'", "'*'", "'as'", "'regex'", "'owner'", "'consumer'", "'timetag'",
				"':'", null, null, null, null, null, null, null, "'indent'", "'dedent'",
				null, "'%QUOTE_BEGIN%'", "'%QUOTE_END%'", "'%EXPRESSION_BEGIN%'", "'%EXPRESSION_END%'"
		};
	}

	private static final String[] _LITERAL_NAMES = makeLiteralNames();

	private static String[] makeSymbolicNames() {
		return new String[]{
				null, "IN", "WITH", "SPLITTED", "LEFT_PARENTHESIS", "RIGHT_PARENTHESIS",
				"LEFT_SQUARE", "RIGHT_SQUARE", "COMMA", "MINUS", "PLUS", "STAR", "AS",
				"REGEX", "OWNER", "CONSUMER", "TIMETAG", "COLON", "LINE_COMMENT", "LABEL",
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
				setState(25);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la == NEWLINE) {
					{
						{
							setState(22);
							match(NEWLINE);
						}
					}
					setState(27);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(37);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << MINUS) | (1L << PLUS) | (1L << STAR))) != 0)) {
					{
						{
							setState(28);
							node();
							setState(32);
							_errHandler.sync(this);
							_la = _input.LA(1);
							while (_la == NEWLINE) {
								{
									{
										setState(29);
										match(NEWLINE);
									}
								}
								setState(34);
								_errHandler.sync(this);
								_la = _input.LA(1);
							}
						}
					}
					setState(39);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(40);
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
				setState(42);
				declaration();
				setState(44);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la == NEW_LINE_INDENT) {
					{
						setState(43);
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

		public OwnerAndConsumerContext ownerAndConsumer() {
			return getRuleContext(OwnerAndConsumerContext.class, 0);
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
				setState(46);
				starting();
				setState(47);
				match(IDENTIFIER);
				setState(50);
				_errHandler.sync(this);
				switch (_input.LA(1)) {
					case LEFT_PARENTHESIS: {
						setState(48);
						parameters();
					}
					break;
					case SPLITTED: {
						setState(49);
						splitted();
					}
					break;
					case EOF:
					case IN:
					case WITH:
					case MINUS:
					case PLUS:
					case STAR:
					case OWNER:
					case CONSUMER:
					case NEWLINE:
					case NEW_LINE_INDENT:
						break;
					default:
						break;
				}
				setState(54);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la == IN) {
					{
						setState(52);
						match(IN);
						setState(53);
						match(LABEL);
					}
				}

				setState(61);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la == WITH) {
					{
						setState(56);
						match(WITH);
						setState(57);
						match(LABEL);
						setState(59);
						_errHandler.sync(this);
						_la = _input.LA(1);
						if (_la == AS) {
							{
								setState(58);
								type();
							}
						}

					}
				}

				setState(63);
				ownerAndConsumer();
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
				setState(65);
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
				setState(67);
				match(LEFT_PARENTHESIS);
				setState(76);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la == IDENTIFIER) {
					{
						setState(68);
						parameter();
						setState(73);
						_errHandler.sync(this);
						_la = _input.LA(1);
						while (_la == COMMA) {
							{
								{
									setState(69);
									match(COMMA);
									setState(70);
									parameter();
								}
							}
							setState(75);
							_errHandler.sync(this);
							_la = _input.LA(1);
						}
					}
				}

				setState(78);
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
				setState(80);
				match(SPLITTED);
				setState(81);
				match(LEFT_SQUARE);
				setState(90);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la == IDENTIFIER) {
					{
						setState(82);
						match(IDENTIFIER);
						setState(87);
						_errHandler.sync(this);
						_la = _input.LA(1);
						while (_la == COMMA) {
							{
								{
									setState(83);
									match(COMMA);
									setState(84);
									match(IDENTIFIER);
								}
							}
							setState(89);
							_errHandler.sync(this);
							_la = _input.LA(1);
						}
					}
				}

				setState(92);
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
				setState(94);
				match(IDENTIFIER);
				setState(96);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la == AS) {
					{
						setState(95);
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
				setState(98);
				match(NEW_LINE_INDENT);
				setState(105);
				_errHandler.sync(this);
				_la = _input.LA(1);
				do {
					{
						{
							setState(99);
							node();
							setState(101);
							_errHandler.sync(this);
							_la = _input.LA(1);
							do {
								{
									{
										setState(100);
										match(NEWLINE);
									}
								}
								setState(103);
								_errHandler.sync(this);
								_la = _input.LA(1);
							} while (_la == NEWLINE);
						}
					}
					setState(107);
					_errHandler.sync(this);
					_la = _input.LA(1);
				} while ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << MINUS) | (1L << PLUS) | (1L << STAR))) != 0));
				setState(109);
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
				setState(111);
				match(AS);
				setState(116);
				_errHandler.sync(this);
				switch (_input.LA(1)) {
					case REGEX: {
						setState(112);
						match(REGEX);
					}
					break;
					case TIMETAG: {
						{
							setState(113);
							match(TIMETAG);
							setState(114);
							match(COLON);
							setState(115);
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

	public static class OwnerAndConsumerContext extends ParserRuleContext {
		public TerminalNode OWNER() {
			return getToken(ArchetypeGrammar.OWNER, 0);
		}

		public TerminalNode IDENTIFIER() {
			return getToken(ArchetypeGrammar.IDENTIFIER, 0);
		}

		public UsesContext uses() {
			return getRuleContext(UsesContext.class, 0);
		}

		public OwnerAndConsumerContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}

		@Override
		public int getRuleIndex() {
			return RULE_ownerAndConsumer;
		}

		@Override
		public void enterRule(ParseTreeListener listener) {
			if (listener instanceof ArchetypeGrammarListener)
				((ArchetypeGrammarListener) listener).enterOwnerAndConsumer(this);
		}

		@Override
		public void exitRule(ParseTreeListener listener) {
			if (listener instanceof ArchetypeGrammarListener)
				((ArchetypeGrammarListener) listener).exitOwnerAndConsumer(this);
		}

		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if (visitor instanceof ArchetypeGrammarVisitor)
				return ((ArchetypeGrammarVisitor<? extends T>) visitor).visitOwnerAndConsumer(this);
			else return visitor.visitChildren(this);
		}
	}

	public final OwnerAndConsumerContext ownerAndConsumer() throws RecognitionException {
		OwnerAndConsumerContext _localctx = new OwnerAndConsumerContext(_ctx, getState());
		enterRule(_localctx, 18, RULE_ownerAndConsumer);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
				setState(120);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la == OWNER) {
					{
						setState(118);
						match(OWNER);
						setState(119);
						match(IDENTIFIER);
					}
				}

				setState(123);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la == CONSUMER) {
					{
						setState(122);
						uses();
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

	public static class UsesContext extends ParserRuleContext {
		public TerminalNode CONSUMER() {
			return getToken(ArchetypeGrammar.CONSUMER, 0);
		}

		public List<TerminalNode> IDENTIFIER() {
			return getTokens(ArchetypeGrammar.IDENTIFIER);
		}

		public TerminalNode IDENTIFIER(int i) {
			return getToken(ArchetypeGrammar.IDENTIFIER, i);
		}

		public UsesContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}

		@Override
		public int getRuleIndex() {
			return RULE_uses;
		}

		@Override
		public void enterRule(ParseTreeListener listener) {
			if (listener instanceof ArchetypeGrammarListener) ((ArchetypeGrammarListener) listener).enterUses(this);
		}

		@Override
		public void exitRule(ParseTreeListener listener) {
			if (listener instanceof ArchetypeGrammarListener) ((ArchetypeGrammarListener) listener).exitUses(this);
		}

		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if (visitor instanceof ArchetypeGrammarVisitor)
				return ((ArchetypeGrammarVisitor<? extends T>) visitor).visitUses(this);
			else return visitor.visitChildren(this);
		}
	}

	public final UsesContext uses() throws RecognitionException {
		UsesContext _localctx = new UsesContext(_ctx, getState());
		enterRule(_localctx, 20, RULE_uses);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
				setState(125);
				match(CONSUMER);
				setState(127);
				_errHandler.sync(this);
				_la = _input.LA(1);
				do {
					{
						{
							setState(126);
							match(IDENTIFIER);
						}
					}
					setState(129);
					_errHandler.sync(this);
					_la = _input.LA(1);
				} while (_la == IDENTIFIER);
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
			"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\3!\u0086\4\2\t\2\4" +
					"\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13\t" +
					"\13\4\f\t\f\3\2\7\2\32\n\2\f\2\16\2\35\13\2\3\2\3\2\7\2!\n\2\f\2\16\2" +
					"$\13\2\7\2&\n\2\f\2\16\2)\13\2\3\2\3\2\3\3\3\3\5\3/\n\3\3\4\3\4\3\4\3" +
					"\4\5\4\65\n\4\3\4\3\4\5\49\n\4\3\4\3\4\3\4\5\4>\n\4\5\4@\n\4\3\4\3\4\3" +
					"\5\3\5\3\6\3\6\3\6\3\6\7\6J\n\6\f\6\16\6M\13\6\5\6O\n\6\3\6\3\6\3\7\3" +
					"\7\3\7\3\7\3\7\7\7X\n\7\f\7\16\7[\13\7\5\7]\n\7\3\7\3\7\3\b\3\b\5\bc\n" +
					"\b\3\t\3\t\3\t\6\th\n\t\r\t\16\ti\6\tl\n\t\r\t\16\tm\3\t\3\t\3\n\3\n\3" +
					"\n\3\n\3\n\5\nw\n\n\3\13\3\13\5\13{\n\13\3\13\5\13~\n\13\3\f\3\f\6\f\u0082" +
					"\n\f\r\f\16\f\u0083\3\f\2\2\r\2\4\6\b\n\f\16\20\22\24\26\2\3\3\2\13\r" +
					"\2\u008e\2\33\3\2\2\2\4,\3\2\2\2\6\60\3\2\2\2\bC\3\2\2\2\nE\3\2\2\2\f" +
					"R\3\2\2\2\16`\3\2\2\2\20d\3\2\2\2\22q\3\2\2\2\24z\3\2\2\2\26\177\3\2\2" +
					"\2\30\32\7\27\2\2\31\30\3\2\2\2\32\35\3\2\2\2\33\31\3\2\2\2\33\34\3\2" +
					"\2\2\34\'\3\2\2\2\35\33\3\2\2\2\36\"\5\4\3\2\37!\7\27\2\2 \37\3\2\2\2" +
					"!$\3\2\2\2\" \3\2\2\2\"#\3\2\2\2#&\3\2\2\2$\"\3\2\2\2%\36\3\2\2\2&)\3" +
					"\2\2\2\'%\3\2\2\2\'(\3\2\2\2(*\3\2\2\2)\'\3\2\2\2*+\7\2\2\3+\3\3\2\2\2" +
					",.\5\6\4\2-/\5\20\t\2.-\3\2\2\2./\3\2\2\2/\5\3\2\2\2\60\61\5\b\5\2\61" +
					"\64\7\26\2\2\62\65\5\n\6\2\63\65\5\f\7\2\64\62\3\2\2\2\64\63\3\2\2\2\64" +
					"\65\3\2\2\2\658\3\2\2\2\66\67\7\3\2\2\679\7\25\2\28\66\3\2\2\289\3\2\2" +
					"\29?\3\2\2\2:;\7\4\2\2;=\7\25\2\2<>\5\22\n\2=<\3\2\2\2=>\3\2\2\2>@\3\2" +
					"\2\2?:\3\2\2\2?@\3\2\2\2@A\3\2\2\2AB\5\24\13\2B\7\3\2\2\2CD\t\2\2\2D\t" +
					"\3\2\2\2EN\7\6\2\2FK\5\16\b\2GH\7\n\2\2HJ\5\16\b\2IG\3\2\2\2JM\3\2\2\2" +
					"KI\3\2\2\2KL\3\2\2\2LO\3\2\2\2MK\3\2\2\2NF\3\2\2\2NO\3\2\2\2OP\3\2\2\2" +
					"PQ\7\7\2\2Q\13\3\2\2\2RS\7\5\2\2S\\\7\b\2\2TY\7\26\2\2UV\7\n\2\2VX\7\26" +
					"\2\2WU\3\2\2\2X[\3\2\2\2YW\3\2\2\2YZ\3\2\2\2Z]\3\2\2\2[Y\3\2\2\2\\T\3" +
					"\2\2\2\\]\3\2\2\2]^\3\2\2\2^_\7\t\2\2_\r\3\2\2\2`b\7\26\2\2ac\5\22\n\2" +
					"ba\3\2\2\2bc\3\2\2\2c\17\3\2\2\2dk\7\33\2\2eg\5\4\3\2fh\7\27\2\2gf\3\2" +
					"\2\2hi\3\2\2\2ig\3\2\2\2ij\3\2\2\2jl\3\2\2\2ke\3\2\2\2lm\3\2\2\2mk\3\2" +
					"\2\2mn\3\2\2\2no\3\2\2\2op\7\34\2\2p\21\3\2\2\2qv\7\16\2\2rw\7\17\2\2" +
					"st\7\22\2\2tu\7\23\2\2uw\7\26\2\2vr\3\2\2\2vs\3\2\2\2w\23\3\2\2\2xy\7" +
					"\20\2\2y{\7\26\2\2zx\3\2\2\2z{\3\2\2\2{}\3\2\2\2|~\5\26\f\2}|\3\2\2\2" +
					"}~\3\2\2\2~\25\3\2\2\2\177\u0081\7\21\2\2\u0080\u0082\7\26\2\2\u0081\u0080" +
					"\3\2\2\2\u0082\u0083\3\2\2\2\u0083\u0081\3\2\2\2\u0083\u0084\3\2\2\2\u0084" +
					"\27\3\2\2\2\25\33\"\'.\648=?KNY\\bimvz}\u0083";
	public static final ATN _ATN =
			new ATNDeserializer().deserialize(_serializedATN.toCharArray());

	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}