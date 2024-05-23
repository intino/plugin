package io.intino.plugin.itrules.lang.lexer;

import com.intellij.psi.tree.IElementType;
import io.intino.plugin.itrules.lang.psi.ItrulesTypes;

%%

%class ItrulesLexer
%implements FlexLexer
%unicode
%function advance
%type IElementType


SCAPED_CHAR         = "$$" | "$<<" | "$>>"
DEFRULE             = "def"
ENDRULE             = "end"
OR                  = "OR"
AND                 = "AND"
DASH                = "-"
UNDERDASH           = "_"
MARK                = "$" {IDENTIFIER_KEY}
FORMATTER           = "+" {IDENTIFIER_KEY}
LEFT_EXPR           = "<<"
RIGHT_EXPR          = ">>"
IDENTIFIER_KEY      = [:jletter:] ([:jletterdigit:] | {UNDERDASH} | {DASH})*

%%
<YYINITIAL> {
	{DEFRULE}                      {   return ItrulesTypes.DEFRULE; }
	{ENDRULE}                      {   return ItrulesTypes.ENDRULE; }
	{OR}    	                   {   return ItrulesTypes.OR; }
	{AND} 	                       {   return ItrulesTypes.AND; }
	{MARK}                         {   return ItrulesTypes.MARK; }
  	{FORMATTER}                    {   return ItrulesTypes.FORMATTER; }
	{LEFT_EXPR}                    {   return ItrulesTypes.LEFT_EXPR; }
	{RIGHT_EXPR}                   {   return ItrulesTypes.RIGHT_EXPR; }
	{SCAPED_CHAR}                  {   return ItrulesTypes.SCAPED_CHAR; }
}

[^]                                 { return ItrulesTypes.TEXT;}
.                                   { return ItrulesTypes.TEXT;}