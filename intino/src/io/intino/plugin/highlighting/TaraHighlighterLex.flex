package io.intino.tara.plugin.highlighting;

import com.intellij.psi.TokenType;
import com.intellij.psi.tree.IElementType;
import com.intellij.openapi.project.Project;
import io.intino.tara.Language;
import LanguageManager;
import TaraTypes;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

%%

%class TaraHighlighterLex
%implements FlexLexer
%unicode
%function advance
%type IElementType

%{
	private Set<String> identifiers;
	private Project project;
	private static final String DSL = "dsl";
	private String dsl = null;

	public TaraHighlighterLex(java.io.Reader reader, Project project) {
		this.zzReader = reader;
		this.project = project;
	}

	private IElementType evaluateIdentifier() {
		String identifier = yytext().toString();
		if (identifiers == null) return TaraTypes.IDENTIFIER_KEY;
		return identifiers.contains(identifier) ? TaraTypes.METAIDENTIFIER_KEY : TaraTypes.IDENTIFIER_KEY;
	}

	private void loadHeritage() {
		if (identifiers != null) return;
		if (dsl == null) {
			String source = zzBuffer.toString().trim();
			int nl = source.indexOf('\n');
            String dslLine = nl > 0 ? source.substring(0, nl).trim() : source;
			if (!dslLine.startsWith(DSL) || dslLine.split(DSL).length < 2) return;
			dsl = dslLine.split(DSL)[1].trim();
		}
		identifiers = new HashSet<>();
		Language heritage = LanguageManager.getLanguage(project, dsl);
        if (heritage != null) Collections.addAll(identifiers, heritage.lexicon());
	}
%}

SUB                 = "sub"
HAS                 = "has"
EXTENDS             = "extends"
DSL                 = "dsl"
AS                  = "as"
IS                  = "is"
INTO                = "into"
VAR                 = "var"
USE                 = "use"

WITH                = "with"

//Tags
ABSTRACT            = "abstract"
TERMINAL            = "terminal"
COMPONENT           = "component"
CONCEPT             = "concept"
FEATURE             = "feature"
CONCEPT             = "concept"
FINAL               = "final"
ENCLOSED            = "enclosed"
PRIVATE             = "private"
REACTIVE            = "reactive"
VOLATILE            = "volatile"
REQUIRED            = "required"
DECORABLE           = "decorable"
DIVINE              = "divine"

LEFT_PARENTHESIS    = "("
RIGHT_PARENTHESIS   = ")"
LEFT_SQUARE         = "["
RIGHT_SQUARE        = "]"
LEFT_CURLY			= "{"
RIGHT_CURLY        	= "}"
DOLLAR              = "$"
EURO                = "€"
GRADE               = "º" | "°"
PERCENTAGE          = "%"
DOT                 = "."
BY                  = "·"
DIVIDED_BY          = "/"
COMMA               = ","
COLON               = ":"
EQUALS              = "="
STAR                = "*"
SEMICOLON           = ";"
QUOTE               = "\""
SINGLE_QUOTE        = "'"
DASH                = "-"
UNDERDASH           = "_"
MINOR				= "<"
MAYOR				= ">"
DASHES              = {DASH} {DASH}+
PLUS                = "+"
HASHTAG             = "#"
AT					= "@"

WORD_TYPE           = "word"
RESOURCE_TYPE       = "resource"
INT_TYPE            = "integer"
FUNCTION_TYPE       = "function"
LONG_TYPE           = "long"
DOUBLE_TYPE         = "double"
STRING_TYPE         = "string"
BOOLEAN_TYPE        = "boolean"
OBJECT_TYPE         = "object"
DATE_TYPE           = "datex"
INSTANT_TYPE        = "instant"
TIME_TYPE           = "time"
BOOLEAN_VALUE_KEY   = "true" | "false"
EMPTY_REF           = "empty"
NATURAL_VALUE_KEY   = {PLUS}? {DIGIT}+
NEGATIVE_VALUE_KEY  = {DASH} {DIGIT}+
SCIENCE_NOTATION    = "E" ({PLUS} | {DASH})? {DIGIT}+
DOUBLE_VALUE_KEY    = ({PLUS} | {DASH})? {DIGIT}+ {DOT} {DIGIT}+ {SCIENCE_NOTATION}?
AT					= "@"
STRING_MULTILINE    	= {EQUALS} {EQUALS}+
NATIVE_MULTILINE_VALUE  = {DASHES}
CLASS_TYPE   		= {IDENTIFIER_KEY} {MINOR} {IDENTIFIER_KEY} ({COMMA} {SP}* {IDENTIFIER_KEY})? {MAYOR}
METRIC_VALUE_KEY    = ([:jletter:] | {PERCENTAGE} | {DOLLAR}| {EURO} | {GRADE}) ([:jletterdigit:] | {UNDERDASH} | {DASH}| {BY} | {DIVIDED_BY})*
DOC_LINE            = "!!" ~[\n]

