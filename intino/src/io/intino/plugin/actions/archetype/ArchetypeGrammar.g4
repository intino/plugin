parser grammar ArchetypeGrammar;
options { tokenVocab=ArchetypeLexer; }

root: NEWLINE* (node NEWLINE*)* EOF;
node: declaration body?;
declaration: starting IDENTIFIER parameters? (IN LABEL)? (WITH LABEL type?)?;
starting: MINUS | PLUS | STAR;
parameters: LEFT_PARENTHESIS (parameter (COMMA parameter)*)? RIGHT_PARENTHESIS;
parameter: IDENTIFIER type?;
body: NEW_LINE_INDENT (node NEWLINE+)+ DEDENT;
type: AS (REGEX|(TIMETAG COLON IDENTIFIER));
