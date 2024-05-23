// This is a generated file. Not intended for manual editing.
package io.intino.plugin.archetype.lang.parser;

import com.intellij.lang.ASTNode;
import com.intellij.lang.LightPsiParser;
import com.intellij.lang.PsiBuilder;
import com.intellij.lang.PsiBuilder.Marker;
import com.intellij.lang.PsiParser;
import com.intellij.psi.tree.IElementType;

import static io.intino.plugin.archetype.lang.parser.ArchetypeParserUtil.*;
import static io.intino.plugin.archetype.lang.psi.ArchetypeTypes.*;

@SuppressWarnings({"SimplifiableIfStatement", "UnusedAssignment"})
public class ArchetypeParser implements PsiParser, LightPsiParser {

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
  // (
  //         NEWLINE
  //         | LEFT_PARENTHESIS
  //         | RIGHT_PARENTHESIS
  //         | LEFT_SQUARE
  //         | RIGHT_SQUARE
  //         | MINUS
  //         | PLUS
  //         | STAR
  //         | stringValue
  //       	| ANNOTATION
  //       	| KEYWORD
  //         | COMMENT
  //         | IDENTIFIER_KEY
  //         | TEXT)*
  static boolean root(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "root")) return false;
    while (true) {
      int c = current_position_(b);
      if (!root_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "root", c)) break;
    }
    return true;
  }

  // NEWLINE
  //         | LEFT_PARENTHESIS
  //         | RIGHT_PARENTHESIS
  //         | LEFT_SQUARE
  //         | RIGHT_SQUARE
  //         | MINUS
  //         | PLUS
  //         | STAR
  //         | stringValue
  //       	| ANNOTATION
  //       	| KEYWORD
  //         | COMMENT
  //         | IDENTIFIER_KEY
  //         | TEXT
  private static boolean root_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "root_0")) return false;
    boolean r;
    r = consumeToken(b, NEWLINE);
    if (!r) r = consumeToken(b, LEFT_PARENTHESIS);
    if (!r) r = consumeToken(b, RIGHT_PARENTHESIS);
    if (!r) r = consumeToken(b, LEFT_SQUARE);
    if (!r) r = consumeToken(b, RIGHT_SQUARE);
    if (!r) r = consumeToken(b, MINUS);
    if (!r) r = consumeToken(b, PLUS);
    if (!r) r = consumeToken(b, STAR);
    if (!r) r = stringValue(b, l + 1);
    if (!r) r = consumeToken(b, ANNOTATION);
    if (!r) r = consumeToken(b, KEYWORD);
    if (!r) r = consumeToken(b, COMMENT);
    if (!r) r = consumeToken(b, IDENTIFIER_KEY);
    if (!r) r = consumeToken(b, TEXT);
    return r;
  }

  /* ********************************************************** */
  // QUOTE_BEGIN CHARACTER* QUOTE_END
  public static boolean stringValue(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "stringValue")) return false;
    if (!nextTokenIs(b, QUOTE_BEGIN)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, QUOTE_BEGIN);
    r = r && stringValue_1(b, l + 1);
    r = r && consumeToken(b, QUOTE_END);
    exit_section_(b, m, STRING_VALUE, r);
    return r;
  }

  // CHARACTER*
  private static boolean stringValue_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "stringValue_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!consumeToken(b, CHARACTER)) break;
      if (!empty_element_parsed_guard_(b, "stringValue_1", c)) break;
    }
    return true;
  }

}