COMMENT = {TraditionalComment} | {EndOfLineComment} | {DocumentationComment}

TraditionalComment   = "/*" [^*] ~"*/" | "/*" "*"+ "/"
LineTerminator      = \r|\n|\r\n
EndOfLineComment     = "//" [^\n]* {LineTerminator}
DocumentationComment = "/**" {CommentContent} "*"+ "/"
CommentContent       = ( [^*] | \*+ [^/*] )*

DIGIT               = [:digit:]
IDENTIFIER_KEY      = [:jletter:] ([:jletterdigit:] | {DASH})*

SP                  = ([ ]+ | [\t]+) | ">"
SPACES              = {SP}+
NEWLINE             = [\n]+


%xstate QUOTED, MULTILINE, EXPRESSION, EXPRESSION_MULTILINE

%%
<YYINITIAL> {
	{COMMENT}                       {   return TaraTypes.COMMENT;}

	{DSL}                           {   loadHeritage();  return TaraTypes.DSL; }

	{USE}                           {   return TaraTypes.USE; }
	{VAR}                           {   return TaraTypes.VAR; }
	{HAS}                           {   return TaraTypes.HAS; }
	{EXTENDS}                       {   return TaraTypes.EXTENDS; }
	{AS}                            {   return TaraTypes.AS; }
	{IS}                            {   return TaraTypes.IS; }
	{INTO}                          {   return TaraTypes.INTO; }
	{WITH}                          {   return TaraTypes.WITH; }

	{COLON}                         {   return TaraTypes.COLON; }
	{EQUALS}                        {   return TaraTypes.EQUALS; }
	{STAR}                          {   return TaraTypes.STAR; }
	{PLUS}                          {   return TaraTypes.PLUS; }

	{AT}   		                    {   return TaraTypes.AT; }
	{CLASS_TYPE}                    {   return TaraTypes.CLASS_TYPE; }

	{SUB}                           {   return TaraTypes.SUB; }

	{ABSTRACT}                      {   return TaraTypes.ABSTRACT; }
    {COMPONENT}                     {   return TaraTypes.COMPONENT; }
    {FEATURE}                       {   return TaraTypes.FEATURE; }
    {CONCEPT}                    	{   return TaraTypes.CONCEPT; }
    {REACTIVE}                      {   return TaraTypes.REACTIVE; }
    {TERMINAL}                      {   return TaraTypes.TERMINAL; }
    {ENCLOSED}                      {   return TaraTypes.ENCLOSED; }
	{PRIVATE}                       {   return TaraTypes.PRIVATE; }
    {FINAL}                         {   return TaraTypes.FINAL; }
    {VOLATILE}                      {   return TaraTypes.VOLATILE; }
    {DIVINE}                        {   return TaraTypes.DIVINE; }
    {REQUIRED}                      {   return TaraTypes.REQUIRED; }
    {DECORABLE}                     {   return TaraTypes.DECORABLE; }

	{DOC_LINE}                      {   yypushback(1); return TaraTypes.DOC_LINE; }

	{QUOTE}                         {   yybegin(QUOTED); return TaraTypes.QUOTE_BEGIN; }
	{STRING_MULTILINE}              {   yybegin(MULTILINE); return TaraTypes.QUOTE_BEGIN; }

	{SINGLE_QUOTE}					{   yybegin(EXPRESSION); return TaraTypes.EXPRESSION_BEGIN; }
	{NATIVE_MULTILINE_VALUE}		{   yybegin(EXPRESSION_MULTILINE); return TaraTypes.EXPRESSION_BEGIN; }

	{BOOLEAN_VALUE_KEY}             {   return TaraTypes.BOOLEAN_VALUE_KEY; }
	{DOUBLE_VALUE_KEY}              {   return TaraTypes.DOUBLE_VALUE_KEY; }
	{NEGATIVE_VALUE_KEY}            {   return TaraTypes.NEGATIVE_VALUE_KEY; }
	{NATURAL_VALUE_KEY}             {   return TaraTypes.NATURAL_VALUE_KEY; }
	{EMPTY_REF}                     {   return TaraTypes.EMPTY_REF; }

	{LEFT_PARENTHESIS}              {   return TaraTypes.LEFT_PARENTHESIS; }
    {RIGHT_PARENTHESIS}             {   return TaraTypes.RIGHT_PARENTHESIS; }

	{LEFT_SQUARE}                   {   return TaraTypes.LEFT_SQUARE; }
	{RIGHT_SQUARE}                  {   return TaraTypes.RIGHT_SQUARE; }

	{LEFT_CURLY}                    {   return TaraTypes.LEFT_CURLY; }
	{RIGHT_CURLY}                   {   return TaraTypes.RIGHT_CURLY; }

	{DOT}                           {   return TaraTypes.DOT; }
	{COMMA}                         {   return TaraTypes.COMMA; }

	{WORD_TYPE}                     {   return TaraTypes.WORD_TYPE; }
	{RESOURCE_TYPE}                 {   return TaraTypes.RESOURCE_TYPE; }
	{INT_TYPE}                      {   return TaraTypes.INT_TYPE; }
    {FUNCTION_TYPE}                 {   return TaraTypes.FUNCTION_TYPE; }
	{BOOLEAN_TYPE}                  {   return TaraTypes.BOOLEAN_TYPE; }
	{OBJECT_TYPE}                   {   return TaraTypes.OBJECT_TYPE; }
    {STRING_TYPE}                   {   return TaraTypes.STRING_TYPE; }
    {DOUBLE_TYPE}                   {   return TaraTypes.DOUBLE_TYPE; }
    {LONG_TYPE}                   	{   return TaraTypes.LONG_TYPE; }
    {INSTANT_TYPE}                  {   return TaraTypes.INSTANT_TYPE; }
    {DATE_TYPE}                     {   return TaraTypes.DATE_TYPE; }
    {TIME_TYPE}                     {   return TaraTypes.TIME_TYPE; }
	{SEMICOLON}                     {   return TaraTypes.DSL;  }

	{SPACES}                        {   return TokenType.WHITE_SPACE; }

    {SP}                            {   return TokenType.WHITE_SPACE; }

	{IDENTIFIER_KEY}                {   return evaluateIdentifier();  }

    {METRIC_VALUE_KEY}              {   return TaraTypes.METRIC_VALUE_KEY; }
    {NEWLINE}                       {   return TokenType.WHITE_SPACE; }
    .                               {   return TokenType.BAD_CHARACTER; }
}

<QUOTED> {
	{QUOTE}                         {   yybegin(YYINITIAL); return TaraTypes.QUOTE_END; }
	[^\n\r\"\\]                     {   return TaraTypes.CHARACTER; }
	\n | \r                         {   return TaraTypes.CHARACTER; }
    \t                              {   return TaraTypes.CHARACTER; }
	\\t                             {   return TaraTypes.CHARACTER; }
	\\n                             {   return TaraTypes.CHARACTER; }
	\\r                             {   return TaraTypes.CHARACTER; }
	\\\"                            {   return TaraTypes.CHARACTER; }
	\\                              {   return TaraTypes.CHARACTER; }
	[^]                             {   return TokenType.BAD_CHARACTER; }
	.                               {   return TokenType.BAD_CHARACTER; }
}

<MULTILINE> {
    {STRING_MULTILINE}              {   yybegin(YYINITIAL); return TaraTypes.QUOTE_END; }
    [^\n\r\\]                       {   return TaraTypes.CHARACTER; }
	\n | \r                         {   return TaraTypes.CHARACTER; }
	\t                              {   return TaraTypes.CHARACTER; }
	\\t                             {   return TaraTypes.CHARACTER; }
    \\n                             {   return TaraTypes.CHARACTER; }
    \\r                             {   return TaraTypes.CHARACTER; }
    \\\"                            {   return TaraTypes.CHARACTER; }
    \\                              {   return TaraTypes.CHARACTER; }
    [^]                             {   return TokenType.BAD_CHARACTER; }
    .                               {   return TokenType.BAD_CHARACTER; }
}

<EXPRESSION> {
    {SINGLE_QUOTE}                  {   yybegin(YYINITIAL); return TaraTypes.EXPRESSION_END; }
    [^\n\r\'\\]                     {   return TaraTypes.CHARACTER; }
    \n | \r                         {   return TaraTypes.CHARACTER; }
    \t                              {   return TaraTypes.CHARACTER; }
    \\t                             {   return TaraTypes.CHARACTER; }
    \\n                             {   return TaraTypes.CHARACTER; }
    \\r                             {   return TaraTypes.CHARACTER; }
    \\\'                            {   return TaraTypes.CHARACTER; }
    \\                              {   return TaraTypes.CHARACTER; }
    [^]                             {   return TokenType.BAD_CHARACTER;}
    .                               {   return TokenType.BAD_CHARACTER;}
}


<EXPRESSION_MULTILINE> {
    {NATIVE_MULTILINE_VALUE}        {   yybegin(YYINITIAL); return TaraTypes.EXPRESSION_END; }
    [^\n\r\\]                       {   return TaraTypes.CHARACTER; }
    \n | \r                         {   return TaraTypes.CHARACTER; }
    \t                              {   return TaraTypes.CHARACTER; }
    \\t                             {   return TaraTypes.CHARACTER; }
    \\n                             {   return TaraTypes.CHARACTER; }
    \\r                             {   return TaraTypes.CHARACTER; }
    \\                              {   return TaraTypes.CHARACTER; }
    [^]                             {   return TokenType.BAD_CHARACTER; }
    .                               {   return TokenType.BAD_CHARACTER; }
}

[^]                                  {  return TokenType.BAD_CHARACTER; }
