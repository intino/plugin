package io.intino.plugin.archetype.lang.lexer;

import com.intellij.lexer.FlexLexer;
import com.intellij.psi.tree.IElementType;
import io.intino.plugin.archetype.lang.psi.ArchetypeTypes;

%%

%class ArchetypeLexer
%implements FlexLexer
%unicode
%function advance
%type IElementType


IN                	= "in"
WITH              	= "with"
SPLITTED           	= "splitted"
OWNER           	= "owner"
CONSUMER         	= "consumer"
AS	           		= "as"
REGEX          		= "regex"
NEWLINE             = [\r|\n|\r\n]+ ([ ] | [\t])*

LEFT_PARENTHESIS    = "("
RIGHT_PARENTHESIS   = ")"
LEFT_SQUARE         = "["
RIGHT_SQUARE        = "]"
MINUS               = "-"
PLUS                = "+"
STAR                = "*"
QUOTE               = "\""
COMMENT 			= {NEWLINE}? "#" [^\n]*
UNDERDASH           = "_"
DASH	            = "-"
IDENTIFIER_KEY      = [:jletter:] ([:jletterdigit:] | {UNDERDASH} | {DASH})*
%xstate QUOTED
%%
<YYINITIAL> {
{IN}                       	{   return ArchetypeTypes.KEYWORD; }
{WITH}                     	{   return ArchetypeTypes.KEYWORD; }
{SPLITTED}                 	{   return ArchetypeTypes.KEYWORD; }
{AS}	                	{   return ArchetypeTypes.KEYWORD;	}
{REGEX}             		{   return ArchetypeTypes.KEYWORD;	}

{OWNER}                 	{   return ArchetypeTypes.ANNOTATION; }
{CONSUMER}	                {   return ArchetypeTypes.ANNOTATION;	}

{MINUS}                 	{   return ArchetypeTypes.MINUS; }
{STAR}                 		{   return ArchetypeTypes.STAR; }
{PLUS}                 		{   return ArchetypeTypes.PLUS; }
{LEFT_SQUARE}              	{   return ArchetypeTypes.LEFT_SQUARE; }
{RIGHT_SQUARE}             	{   return ArchetypeTypes.RIGHT_SQUARE; }
{LEFT_PARENTHESIS}        	{   return ArchetypeTypes.LEFT_PARENTHESIS; }
{RIGHT_PARENTHESIS}        	{   return ArchetypeTypes.RIGHT_PARENTHESIS; }
{IDENTIFIER_KEY}            {   return ArchetypeTypes.IDENTIFIER_KEY; }
{COMMENT}          		   	{   return ArchetypeTypes.COMMENT; }
{QUOTE}                    	{   yybegin(QUOTED); return ArchetypeTypes.QUOTE_BEGIN; }
}

<QUOTED> {
    {QUOTE}                         {   yybegin(YYINITIAL); return ArchetypeTypes.QUOTE_END; }
    [^\n\r\"\\]                     {   return ArchetypeTypes.CHARACTER; }
    \n | \r                         {   return ArchetypeTypes.CHARACTER; }
    \t                              {   return ArchetypeTypes.CHARACTER; }
    \\t                             {   return ArchetypeTypes.CHARACTER; }
    \\n                             {   return ArchetypeTypes.CHARACTER; }
    \\r                             {   return ArchetypeTypes.CHARACTER; }
    \\\"                            {   return ArchetypeTypes.CHARACTER; }
    \\                              {   return ArchetypeTypes.CHARACTER; }
}

[^]                                 { return ArchetypeTypes.TEXT;}
.                                   { return ArchetypeTypes.TEXT;}