// Generated from /Users/oroncal/workspace/intino/intino/src/io/intino/plugin/archetype/lang/antlr/ArchetypeGrammar.g4 by ANTLR 4.10.1
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
		RuntimeMetaData.checkVersion("4.10.1", RuntimeMetaData.VERSION);
	}

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		IN=1, WITH=2, SPLITTED=3, LEFT_PARENTHESIS=4, RIGHT_PARENTHESIS=5, LEFT_SQUARE=6, 
		RIGHT_SQUARE=7, COMMA=8, MINUS=9, PLUS=10, STAR=11, AS=12, REGEX=13, OWNER=14, 
		CONSUMER=15, TIMETAG=16, COLON=17, LINE_COMMENT=18, LABEL=19, IDENTIFIER=20, 
		NEWLINE=21, SPACES=22, SP=23, NL=24, NEW_LINE_INDENT=25, DEDENT=26, UNKNOWN_TOKEN=27, 
		QUOTE_BEGIN=28, QUOTE_END=29, EXPRESSION_BEGIN=30, EXPRESSION_END=31;
	public static final int
		RULE_root = 0, RULE_node = 1, RULE_declaration = 2, RULE_starting = 3, 
		RULE_parameters = 4, RULE_splitted = 5, RULE_parameter = 6, RULE_body = 7, 
		RULE_type = 8, RULE_ownerAndConsumer = 9, RULE_uses = 10;
	private static String[] makeRuleNames() {
		return new String[] {
			"root", "node", "declaration", "starting", "parameters", "splitted", 
			"parameter", "body", "type", "ownerAndConsumer", "uses"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "'in'", "'with'", "'splitted'", "'('", "')'", "'['", "']'", "','", 
			"'-'", "'+'", "'*'", "'as'", "'regex'", "'owner'", "'consumer'", "'timetag'", 
			"':'", null, null, null, null, null, null, null, "'indent'", "'dedent'", 
			null, "'%QUOTE_BEGIN%'", "'%QUOTE_END%'", "'%EXPRESSION_BEGIN%'", "'%EXPRESSION_END%'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
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
	public String getGrammarFileName() { return "ArchetypeGrammar.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public ATN getATN() { return _ATN; }

	public ArchetypeGrammar(TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	public static class RootContext extends ParserRuleContext {
		public TerminalNode EOF() { return getToken(ArchetypeGrammar.EOF, 0); }
		public List<TerminalNode> NEWLINE() { return getTokens(ArchetypeGrammar.NEWLINE); }
		public TerminalNode NEWLINE(int i) {
			return getToken(ArchetypeGrammar.NEWLINE, i);
		}
		public List<NodeContext> node() {
			return getRuleContexts(NodeContext.class);
		}
		public NodeContext node(int i) {
			return getRuleContext(NodeContext.class,i);
		}
		public RootContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_root; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArchetypeGrammarListener ) ((ArchetypeGrammarListener)listener).enterRoot(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArchetypeGrammarListener ) ((ArchetypeGrammarListener)listener).exitRoot(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArchetypeGrammarVisitor ) return ((ArchetypeGrammarVisitor<? extends T>)visitor).visitRoot(this);
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
			while (_la==NEWLINE) {
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
				while (_la==NEWLINE) {
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
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class NodeContext extends ParserRuleContext {
		public DeclarationContext declaration() {
			return getRuleContext(DeclarationContext.class,0);
		}
		public BodyContext body() {
			return getRuleContext(BodyContext.class,0);
		}
		public NodeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_node; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArchetypeGrammarListener ) ((ArchetypeGrammarListener)listener).enterNode(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArchetypeGrammarListener ) ((ArchetypeGrammarListener)listener).exitNode(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArchetypeGrammarVisitor ) return ((ArchetypeGrammarVisitor<? extends T>)visitor).visitNode(this);
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
			if (_la==NEW_LINE_INDENT) {
				{
				setState(43);
				body();
				}
			}

			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class DeclarationContext extends ParserRuleContext {
		public StartingContext starting() {
			return getRuleContext(StartingContext.class,0);
		}
		public TerminalNode IDENTIFIER() { return getToken(ArchetypeGrammar.IDENTIFIER, 0); }
		public OwnerAndConsumerContext ownerAndConsumer() {
			return getRuleContext(OwnerAndConsumerContext.class,0);
		}
		public ParametersContext parameters() {
			return getRuleContext(ParametersContext.class,0);
		}
		public SplittedContext splitted() {
			return getRuleContext(SplittedContext.class,0);
		}
		public TerminalNode IN() { return getToken(ArchetypeGrammar.IN, 0); }
		public List<TerminalNode> LABEL() { return getTokens(ArchetypeGrammar.LABEL); }
		public TerminalNode LABEL(int i) {
			return getToken(ArchetypeGrammar.LABEL, i);
		}
		public TerminalNode WITH() { return getToken(ArchetypeGrammar.WITH, 0); }
		public TypeContext type() {
			return getRuleContext(TypeContext.class,0);
		}
		public DeclarationContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_declaration; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArchetypeGrammarListener ) ((ArchetypeGrammarListener)listener).enterDeclaration(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArchetypeGrammarListener ) ((ArchetypeGrammarListener)listener).exitDeclaration(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArchetypeGrammarVisitor ) return ((ArchetypeGrammarVisitor<? extends T>)visitor).visitDeclaration(this);
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
			case LEFT_PARENTHESIS:
				{
				setState(48);
				parameters();
				}
				break;
			case SPLITTED:
				{
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
			if (_la==IN) {
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
			if (_la==WITH) {
				{
				setState(56);
				match(WITH);
				setState(57);
				match(LABEL);
				setState(59);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==AS) {
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
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class StartingContext extends ParserRuleContext {
		public TerminalNode MINUS() { return getToken(ArchetypeGrammar.MINUS, 0); }
		public TerminalNode PLUS() { return getToken(ArchetypeGrammar.PLUS, 0); }
		public TerminalNode STAR() { return getToken(ArchetypeGrammar.STAR, 0); }
		public StartingContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_starting; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArchetypeGrammarListener ) ((ArchetypeGrammarListener)listener).enterStarting(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArchetypeGrammarListener ) ((ArchetypeGrammarListener)listener).exitStarting(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArchetypeGrammarVisitor ) return ((ArchetypeGrammarVisitor<? extends T>)visitor).visitStarting(this);
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
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << MINUS) | (1L << PLUS) | (1L << STAR))) != 0)) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ParametersContext extends ParserRuleContext {
		public TerminalNode LEFT_PARENTHESIS() { return getToken(ArchetypeGrammar.LEFT_PARENTHESIS, 0); }
		public TerminalNode RIGHT_PARENTHESIS() { return getToken(ArchetypeGrammar.RIGHT_PARENTHESIS, 0); }
		public List<ParameterContext> parameter() {
			return getRuleContexts(ParameterContext.class);
		}
		public ParameterContext parameter(int i) {
			return getRuleContext(ParameterContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(ArchetypeGrammar.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(ArchetypeGrammar.COMMA, i);
		}
		public ParametersContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_parameters; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArchetypeGrammarListener ) ((ArchetypeGrammarListener)listener).enterParameters(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArchetypeGrammarListener ) ((ArchetypeGrammarListener)listener).exitParameters(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArchetypeGrammarVisitor ) return ((ArchetypeGrammarVisitor<? extends T>)visitor).visitParameters(this);
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
			if (_la==IDENTIFIER) {
				{
				setState(68);
				parameter();
				setState(73);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==COMMA) {
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
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class SplittedContext extends ParserRuleContext {
		public TerminalNode SPLITTED() { return getToken(ArchetypeGrammar.SPLITTED, 0); }
		public TerminalNode LEFT_SQUARE() { return getToken(ArchetypeGrammar.LEFT_SQUARE, 0); }
		public TerminalNode RIGHT_SQUARE() { return getToken(ArchetypeGrammar.RIGHT_SQUARE, 0); }
		public List<TerminalNode> IDENTIFIER() { return getTokens(ArchetypeGrammar.IDENTIFIER); }
		public TerminalNode IDENTIFIER(int i) {
			return getToken(ArchetypeGrammar.IDENTIFIER, i);
		}
		public List<TerminalNode> COMMA() { return getTokens(ArchetypeGrammar.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(ArchetypeGrammar.COMMA, i);
		}
		public SplittedContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_splitted; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArchetypeGrammarListener ) ((ArchetypeGrammarListener)listener).enterSplitted(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArchetypeGrammarListener ) ((ArchetypeGrammarListener)listener).exitSplitted(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArchetypeGrammarVisitor ) return ((ArchetypeGrammarVisitor<? extends T>)visitor).visitSplitted(this);
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
			if (_la==IDENTIFIER) {
				{
				setState(82);
				match(IDENTIFIER);
				setState(87);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==COMMA) {
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
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ParameterContext extends ParserRuleContext {
		public TerminalNode IDENTIFIER() { return getToken(ArchetypeGrammar.IDENTIFIER, 0); }
		public TypeContext type() {
			return getRuleContext(TypeContext.class,0);
		}
		public ParameterContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_parameter; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArchetypeGrammarListener ) ((ArchetypeGrammarListener)listener).enterParameter(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArchetypeGrammarListener ) ((ArchetypeGrammarListener)listener).exitParameter(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArchetypeGrammarVisitor ) return ((ArchetypeGrammarVisitor<? extends T>)visitor).visitParameter(this);
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
			if (_la==AS) {
				{
				setState(95);
				type();
				}
			}

			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class BodyContext extends ParserRuleContext {
		public TerminalNode NEW_LINE_INDENT() { return getToken(ArchetypeGrammar.NEW_LINE_INDENT, 0); }
		public TerminalNode DEDENT() { return getToken(ArchetypeGrammar.DEDENT, 0); }
		public List<NodeContext> node() {
			return getRuleContexts(NodeContext.class);
		}
		public NodeContext node(int i) {
			return getRuleContext(NodeContext.class,i);
		}
		public List<TerminalNode> NEWLINE() { return getTokens(ArchetypeGrammar.NEWLINE); }
		public TerminalNode NEWLINE(int i) {
			return getToken(ArchetypeGrammar.NEWLINE, i);
		}
		public BodyContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_body; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArchetypeGrammarListener ) ((ArchetypeGrammarListener)listener).enterBody(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArchetypeGrammarListener ) ((ArchetypeGrammarListener)listener).exitBody(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArchetypeGrammarVisitor ) return ((ArchetypeGrammarVisitor<? extends T>)visitor).visitBody(this);
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
				} while ( _la==NEWLINE );
				}
				}
				setState(107); 
				_errHandler.sync(this);
				_la = _input.LA(1);
			} while ( (((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << MINUS) | (1L << PLUS) | (1L << STAR))) != 0) );
			setState(109);
			match(DEDENT);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class TypeContext extends ParserRuleContext {
		public TerminalNode AS() { return getToken(ArchetypeGrammar.AS, 0); }
		public TerminalNode REGEX() { return getToken(ArchetypeGrammar.REGEX, 0); }
		public TerminalNode TIMETAG() { return getToken(ArchetypeGrammar.TIMETAG, 0); }
		public TerminalNode COLON() { return getToken(ArchetypeGrammar.COLON, 0); }
		public TerminalNode IDENTIFIER() { return getToken(ArchetypeGrammar.IDENTIFIER, 0); }
		public TypeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_type; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArchetypeGrammarListener ) ((ArchetypeGrammarListener)listener).enterType(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArchetypeGrammarListener ) ((ArchetypeGrammarListener)listener).exitType(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArchetypeGrammarVisitor ) return ((ArchetypeGrammarVisitor<? extends T>)visitor).visitType(this);
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
			case REGEX:
				{
				setState(112);
				match(REGEX);
				}
				break;
			case TIMETAG:
				{
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
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class OwnerAndConsumerContext extends ParserRuleContext {
		public TerminalNode OWNER() { return getToken(ArchetypeGrammar.OWNER, 0); }
		public TerminalNode IDENTIFIER() { return getToken(ArchetypeGrammar.IDENTIFIER, 0); }
		public UsesContext uses() {
			return getRuleContext(UsesContext.class,0);
		}
		public OwnerAndConsumerContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_ownerAndConsumer; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArchetypeGrammarListener ) ((ArchetypeGrammarListener)listener).enterOwnerAndConsumer(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArchetypeGrammarListener ) ((ArchetypeGrammarListener)listener).exitOwnerAndConsumer(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArchetypeGrammarVisitor ) return ((ArchetypeGrammarVisitor<? extends T>)visitor).visitOwnerAndConsumer(this);
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
			if (_la==OWNER) {
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
			if (_la==CONSUMER) {
				{
				setState(122);
				uses();
				}
			}

			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class UsesContext extends ParserRuleContext {
		public TerminalNode CONSUMER() { return getToken(ArchetypeGrammar.CONSUMER, 0); }
		public List<TerminalNode> IDENTIFIER() { return getTokens(ArchetypeGrammar.IDENTIFIER); }
		public TerminalNode IDENTIFIER(int i) {
			return getToken(ArchetypeGrammar.IDENTIFIER, i);
		}
		public UsesContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_uses; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArchetypeGrammarListener ) ((ArchetypeGrammarListener)listener).enterUses(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArchetypeGrammarListener ) ((ArchetypeGrammarListener)listener).exitUses(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ArchetypeGrammarVisitor ) return ((ArchetypeGrammarVisitor<? extends T>)visitor).visitUses(this);
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
			} while ( _la==IDENTIFIER );
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static final String _serializedATN =
			"\u0004\u0001\u001f\u0084\u0002\u0000\u0007\u0000\u0002\u0001\u0007\u0001" +
					"\u0002\u0002\u0007\u0002\u0002\u0003\u0007\u0003\u0002\u0004\u0007\u0004" +
					"\u0002\u0005\u0007\u0005\u0002\u0006\u0007\u0006\u0002\u0007\u0007\u0007" +
					"\u0002\b\u0007\b\u0002\t\u0007\t\u0002\n\u0007\n\u0001\u0000\u0005\u0000" +
					"\u0018\b\u0000\n\u0000\f\u0000\u001b\t\u0000\u0001\u0000\u0001\u0000\u0005" +
					"\u0000\u001f\b\u0000\n\u0000\f\u0000\"\t\u0000\u0005\u0000$\b\u0000\n" +
					"\u0000\f\u0000\'\t\u0000\u0001\u0000\u0001\u0000\u0001\u0001\u0001\u0001" +
					"\u0003\u0001-\b\u0001\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002" +
					"\u0003\u00023\b\u0002\u0001\u0002\u0001\u0002\u0003\u00027\b\u0002\u0001" +
					"\u0002\u0001\u0002\u0001\u0002\u0003\u0002<\b\u0002\u0003\u0002>\b\u0002" +
					"\u0001\u0002\u0001\u0002\u0001\u0003\u0001\u0003\u0001\u0004\u0001\u0004" +
					"\u0001\u0004\u0001\u0004\u0005\u0004H\b\u0004\n\u0004\f\u0004K\t\u0004" +
					"\u0003\u0004M\b\u0004\u0001\u0004\u0001\u0004\u0001\u0005\u0001\u0005" +
					"\u0001\u0005\u0001\u0005\u0001\u0005\u0005\u0005V\b\u0005\n\u0005\f\u0005" +
					"Y\t\u0005\u0003\u0005[\b\u0005\u0001\u0005\u0001\u0005\u0001\u0006\u0001" +
					"\u0006\u0003\u0006a\b\u0006\u0001\u0007\u0001\u0007\u0001\u0007\u0004" +
					"\u0007f\b\u0007\u000b\u0007\f\u0007g\u0004\u0007j\b\u0007\u000b\u0007" +
					"\f\u0007k\u0001\u0007\u0001\u0007\u0001\b\u0001\b\u0001\b\u0001\b\u0001" +
					"\b\u0003\bu\b\b\u0001\t\u0001\t\u0003\ty\b\t\u0001\t\u0003\t|\b\t\u0001" +
					"\n\u0001\n\u0004\n\u0080\b\n\u000b\n\f\n\u0081\u0001\n\u0000\u0000\u000b" +
					"\u0000\u0002\u0004\u0006\b\n\f\u000e\u0010\u0012\u0014\u0000\u0001\u0001" +
					"\u0000\t\u000b\u008c\u0000\u0019\u0001\u0000\u0000\u0000\u0002*\u0001" +
					"\u0000\u0000\u0000\u0004.\u0001\u0000\u0000\u0000\u0006A\u0001\u0000\u0000" +
					"\u0000\bC\u0001\u0000\u0000\u0000\nP\u0001\u0000\u0000\u0000\f^\u0001" +
					"\u0000\u0000\u0000\u000eb\u0001\u0000\u0000\u0000\u0010o\u0001\u0000\u0000" +
					"\u0000\u0012x\u0001\u0000\u0000\u0000\u0014}\u0001\u0000\u0000\u0000\u0016" +
					"\u0018\u0005\u0015\u0000\u0000\u0017\u0016\u0001\u0000\u0000\u0000\u0018" +
					"\u001b\u0001\u0000\u0000\u0000\u0019\u0017\u0001\u0000\u0000\u0000\u0019" +
					"\u001a\u0001\u0000\u0000\u0000\u001a%\u0001\u0000\u0000\u0000\u001b\u0019" +
					"\u0001\u0000\u0000\u0000\u001c \u0003\u0002\u0001\u0000\u001d\u001f\u0005" +
					"\u0015\u0000\u0000\u001e\u001d\u0001\u0000\u0000\u0000\u001f\"\u0001\u0000" +
					"\u0000\u0000 \u001e\u0001\u0000\u0000\u0000 !\u0001\u0000\u0000\u0000" +
					"!$\u0001\u0000\u0000\u0000\" \u0001\u0000\u0000\u0000#\u001c\u0001\u0000" +
					"\u0000\u0000$\'\u0001\u0000\u0000\u0000%#\u0001\u0000\u0000\u0000%&\u0001" +
					"\u0000\u0000\u0000&(\u0001\u0000\u0000\u0000\'%\u0001\u0000\u0000\u0000" +
					"()\u0005\u0000\u0000\u0001)\u0001\u0001\u0000\u0000\u0000*,\u0003\u0004" +
					"\u0002\u0000+-\u0003\u000e\u0007\u0000,+\u0001\u0000\u0000\u0000,-\u0001" +
					"\u0000\u0000\u0000-\u0003\u0001\u0000\u0000\u0000./\u0003\u0006\u0003" +
					"\u0000/2\u0005\u0014\u0000\u000003\u0003\b\u0004\u000013\u0003\n\u0005" +
					"\u000020\u0001\u0000\u0000\u000021\u0001\u0000\u0000\u000023\u0001\u0000" +
					"\u0000\u000036\u0001\u0000\u0000\u000045\u0005\u0001\u0000\u000057\u0005" +
					"\u0013\u0000\u000064\u0001\u0000\u0000\u000067\u0001\u0000\u0000\u0000" +
					"7=\u0001\u0000\u0000\u000089\u0005\u0002\u0000\u00009;\u0005\u0013\u0000" +
					"\u0000:<\u0003\u0010\b\u0000;:\u0001\u0000\u0000\u0000;<\u0001\u0000\u0000" +
					"\u0000<>\u0001\u0000\u0000\u0000=8\u0001\u0000\u0000\u0000=>\u0001\u0000" +
					"\u0000\u0000>?\u0001\u0000\u0000\u0000?@\u0003\u0012\t\u0000@\u0005\u0001" +
					"\u0000\u0000\u0000AB\u0007\u0000\u0000\u0000B\u0007\u0001\u0000\u0000" +
					"\u0000CL\u0005\u0004\u0000\u0000DI\u0003\f\u0006\u0000EF\u0005\b\u0000" +
					"\u0000FH\u0003\f\u0006\u0000GE\u0001\u0000\u0000\u0000HK\u0001\u0000\u0000" +
					"\u0000IG\u0001\u0000\u0000\u0000IJ\u0001\u0000\u0000\u0000JM\u0001\u0000" +
					"\u0000\u0000KI\u0001\u0000\u0000\u0000LD\u0001\u0000\u0000\u0000LM\u0001" +
					"\u0000\u0000\u0000MN\u0001\u0000\u0000\u0000NO\u0005\u0005\u0000\u0000" +
					"O\t\u0001\u0000\u0000\u0000PQ\u0005\u0003\u0000\u0000QZ\u0005\u0006\u0000" +
					"\u0000RW\u0005\u0014\u0000\u0000ST\u0005\b\u0000\u0000TV\u0005\u0014\u0000" +
					"\u0000US\u0001\u0000\u0000\u0000VY\u0001\u0000\u0000\u0000WU\u0001\u0000" +
					"\u0000\u0000WX\u0001\u0000\u0000\u0000X[\u0001\u0000\u0000\u0000YW\u0001" +
					"\u0000\u0000\u0000ZR\u0001\u0000\u0000\u0000Z[\u0001\u0000\u0000\u0000" +
					"[\\\u0001\u0000\u0000\u0000\\]\u0005\u0007\u0000\u0000]\u000b\u0001\u0000" +
					"\u0000\u0000^`\u0005\u0014\u0000\u0000_a\u0003\u0010\b\u0000`_\u0001\u0000" +
					"\u0000\u0000`a\u0001\u0000\u0000\u0000a\r\u0001\u0000\u0000\u0000bi\u0005" +
					"\u0019\u0000\u0000ce\u0003\u0002\u0001\u0000df\u0005\u0015\u0000\u0000" +
					"ed\u0001\u0000\u0000\u0000fg\u0001\u0000\u0000\u0000ge\u0001\u0000\u0000" +
					"\u0000gh\u0001\u0000\u0000\u0000hj\u0001\u0000\u0000\u0000ic\u0001\u0000" +
					"\u0000\u0000jk\u0001\u0000\u0000\u0000ki\u0001\u0000\u0000\u0000kl\u0001" +
					"\u0000\u0000\u0000lm\u0001\u0000\u0000\u0000mn\u0005\u001a\u0000\u0000" +
					"n\u000f\u0001\u0000\u0000\u0000ot\u0005\f\u0000\u0000pu\u0005\r\u0000" +
					"\u0000qr\u0005\u0010\u0000\u0000rs\u0005\u0011\u0000\u0000su\u0005\u0014" +
					"\u0000\u0000tp\u0001\u0000\u0000\u0000tq\u0001\u0000\u0000\u0000u\u0011" +
					"\u0001\u0000\u0000\u0000vw\u0005\u000e\u0000\u0000wy\u0005\u0014\u0000" +
					"\u0000xv\u0001\u0000\u0000\u0000xy\u0001\u0000\u0000\u0000y{\u0001\u0000" +
					"\u0000\u0000z|\u0003\u0014\n\u0000{z\u0001\u0000\u0000\u0000{|\u0001\u0000" +
					"\u0000\u0000|\u0013\u0001\u0000\u0000\u0000}\u007f\u0005\u000f\u0000\u0000" +
					"~\u0080\u0005\u0014\u0000\u0000\u007f~\u0001\u0000\u0000\u0000\u0080\u0081" +
					"\u0001\u0000\u0000\u0000\u0081\u007f\u0001\u0000\u0000\u0000\u0081\u0082" +
					"\u0001\u0000\u0000\u0000\u0082\u0015\u0001\u0000\u0000\u0000\u0013\u0019" +
					" %,26;=ILWZ`gktx{\u0081";
	public static final ATN _ATN =
			new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}