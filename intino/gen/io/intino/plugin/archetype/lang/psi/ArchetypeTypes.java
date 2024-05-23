// This is a generated file. Not intended for manual editing.
package io.intino.plugin.archetype.lang.psi;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import io.intino.plugin.archetype.lang.psi.impl.ArchetypeStringValueImpl;

public interface ArchetypeTypes {

  IElementType STRING_VALUE = new ArchetypeElementType("STRING_VALUE");

  IElementType ANNOTATION = new ArchetypeTokenType("ANNOTATION");
  IElementType CHARACTER = new ArchetypeTokenType("CHARACTER");
  IElementType COMMENT = new ArchetypeTokenType("COMMENT");
  IElementType IDENTIFIER_KEY = new ArchetypeTokenType("IDENTIFIER_KEY");
  IElementType KEYWORD = new ArchetypeTokenType("KEYWORD");
  IElementType LEFT_PARENTHESIS = new ArchetypeTokenType("LEFT_PARENTHESIS");
  IElementType LEFT_SQUARE = new ArchetypeTokenType("LEFT_SQUARE");
  IElementType MINUS = new ArchetypeTokenType("MINUS");
  IElementType NEWLINE = new ArchetypeTokenType("NEWLINE");
  IElementType PLUS = new ArchetypeTokenType("PLUS");
  IElementType QUOTE_BEGIN = new ArchetypeTokenType("QUOTE_BEGIN");
  IElementType QUOTE_END = new ArchetypeTokenType("QUOTE_END");
  IElementType RIGHT_PARENTHESIS = new ArchetypeTokenType("RIGHT_PARENTHESIS");
  IElementType RIGHT_SQUARE = new ArchetypeTokenType("RIGHT_SQUARE");
  IElementType STAR = new ArchetypeTokenType("STAR");
  IElementType TEXT = new ArchetypeTokenType("TEXT");

  class Factory {
    public static PsiElement createElement(ASTNode node) {
      IElementType type = node.getElementType();
      if (type == STRING_VALUE) {
        return new ArchetypeStringValueImpl(node);
      }
      throw new AssertionError("Unknown element type: " + type);
    }
  }
}
