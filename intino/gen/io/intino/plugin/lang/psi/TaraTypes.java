// This is a generated file. Not intended for manual editing.
package io.intino.plugin.lang.psi;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import io.intino.plugin.lang.psi.impl.*;

public interface TaraTypes {

  IElementType ANNOTATION = new TaraElementType("ANNOTATION");
  IElementType ANNOTATIONS = new TaraElementType("ANNOTATIONS");
  IElementType AN_IMPORT = new TaraElementType("AN_IMPORT");
  IElementType BODY = new TaraElementType("BODY");
  IElementType BODY_VALUE = new TaraElementType("BODY_VALUE");
  IElementType BOOLEAN_VALUE = new TaraElementType("BOOLEAN_VALUE");
  IElementType CONSTRAINT = new TaraElementType("CONSTRAINT");
  IElementType DOC = new TaraElementType("DOC");
  IElementType DOUBLE_VALUE = new TaraElementType("DOUBLE_VALUE");
  IElementType DSL_DECLARATION = new TaraElementType("DSL_DECLARATION");
  IElementType EMPTY_FIELD = new TaraElementType("EMPTY_FIELD");
  IElementType EXPRESSION = new TaraElementType("EXPRESSION");
  IElementType FACETS = new TaraElementType("FACETS");
  IElementType FACET_APPLY = new TaraElementType("FACET_APPLY");
  IElementType FLAG = new TaraElementType("FLAG");
  IElementType FLAGS = new TaraElementType("FLAGS");
  IElementType HEADER_REFERENCE = new TaraElementType("HEADER_REFERENCE");
  IElementType IDENTIFIER = new TaraElementType("IDENTIFIER");
  IElementType IDENTIFIER_REFERENCE = new TaraElementType("IDENTIFIER_REFERENCE");
  IElementType IMPORTS = new TaraElementType("IMPORTS");
  IElementType INTEGER_VALUE = new TaraElementType("INTEGER_VALUE");
  IElementType LIST_RANGE = new TaraElementType("LIST_RANGE");
  IElementType META_IDENTIFIER = new TaraElementType("META_IDENTIFIER");
  IElementType METHOD_REFERENCE = new TaraElementType("METHOD_REFERENCE");
  IElementType METRIC = new TaraElementType("METRIC");
  IElementType MOGRAM = new TaraElementType("MOGRAM");
  IElementType MOGRAM_REFERENCE = new TaraElementType("MOGRAM_REFERENCE");
  IElementType PARAMETER = new TaraElementType("PARAMETER");
  IElementType PARAMETERS = new TaraElementType("PARAMETERS");
  IElementType RANGE = new TaraElementType("RANGE");
  IElementType RULE = new TaraElementType("RULE");
  IElementType RULE_CONTAINER = new TaraElementType("RULE_CONTAINER");
  IElementType SIGNATURE = new TaraElementType("SIGNATURE");
  IElementType SIZE = new TaraElementType("SIZE");
  IElementType SIZE_RANGE = new TaraElementType("SIZE_RANGE");
  IElementType STRING_VALUE = new TaraElementType("STRING_VALUE");
  IElementType TAGS = new TaraElementType("TAGS");
  IElementType TUPLE_VALUE = new TaraElementType("TUPLE_VALUE");
  IElementType VALUE = new TaraElementType("VALUE");
  IElementType VARIABLE = new TaraElementType("VARIABLE");
  IElementType VARIABLE_TYPE = new TaraElementType("VARIABLE_TYPE");
  IElementType VAR_INIT = new TaraElementType("VAR_INIT");

