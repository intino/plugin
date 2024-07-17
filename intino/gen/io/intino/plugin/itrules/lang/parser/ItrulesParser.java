// This is a generated file. Not intended for manual editing.
package io.intino.plugin.itrules.lang.parser;

import com.intellij.lang.ASTNode;
import com.intellij.lang.LightPsiParser;
import com.intellij.lang.PsiBuilder;
import com.intellij.lang.PsiBuilder.Marker;
import com.intellij.lang.PsiParser;
import com.intellij.psi.tree.IElementType;

import static io.intino.plugin.itrules.lang.parser.ItrulesParserUtil.*;
import static io.intino.plugin.itrules.lang.psi.ItrulesTypes.*;

@SuppressWarnings({"SimplifiableIfStatement", "UnusedAssignment"})
public class ItrulesParser implements PsiParser, LightPsiParser {

  public ASTNode parse(IElementType t, PsiBuilder b) {
    parseLight(t, b);
    return b.getTreeBuilt();
  }

  public void parseLight(IElementType t, PsiBuilder b) {
    boolean r;
    b = adapt_builder_(t, b, this, null);
    Marker m = enter_section_(b, 0, _COLLAPSE_, null);
    r = parse_root_(t, b);
    exit_section_(b, 0, m, t, r, true, TRUE_CONDITION);
  }

  protected boolean parse_root_(IElementType t, PsiBuilder b) {
    return parse_root_(t, b, 0);
  }

  static boolean parse_root_(IElementType t, PsiBuilder b, int l) {
    return root(b, l + 1);
  }

  /* ********************************************************** */
  // (SCAPED_CHAR
  //          | DEFRULE
  //          | DASH
  //          | OR
  //          | AND
  //          | NOT
  //          | UNDERDASH
  //          | PLACEHOLDER
  //          | FORMATTER
  //          | LEFT_EXPR
  //          | RIGHT_EXPR
  //          | IDENTIFIER_KEY
  //          | TEXT)*
  static boolean root(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "root")) return false;
    while (true) {
      int c = current_position_(b);
      if (!root_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "root", c)) break;
    }
    return true;
  }

  // SCAPED_CHAR
  //          | DEFRULE
  //          | DASH
  //          | OR
  //          | AND
  //          | NOT
  //          | UNDERDASH
  //          | PLACEHOLDER
  //          | FORMATTER
  //          | LEFT_EXPR
  //          | RIGHT_EXPR
  //          | IDENTIFIER_KEY
  //          | TEXT
  private static boolean root_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "root_0")) return false;
    boolean r;
    r = consumeToken(b, SCAPED_CHAR);
    if (!r) r = consumeToken(b, DEFRULE);
    if (!r) r = consumeToken(b, DASH);
    if (!r) r = consumeToken(b, OR);
    if (!r) r = consumeToken(b, AND);
    if (!r) r = consumeToken(b, NOT);
    if (!r) r = consumeToken(b, UNDERDASH);
    if (!r) r = consumeToken(b, PLACEHOLDER);
    if (!r) r = consumeToken(b, FORMATTER);
    if (!r) r = consumeToken(b, LEFT_EXPR);
    if (!r) r = consumeToken(b, RIGHT_EXPR);
    if (!r) r = consumeToken(b, IDENTIFIER_KEY);
    if (!r) r = consumeToken(b, TEXT);
    return r;
  }

}
