package io.intino.plugin.itrules.lang.lexer;

import com.intellij.lexer.FlexLexer;
import com.intellij.psi.tree.IElementType;
import io.intino.plugin.itrules.lang.psi.ItrulesTypes;

%%

%class ItrulesLexer
%implements FlexLexer
%unicode
%function advance
%type IElementType


SCAPED_CHAR         = "$$"| "$[" | "$]"
DEFRULE             = "def"
ENDRULE             = "end"
DASH                = "-"
UNDERDASH           = "_"
MARK                = "$" {IDENTIFIER_KEY}
FORMATTER           = "+" {IDENTIFIER_KEY}
LEFT_SQUARE         = "["
RIGHT_SQUARE        = "]"
IDENTIFIER_KEY      = [:jletter:] ([:jletterdigit:] | {UNDERDASH} | {DASH})*

%%
<YYINITIAL> {
	{DEFRULE}                       {   return ItrulesTypes.DEFRULE; }
	{ENDRULE}                       {   return ItrulesTypes.ENDRULE; }
	{MARK}                          {   return ItrulesTypes.MARK; }
  	{FORMATTER}                     {   return ItrulesTypes.FORMATTER; }
	{LEFT_SQUARE}                   {   return ItrulesTypes.LEFT_SQUARE; }
	{RIGHT_SQUARE}                  {   return ItrulesTypes.RIGHT_SQUARE; }
	{SCAPED_CHAR}                   {   return ItrulesTypes.SCAPED_CHAR; }
}

[^]                                 { return ItrulesTypes.TEXT;}
.                                   { return ItrulesTypes.TEXT;}