  IElementType ABSTRACT = new TaraTokenType("ABSTRACT");
  IElementType AS = new TaraTokenType("AS");
  IElementType AT = new TaraTokenType("AT");
  IElementType BOOLEAN_TYPE = new TaraTokenType("BOOLEAN_TYPE");
  IElementType BOOLEAN_VALUE_KEY = new TaraTokenType("BOOLEAN_VALUE_KEY");
  IElementType CHARACTER = new TaraTokenType("CHARACTER");
  IElementType CLASS_TYPE = new TaraTokenType("CLASS_TYPE");
  IElementType COLON = new TaraTokenType("COLON");
  IElementType COMMA = new TaraTokenType("COMMA");
  IElementType COMMENT = new TaraTokenType("COMMENT");
  IElementType COMPONENT = new TaraTokenType("COMPONENT");
  IElementType CONCEPT = new TaraTokenType("CONCEPT");
  IElementType DATE_TYPE = new TaraTokenType("DATE_TYPE");
  IElementType DECORABLE = new TaraTokenType("DECORABLE");
  IElementType DEDENT = new TaraTokenType("DEDENT");
  IElementType DIVINE = new TaraTokenType("DIVINE");
  IElementType DOC_LINE = new TaraTokenType("DOC_LINE");
  IElementType DOT = new TaraTokenType("DOT");
  IElementType DOUBLE_TYPE = new TaraTokenType("DOUBLE_TYPE");
  IElementType DOUBLE_VALUE_KEY = new TaraTokenType("DOUBLE_VALUE_KEY");
  IElementType DSL = new TaraTokenType("DSL");
  IElementType EMPTY_REF = new TaraTokenType("EMPTY_REF");
  IElementType ENCLOSED = new TaraTokenType("ENCLOSED");
  IElementType EQUALS = new TaraTokenType("EQUALS");
  IElementType EXPRESSION_BEGIN = new TaraTokenType("EXPRESSION_BEGIN");
  IElementType EXPRESSION_END = new TaraTokenType("EXPRESSION_END");
  IElementType EXTENDS = new TaraTokenType("EXTENDS");
  IElementType FEATURE = new TaraTokenType("FEATURE");
  IElementType FINAL = new TaraTokenType("FINAL");
  IElementType FUNCTION_TYPE = new TaraTokenType("FUNCTION_TYPE");
  IElementType HAS = new TaraTokenType("HAS");
  IElementType IDENTIFIER_KEY = new TaraTokenType("IDENTIFIER_KEY");
  IElementType INLINE = new TaraTokenType("INLINE");
  IElementType INSTANT_TYPE = new TaraTokenType("INSTANT_TYPE");
  IElementType INTO = new TaraTokenType("INTO");
  IElementType INT_TYPE = new TaraTokenType("INT_TYPE");
  IElementType IS = new TaraTokenType("IS");
  IElementType LEFT_CURLY = new TaraTokenType("LEFT_CURLY");
  IElementType LEFT_PARENTHESIS = new TaraTokenType("LEFT_PARENTHESIS");
  IElementType LEFT_SQUARE = new TaraTokenType("LEFT_SQUARE");
  IElementType LONG_TYPE = new TaraTokenType("LONG_TYPE");
  IElementType METAIDENTIFIER_KEY = new TaraTokenType("METAIDENTIFIER_KEY");
  IElementType METRIC_VALUE_KEY = new TaraTokenType("METRIC_VALUE_KEY");
  IElementType NATURAL_VALUE_KEY = new TaraTokenType("NATURAL_VALUE_KEY");
  IElementType NEGATIVE_VALUE_KEY = new TaraTokenType("NEGATIVE_VALUE_KEY");
  IElementType NEWLINE = new TaraTokenType("NEWLINE");
  IElementType NEW_LINE_INDENT = new TaraTokenType("NEW_LINE_INDENT");
  IElementType OBJECT_TYPE = new TaraTokenType("OBJECT_TYPE");
  IElementType PLUS = new TaraTokenType("PLUS");
  IElementType PRIVATE = new TaraTokenType("PRIVATE");
  IElementType QUOTE_BEGIN = new TaraTokenType("QUOTE_BEGIN");
  IElementType QUOTE_END = new TaraTokenType("QUOTE_END");
  IElementType REACTIVE = new TaraTokenType("REACTIVE");
  IElementType REQUIRED = new TaraTokenType("REQUIRED");
  IElementType RESOURCE_TYPE = new TaraTokenType("RESOURCE_TYPE");
  IElementType RIGHT_CURLY = new TaraTokenType("RIGHT_CURLY");
  IElementType RIGHT_PARENTHESIS = new TaraTokenType("RIGHT_PARENTHESIS");
  IElementType RIGHT_SQUARE = new TaraTokenType("RIGHT_SQUARE");
  IElementType STAR = new TaraTokenType("STAR");
  IElementType STRING_TYPE = new TaraTokenType("STRING_TYPE");
  IElementType SUB = new TaraTokenType("SUB");
  IElementType TERMINAL = new TaraTokenType("TERMINAL");
  IElementType TIME_TYPE = new TaraTokenType("TIME_TYPE");
  IElementType USE = new TaraTokenType("USE");
  IElementType VAR = new TaraTokenType("VAR");
  IElementType VOLATILE = new TaraTokenType("VOLATILE");
  IElementType WITH = new TaraTokenType("WITH");
  IElementType WORD_TYPE = new TaraTokenType("WORD_TYPE");

