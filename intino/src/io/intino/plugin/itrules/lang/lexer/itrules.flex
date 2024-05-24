package io.intino.plugin.itrules.lang.lexer;

import com.intellij.psi.tree.IElementType;
import io.intino.plugin.itrules.lang.psi.ItrulesTypes;

%%

%class ItrulesLexer
%implements FlexLexer
%unicode
%function advance
%type IElementType

%xstate HEADER, BODY
SCAPED_CHAR         = "$$" | "$<<" | "$>>"
DEFRULE             = "def"

NOT                 = "not"
OR                  = "or"
AND                 = "and"
DASH                = "-"
UNDERDASH           = "_"
TARGET		        = "<" ({IDENTIFIER_KEY}| ".")+ ">"
PLACEHOLDER         = "$" {TARGET}? {IDENTIFIER_KEY}
FORMATTER           = "+" {IDENTIFIER_KEY}
LEFT_EXPR           = "<<"
RIGHT_EXPR          = ">>"
MULTIPLE			= "...[" (~']')* "]"
IDENTIFIER_KEY      = [:jletter:] ([:jletterdigit:] | {UNDERDASH} | {DASH})*
NEWLINE             = [\r|\n|\r\n]
ENDRULE             = {NEWLINE} "end"
%%
<YYINITIAL> {
	{DEFRULE}                      {   yybegin(HEADER); return ItrulesTypes.DEFRULE; }

}
<HEADER> {
	{NOT}    	                   {   return ItrulesTypes.NOT; }
	{OR}    	                   {   return ItrulesTypes.OR; }
	{AND} 	                       {   return ItrulesTypes.AND; }
  	{NEWLINE}					   {    yybegin(BODY); return ItrulesTypes.TEXT;}
	[^]                            { return ItrulesTypes.TEXT;}
	.                              { return ItrulesTypes.TEXT;}
}

<BODY> {
    "~end"                         {   return ItrulesTypes.TEXT;}
	{PLACEHOLDER}                  {   return ItrulesTypes.PLACEHOLDER; }
    {MULTIPLE}                     {   return ItrulesTypes.MULTIPLE; }
  	{FORMATTER}                    {   return ItrulesTypes.FORMATTER; }
	{LEFT_EXPR}                    {   return ItrulesTypes.LEFT_EXPR; }
	{RIGHT_EXPR}                   {   return ItrulesTypes.RIGHT_EXPR; }
	{SCAPED_CHAR}                  {   return ItrulesTypes.SCAPED_CHAR; }
    ~('\$' | '\r' | '\n' | '<')+   { return ItrulesTypes.TEXT;}
	{ENDRULE}                      {   yybegin(YYINITIAL); return ItrulesTypes.ENDRULE; }
	[^]                            { return ItrulesTypes.TEXT;}
	.                              { return ItrulesTypes.TEXT;}
}


[^]                                 { return ItrulesTypes.TEXT;}
.                                   { return ItrulesTypes.TEXT;}