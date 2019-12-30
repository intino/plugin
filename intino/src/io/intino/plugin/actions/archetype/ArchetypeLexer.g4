lexer grammar ArchetypeLexer;
@header{
import io.intino.plugin.actions.archetype.BlockManager;
}
@lexer::members {
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
        return queue.isEmpty()? emitEOF() : queue.poll();
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
        }
        else skip();
    }

    private String getTextSpaces(String text) {
        int index = (text.indexOf(' ') == -1)? text.indexOf('\t') : text.indexOf(' ');
        return (index == -1)? "" : text.substring(index);
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

    private int translate (BlockManager.Token token) {
        if (token.toString().equals("NEWLINE")) return NEWLINE;
        if (token.toString().equals("DEDENT")) return DEDENT;
        if (token.toString().equals("NEWLINE_INDENT")) return NEW_LINE_INDENT;
        return UNKNOWN_TOKEN;
    }
}


IN                	: 'in';
WITH              	: 'with';

LEFT_PARENTHESIS    : '(';
RIGHT_PARENTHESIS   : ')';
COMMA               : ',';

MINUS               : '-';
PLUS                : '+';
STAR                : '*';
AS	                : 'as';
REGEX               : 'regex';
TIMETAG             : 'timetag';
COLON               : ':';

LABEL         		: '"' (~('"' | '\\') | '\\' ('"' | '\\'))* '"';

IDENTIFIER          : (LETTER | UNDERDASH) (DIGIT | LETTER | DASH | UNDERDASH | DOT)*;

NEWLINE: NL+ SP* { newlinesAndSpaces(); };

SPACES: SP+ EOF? 	-> channel(HIDDEN);

SP: (' ' | '\t');
NL: ('\r'? '\n' | '\r');

NEW_LINE_INDENT: 'indent';
DEDENT         : 'dedent';

UNKNOWN_TOKEN: . ;

QUOTE_BEGIN        : '%QUOTE_BEGIN%';
QUOTE_END          : '%QUOTE_END%';
EXPRESSION_BEGIN   : '%EXPRESSION_BEGIN%';
EXPRESSION_END     :  '%EXPRESSION_END%';

fragment DOLLAR              : '$';
fragment EURO                : '€';
fragment PERCENTAGE          : '%';
fragment GRADE               : 'º'| '°';
fragment BY                  : '·';
fragment DIVIDED_BY          : '/';
fragment DASH                : '-';

fragment UNDERDASH           : '_';
fragment DOT                 : '.';
fragment DIGIT               : [0-9];
fragment LETTER              : 'a'..'z' | 'A'..'Z' | 'ñ' | 'Ñ' | 'á'| 'é'| 'í' | 'ó' | 'ú' | 'Á'| 'É' | 'Í' | 'Ó' | 'Ú';