  class Factory {
    public static PsiElement createElement(ASTNode node) {
      IElementType type = node.getElementType();
      if (type == ANNOTATION) {
        return new TaraAnnotationImpl(node);
      }
      else if (type == ANNOTATIONS) {
        return new TaraAnnotationsImpl(node);
      }
      else if (type == AN_IMPORT) {
        return new TaraAnImportImpl(node);
      }
      else if (type == BODY) {
        return new TaraBodyImpl(node);
      }
      else if (type == BODY_VALUE) {
        return new TaraBodyValueImpl(node);
      }
      else if (type == BOOLEAN_VALUE) {
        return new TaraBooleanValueImpl(node);
      }
      else if (type == CONSTRAINT) {
        return new TaraConstraintImpl(node);
      }
      else if (type == DOC) {
        return new TaraDocImpl(node);
      }
      else if (type == DOUBLE_VALUE) {
        return new TaraDoubleValueImpl(node);
      }
      else if (type == DSL_DECLARATION) {
        return new TaraDslDeclarationImpl(node);
      }
      else if (type == EMPTY_FIELD) {
        return new TaraEmptyFieldImpl(node);
      }
      else if (type == EXPRESSION) {
        return new TaraExpressionImpl(node);
      }
      else if (type == FACETS) {
        return new TaraFacetsImpl(node);
      }
      else if (type == FACET_APPLY) {
        return new TaraFacetApplyImpl(node);
      }
      else if (type == FLAG) {
        return new TaraFlagImpl(node);
      }
      else if (type == FLAGS) {
        return new TaraFlagsImpl(node);
      }
      else if (type == HEADER_REFERENCE) {
        return new TaraHeaderReferenceImpl(node);
      }
      else if (type == IDENTIFIER) {
        return new TaraIdentifierImpl(node);
      }
      else if (type == IDENTIFIER_REFERENCE) {
        return new TaraIdentifierReferenceImpl(node);
      }
      else if (type == IMPORTS) {
        return new TaraImportsImpl(node);
      }
      else if (type == INTEGER_VALUE) {
        return new TaraIntegerValueImpl(node);
      }
      else if (type == LIST_RANGE) {
        return new TaraListRangeImpl(node);
      }
      else if (type == META_IDENTIFIER) {
        return new TaraMetaIdentifierImpl(node);
      }
      else if (type == METHOD_REFERENCE) {
        return new TaraMethodReferenceImpl(node);
      }
      else if (type == METRIC) {
        return new TaraMetricImpl(node);
      }
      else if (type == MOGRAM) {
        return new TaraMogramImpl(node);
      }
      else if (type == MOGRAM_REFERENCE) {
        return new TaraMogramReferenceImpl(node);
      }
      else if (type == PARAMETER) {
        return new TaraParameterImpl(node);
      }
      else if (type == PARAMETERS) {
        return new TaraParametersImpl(node);
      }
      else if (type == RANGE) {
        return new TaraRangeImpl(node);
      }
      else if (type == RULE) {
        return new TaraRuleImpl(node);
      }
      else if (type == RULE_CONTAINER) {
        return new TaraRuleContainerImpl(node);
      }
      else if (type == SIGNATURE) {
        return new TaraSignatureImpl(node);
      }
      else if (type == SIZE) {
        return new TaraSizeImpl(node);
      }
      else if (type == SIZE_RANGE) {
        return new TaraSizeRangeImpl(node);
      }
      else if (type == STRING_VALUE) {
        return new TaraStringValueImpl(node);
      }
      else if (type == TAGS) {
        return new TaraTagsImpl(node);
      }
      else if (type == TUPLE_VALUE) {
        return new TaraTupleValueImpl(node);
      }
      else if (type == VALUE) {
        return new TaraValueImpl(node);
      }
      else if (type == VARIABLE) {
        return new TaraVariableImpl(node);
      }
      else if (type == VARIABLE_TYPE) {
        return new TaraVariableTypeImpl(node);
      }
      else if (type == VAR_INIT) {
        return new TaraVarInitImpl(node);
      }
      throw new AssertionError("Unknown element type: " + type);
    }
  }
}
