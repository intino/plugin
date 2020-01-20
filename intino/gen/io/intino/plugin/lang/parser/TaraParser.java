// This is a generated file. Not intended for manual editing.
package io.intino.plugin.lang.parser;

import com.intellij.lang.ASTNode;
import com.intellij.lang.LightPsiParser;
import com.intellij.lang.PsiBuilder;
import com.intellij.lang.PsiBuilder.Marker;
import com.intellij.lang.PsiParser;
import com.intellij.psi.tree.IElementType;

import static io.intino.plugin.lang.parser.GeneratedParserUtilBase.*;
import static io.intino.plugin.lang.psi.TaraTypes.*;

@SuppressWarnings({"SimplifiableIfStatement", "UnusedAssignment"})
public class TaraParser implements PsiParser, LightPsiParser {

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
	// USE headerReference NEWLINE
	public static boolean anImport(PsiBuilder b, int l) {
		if (!recursion_guard_(b, l, "anImport")) return false;
		if (!nextTokenIs(b, USE)) return false;
		boolean r;
		Marker m = enter_section_(b);
		r = consumeToken(b, USE);
		r = r && headerReference(b, l + 1);
		r = r && consumeToken(b, NEWLINE);
		exit_section_(b, m, AN_IMPORT, r);
		return r;
	}

	/* ********************************************************** */
	// COMPONENT | FEATURE | ENCLOSED
	public static boolean annotation(PsiBuilder b, int l) {
		if (!recursion_guard_(b, l, "annotation")) return false;
		boolean r;
		Marker m = enter_section_(b, l, _NONE_, ANNOTATION, "<annotation>");
		r = consumeToken(b, COMPONENT);
		if (!r) r = consumeToken(b, FEATURE);
		if (!r) r = consumeToken(b, ENCLOSED);
		exit_section_(b, l, m, r, false, null);
		return r;
	}

	/* ********************************************************** */
	// INTO annotation+
	public static boolean annotations(PsiBuilder b, int l) {
		if (!recursion_guard_(b, l, "annotations")) return false;
		if (!nextTokenIs(b, INTO)) return false;
		boolean r, p;
		Marker m = enter_section_(b, l, _NONE_, ANNOTATIONS, null);
		r = consumeToken(b, INTO);
		p = r; // pin = 1
		r = r && annotations_1(b, l + 1);
		exit_section_(b, l, m, r, p, null);
		return r || p;
	}

	// annotation+
	private static boolean annotations_1(PsiBuilder b, int l) {
		if (!recursion_guard_(b, l, "annotations_1")) return false;
		boolean r;
		Marker m = enter_section_(b);
		r = annotation(b, l + 1);
		while (r) {
			int c = current_position_(b);
			if (!annotation(b, l + 1)) break;
			if (!empty_element_parsed_guard_(b, "annotations_1", c)) break;
		}
		exit_section_(b, m, null, r);
		return r;
	}

	/* ********************************************************** */
	// metaIdentifier parameters?
	public static boolean aspectApply(PsiBuilder b, int l) {
		if (!recursion_guard_(b, l, "aspectApply")) return false;
		if (!nextTokenIs(b, "<aspect apply>", IDENTIFIER_KEY, METAIDENTIFIER_KEY)) return false;
		boolean r;
		Marker m = enter_section_(b, l, _NONE_, ASPECT_APPLY, "<aspect apply>");
		r = metaIdentifier(b, l + 1);
		r = r && aspectApply_1(b, l + 1);
		exit_section_(b, l, m, r, false, null);
		return r;
	}

	// parameters?
	private static boolean aspectApply_1(PsiBuilder b, int l) {
		if (!recursion_guard_(b, l, "aspectApply_1")) return false;
		parameters(b, l + 1);
		return true;
	}

	/* ********************************************************** */
	// AS aspectApply+
	public static boolean aspects(PsiBuilder b, int l) {
		if (!recursion_guard_(b, l, "aspects")) return false;
		if (!nextTokenIs(b, AS)) return false;
		boolean r, p;
		Marker m = enter_section_(b, l, _NONE_, ASPECTS, null);
		r = consumeToken(b, AS);
		p = r; // pin = 1
		r = r && aspects_1(b, l + 1);
		exit_section_(b, l, m, r, p, null);
		return r || p;
	}

	// aspectApply+
	private static boolean aspects_1(PsiBuilder b, int l) {
		if (!recursion_guard_(b, l, "aspects_1")) return false;
		boolean r;
		Marker m = enter_section_(b);
		r = aspectApply(b, l + 1);
		while (r) {
			int c = current_position_(b);
			if (!aspectApply(b, l + 1)) break;
			if (!empty_element_parsed_guard_(b, "aspects_1", c)) break;
		}
		exit_section_(b, m, null, r);
		return r;
	}

	/* ********************************************************** */
	// (NEW_LINE_INDENT NEWLINE? | INLINE) (nodeConstituents NEWLINE+)+ DEDENT
	public static boolean body(PsiBuilder b, int l) {
		if (!recursion_guard_(b, l, "body")) return false;
		if (!nextTokenIs(b, "<body>", INLINE, NEW_LINE_INDENT)) return false;
		boolean r, p;
		Marker m = enter_section_(b, l, _NONE_, BODY, "<body>");
		r = body_0(b, l + 1);
		p = r; // pin = 1
		r = r && report_error_(b, body_1(b, l + 1));
		r = p && consumeToken(b, DEDENT) && r;
		exit_section_(b, l, m, r, p, null);
		return r || p;
	}

	// NEW_LINE_INDENT NEWLINE? | INLINE
	private static boolean body_0(PsiBuilder b, int l) {
		if (!recursion_guard_(b, l, "body_0")) return false;
		boolean r;
		Marker m = enter_section_(b);
		r = body_0_0(b, l + 1);
		if (!r) r = consumeToken(b, INLINE);
		exit_section_(b, m, null, r);
		return r;
	}

	// NEW_LINE_INDENT NEWLINE?
	private static boolean body_0_0(PsiBuilder b, int l) {
		if (!recursion_guard_(b, l, "body_0_0")) return false;
		boolean r;
		Marker m = enter_section_(b);
		r = consumeToken(b, NEW_LINE_INDENT);
		r = r && body_0_0_1(b, l + 1);
		exit_section_(b, m, null, r);
		return r;
	}

	// NEWLINE?
	private static boolean body_0_0_1(PsiBuilder b, int l) {
		if (!recursion_guard_(b, l, "body_0_0_1")) return false;
		consumeToken(b, NEWLINE);
		return true;
	}

	// (nodeConstituents NEWLINE+)+
	private static boolean body_1(PsiBuilder b, int l) {
		if (!recursion_guard_(b, l, "body_1")) return false;
		boolean r;
		Marker m = enter_section_(b);
		r = body_1_0(b, l + 1);
		while (r) {
			int c = current_position_(b);
			if (!body_1_0(b, l + 1)) break;
			if (!empty_element_parsed_guard_(b, "body_1", c)) break;
		}
		exit_section_(b, m, null, r);
		return r;
	}

	// nodeConstituents NEWLINE+
	private static boolean body_1_0(PsiBuilder b, int l) {
		if (!recursion_guard_(b, l, "body_1_0")) return false;
		boolean r;
		Marker m = enter_section_(b);
		r = nodeConstituents(b, l + 1);
		r = r && body_1_0_1(b, l + 1);
		exit_section_(b, m, null, r);
		return r;
	}

	// NEWLINE+
	private static boolean body_1_0_1(PsiBuilder b, int l) {
		if (!recursion_guard_(b, l, "body_1_0_1")) return false;
		boolean r;
		Marker m = enter_section_(b);
		r = consumeToken(b, NEWLINE);
		while (r) {
			int c = current_position_(b);
			if (!consumeToken(b, NEWLINE)) break;
			if (!empty_element_parsed_guard_(b, "body_1_0_1", c)) break;
		}
		exit_section_(b, m, null, r);
		return r;
	}

	/* ********************************************************** */
	// NEW_LINE_INDENT (stringValue | expression) NEWLINE? DEDENT
	public static boolean bodyValue(PsiBuilder b, int l) {
		if (!recursion_guard_(b, l, "bodyValue")) return false;
		if (!nextTokenIs(b, NEW_LINE_INDENT)) return false;
		boolean r;
		Marker m = enter_section_(b);
		r = consumeToken(b, NEW_LINE_INDENT);
		r = r && bodyValue_1(b, l + 1);
		r = r && bodyValue_2(b, l + 1);
		r = r && consumeToken(b, DEDENT);
		exit_section_(b, m, BODY_VALUE, r);
		return r;
	}

	// stringValue | expression
	private static boolean bodyValue_1(PsiBuilder b, int l) {
		if (!recursion_guard_(b, l, "bodyValue_1")) return false;
		boolean r;
		r = stringValue(b, l + 1);
		if (!r) r = expression(b, l + 1);
		return r;
	}

	// NEWLINE?
	private static boolean bodyValue_2(PsiBuilder b, int l) {
		if (!recursion_guard_(b, l, "bodyValue_2")) return false;
		consumeToken(b, NEWLINE);
		return true;
	}

	/* ********************************************************** */
	// BOOLEAN_VALUE_KEY
	public static boolean booleanValue(PsiBuilder b, int l) {
		if (!recursion_guard_(b, l, "booleanValue")) return false;
		if (!nextTokenIs(b, BOOLEAN_VALUE_KEY)) return false;
		boolean r;
		Marker m = enter_section_(b);
		r = consumeToken(b, BOOLEAN_VALUE_KEY);
		exit_section_(b, m, BOOLEAN_VALUE, r);
		return r;
	}

	/* ********************************************************** */
	// WITH identifierReference (COMMA identifierReference)*
	public static boolean constraint(PsiBuilder b, int l) {
		if (!recursion_guard_(b, l, "constraint")) return false;
		if (!nextTokenIs(b, WITH)) return false;
		boolean r, p;
		Marker m = enter_section_(b, l, _NONE_, CONSTRAINT, null);
		r = consumeToken(b, WITH);
		p = r; // pin = 1
		r = r && report_error_(b, identifierReference(b, l + 1));
		r = p && constraint_2(b, l + 1) && r;
		exit_section_(b, l, m, r, p, null);
		return r || p;
	}

	// (COMMA identifierReference)*
	private static boolean constraint_2(PsiBuilder b, int l) {
		if (!recursion_guard_(b, l, "constraint_2")) return false;
		while (true) {
			int c = current_position_(b);
			if (!constraint_2_0(b, l + 1)) break;
			if (!empty_element_parsed_guard_(b, "constraint_2", c)) break;
		}
		return true;
	}

	// COMMA identifierReference
	private static boolean constraint_2_0(PsiBuilder b, int l) {
		if (!recursion_guard_(b, l, "constraint_2_0")) return false;
		boolean r;
		Marker m = enter_section_(b);
		r = consumeToken(b, COMMA);
		r = r && identifierReference(b, l + 1);
		exit_section_(b, m, null, r);
		return r;
	}

	/* ********************************************************** */
	// (DOC_LINE NEWLINE?)+
	public static boolean doc(PsiBuilder b, int l) {
		if (!recursion_guard_(b, l, "doc")) return false;
		if (!nextTokenIs(b, DOC_LINE)) return false;
		boolean r;
		Marker m = enter_section_(b);
		r = doc_0(b, l + 1);
		while (r) {
			int c = current_position_(b);
			if (!doc_0(b, l + 1)) break;
			if (!empty_element_parsed_guard_(b, "doc", c)) break;
		}
		exit_section_(b, m, DOC, r);
		return r;
	}

	// DOC_LINE NEWLINE?
	private static boolean doc_0(PsiBuilder b, int l) {
		if (!recursion_guard_(b, l, "doc_0")) return false;
		boolean r;
		Marker m = enter_section_(b);
		r = consumeToken(b, DOC_LINE);
		r = r && doc_0_1(b, l + 1);
		exit_section_(b, m, null, r);
		return r;
	}

	// NEWLINE?
	private static boolean doc_0_1(PsiBuilder b, int l) {
		if (!recursion_guard_(b, l, "doc_0_1")) return false;
		consumeToken(b, NEWLINE);
		return true;
	}

	/* ********************************************************** */
	// NATURAL_VALUE_KEY | NEGATIVE_VALUE_KEY | DOUBLE_VALUE_KEY
	public static boolean doubleValue(PsiBuilder b, int l) {
		if (!recursion_guard_(b, l, "doubleValue")) return false;
		boolean r;
		Marker m = enter_section_(b, l, _NONE_, DOUBLE_VALUE, "<double value>");
		r = consumeToken(b, NATURAL_VALUE_KEY);
		if (!r) r = consumeToken(b, NEGATIVE_VALUE_KEY);
		if (!r) r = consumeToken(b, DOUBLE_VALUE_KEY);
		exit_section_(b, l, m, r, false, null);
		return r;
	}

	/* ********************************************************** */
	// DSL headerReference
	public static boolean dslDeclaration(PsiBuilder b, int l) {
		if (!recursion_guard_(b, l, "dslDeclaration")) return false;
		if (!nextTokenIs(b, DSL)) return false;
		boolean r;
		Marker m = enter_section_(b);
		r = consumeToken(b, DSL);
		r = r && headerReference(b, l + 1);
		exit_section_(b, m, DSL_DECLARATION, r);
		return r;
	}

	/* ********************************************************** */
	// EMPTY_REF
	public static boolean emptyField(PsiBuilder b, int l) {
		if (!recursion_guard_(b, l, "emptyField")) return false;
		if (!nextTokenIs(b, EMPTY_REF)) return false;
		boolean r;
		Marker m = enter_section_(b);
		r = consumeToken(b, EMPTY_REF);
		exit_section_(b, m, EMPTY_FIELD, r);
		return r;
	}

	/* ********************************************************** */
	// EXPRESSION_BEGIN CHARACTER* EXPRESSION_END
	public static boolean expression(PsiBuilder b, int l) {
		if (!recursion_guard_(b, l, "expression")) return false;
		if (!nextTokenIs(b, EXPRESSION_BEGIN)) return false;
		boolean r;
		Marker m = enter_section_(b);
		r = consumeToken(b, EXPRESSION_BEGIN);
		r = r && expression_1(b, l + 1);
		r = r && consumeToken(b, EXPRESSION_END);
		exit_section_(b, m, EXPRESSION, r);
		return r;
	}

	// CHARACTER*
	private static boolean expression_1(PsiBuilder b, int l) {
		if (!recursion_guard_(b, l, "expression_1")) return false;
		while (true) {
			int c = current_position_(b);
			if (!consumeToken(b, CHARACTER)) break;
			if (!empty_element_parsed_guard_(b, "expression_1", c)) break;
		}
		return true;
	}

	/* ********************************************************** */
	// ABSTRACT | TERMINAL | PRIVATE | REACTIVE | COMPONENT
	// 	| FEATURE | ENCLOSED | FINAL | CONCEPT | VOLATILE | REQUIRED | DECORABLE | DIVINE
	public static boolean flag(PsiBuilder b, int l) {
		if (!recursion_guard_(b, l, "flag")) return false;
		boolean r;
		Marker m = enter_section_(b, l, _NONE_, FLAG, "<flag>");
		r = consumeToken(b, ABSTRACT);
		if (!r) r = consumeToken(b, TERMINAL);
		if (!r) r = consumeToken(b, PRIVATE);
		if (!r) r = consumeToken(b, REACTIVE);
		if (!r) r = consumeToken(b, COMPONENT);
		if (!r) r = consumeToken(b, FEATURE);
		if (!r) r = consumeToken(b, ENCLOSED);
		if (!r) r = consumeToken(b, FINAL);
		if (!r) r = consumeToken(b, CONCEPT);
		if (!r) r = consumeToken(b, VOLATILE);
		if (!r) r = consumeToken(b, REQUIRED);
		if (!r) r = consumeToken(b, DECORABLE);
		if (!r) r = consumeToken(b, DIVINE);
		exit_section_(b, l, m, r, false, null);
		return r;
	}

	/* ********************************************************** */
	// IS flag+
	public static boolean flags(PsiBuilder b, int l) {
		if (!recursion_guard_(b, l, "flags")) return false;
		if (!nextTokenIs(b, IS)) return false;
		boolean r, p;
		Marker m = enter_section_(b, l, _NONE_, FLAGS, null);
		r = consumeToken(b, IS);
		p = r; // pin = 1
		r = r && flags_1(b, l + 1);
		exit_section_(b, l, m, r, p, null);
		return r || p;
	}

	// flag+
	private static boolean flags_1(PsiBuilder b, int l) {
		if (!recursion_guard_(b, l, "flags_1")) return false;
		boolean r;
		Marker m = enter_section_(b);
		r = flag(b, l + 1);
		while (r) {
			int c = current_position_(b);
			if (!flag(b, l + 1)) break;
			if (!empty_element_parsed_guard_(b, "flags_1", c)) break;
		}
		exit_section_(b, m, null, r);
		return r;
	}

	/* ********************************************************** */
	// hierarchy* identifier
	public static boolean headerReference(PsiBuilder b, int l) {
		if (!recursion_guard_(b, l, "headerReference")) return false;
		if (!nextTokenIs(b, IDENTIFIER_KEY)) return false;
		boolean r;
		Marker m = enter_section_(b);
		r = headerReference_0(b, l + 1);
		r = r && identifier(b, l + 1);
		exit_section_(b, m, HEADER_REFERENCE, r);
		return r;
	}

	// hierarchy*
	private static boolean headerReference_0(PsiBuilder b, int l) {
		if (!recursion_guard_(b, l, "headerReference_0")) return false;
		while (true) {
			int c = current_position_(b);
			if (!hierarchy(b, l + 1)) break;
			if (!empty_element_parsed_guard_(b, "headerReference_0", c)) break;
		}
		return true;
	}

	/* ********************************************************** */
	// identifier (DOT | PLUS)
	static boolean hierarchy(PsiBuilder b, int l) {
		if (!recursion_guard_(b, l, "hierarchy")) return false;
		if (!nextTokenIs(b, IDENTIFIER_KEY)) return false;
		boolean r;
		Marker m = enter_section_(b);
		r = identifier(b, l + 1);
		r = r && hierarchy_1(b, l + 1);
		exit_section_(b, m, null, r);
		return r;
	}

	// DOT | PLUS
	private static boolean hierarchy_1(PsiBuilder b, int l) {
		if (!recursion_guard_(b, l, "hierarchy_1")) return false;
		boolean r;
		r = consumeToken(b, DOT);
		if (!r) r = consumeToken(b, PLUS);
		return r;
	}

	/* ********************************************************** */
	// IDENTIFIER_KEY
	public static boolean identifier(PsiBuilder b, int l) {
		if (!recursion_guard_(b, l, "identifier")) return false;
		if (!nextTokenIs(b, IDENTIFIER_KEY)) return false;
		boolean r;
		Marker m = enter_section_(b);
		r = consumeToken(b, IDENTIFIER_KEY);
		exit_section_(b, m, IDENTIFIER, r);
		return r;
	}

	/* ********************************************************** */
	// hierarchy* identifier
	public static boolean identifierReference(PsiBuilder b, int l) {
		if (!recursion_guard_(b, l, "identifierReference")) return false;
		if (!nextTokenIs(b, IDENTIFIER_KEY)) return false;
		boolean r;
		Marker m = enter_section_(b);
		r = identifierReference_0(b, l + 1);
		r = r && identifier(b, l + 1);
		exit_section_(b, m, IDENTIFIER_REFERENCE, r);
		return r;
	}

	// hierarchy*
	private static boolean identifierReference_0(PsiBuilder b, int l) {
		if (!recursion_guard_(b, l, "identifierReference_0")) return false;
		while (true) {
			int c = current_position_(b);
			if (!hierarchy(b, l + 1)) break;
			if (!empty_element_parsed_guard_(b, "identifierReference_0", c)) break;
		}
		return true;
	}

	/* ********************************************************** */
	// (anImport NEWLINE*)+
	public static boolean imports(PsiBuilder b, int l) {
		if (!recursion_guard_(b, l, "imports")) return false;
		if (!nextTokenIs(b, USE)) return false;
		boolean r;
		Marker m = enter_section_(b);
		r = imports_0(b, l + 1);
		while (r) {
			int c = current_position_(b);
			if (!imports_0(b, l + 1)) break;
			if (!empty_element_parsed_guard_(b, "imports", c)) break;
		}
		exit_section_(b, m, IMPORTS, r);
		return r;
	}

	// anImport NEWLINE*
	private static boolean imports_0(PsiBuilder b, int l) {
		if (!recursion_guard_(b, l, "imports_0")) return false;
		boolean r;
		Marker m = enter_section_(b);
		r = anImport(b, l + 1);
		r = r && imports_0_1(b, l + 1);
		exit_section_(b, m, null, r);
		return r;
	}

	// NEWLINE*
	private static boolean imports_0_1(PsiBuilder b, int l) {
		if (!recursion_guard_(b, l, "imports_0_1")) return false;
		while (true) {
			int c = current_position_(b);
			if (!consumeToken(b, NEWLINE)) break;
			if (!empty_element_parsed_guard_(b, "imports_0_1", c)) break;
		}
		return true;
	}

	/* ********************************************************** */
	// NATURAL_VALUE_KEY | NEGATIVE_VALUE_KEY
	public static boolean integerValue(PsiBuilder b, int l) {
		if (!recursion_guard_(b, l, "integerValue")) return false;
		if (!nextTokenIs(b, "<integer value>", NATURAL_VALUE_KEY, NEGATIVE_VALUE_KEY)) return false;
		boolean r;
		Marker m = enter_section_(b, l, _NONE_, INTEGER_VALUE, "<integer value>");
		r = consumeToken(b, NATURAL_VALUE_KEY);
		if (!r) r = consumeToken(b, NEGATIVE_VALUE_KEY);
		exit_section_(b, l, m, r, false, null);
		return r;
	}

	/* ********************************************************** */
	// (NATURAL_VALUE_KEY | STAR) DOT DOT (NATURAL_VALUE_KEY | STAR)
	public static boolean listRange(PsiBuilder b, int l) {
		if (!recursion_guard_(b, l, "listRange")) return false;
		if (!nextTokenIs(b, "<list range>", NATURAL_VALUE_KEY, STAR)) return false;
		boolean r;
		Marker m = enter_section_(b, l, _NONE_, LIST_RANGE, "<list range>");
		r = listRange_0(b, l + 1);
		r = r && consumeTokens(b, 0, DOT, DOT);
		r = r && listRange_3(b, l + 1);
		exit_section_(b, l, m, r, false, null);
		return r;
	}

	// NATURAL_VALUE_KEY | STAR
	private static boolean listRange_0(PsiBuilder b, int l) {
		if (!recursion_guard_(b, l, "listRange_0")) return false;
		boolean r;
		r = consumeToken(b, NATURAL_VALUE_KEY);
		if (!r) r = consumeToken(b, STAR);
		return r;
	}

	// NATURAL_VALUE_KEY | STAR
	private static boolean listRange_3(PsiBuilder b, int l) {
		if (!recursion_guard_(b, l, "listRange_3")) return false;
		boolean r;
		r = consumeToken(b, NATURAL_VALUE_KEY);
		if (!r) r = consumeToken(b, STAR);
		return r;
	}

	/* ********************************************************** */
	// METAIDENTIFIER_KEY | IDENTIFIER_KEY
	public static boolean metaIdentifier(PsiBuilder b, int l) {
		if (!recursion_guard_(b, l, "metaIdentifier")) return false;
		if (!nextTokenIs(b, "<meta identifier>", IDENTIFIER_KEY, METAIDENTIFIER_KEY)) return false;
		boolean r;
		Marker m = enter_section_(b, l, _NONE_, META_IDENTIFIER, "<meta identifier>");
		r = consumeToken(b, METAIDENTIFIER_KEY);
		if (!r) r = consumeToken(b, IDENTIFIER_KEY);
		exit_section_(b, l, m, r, false, null);
		return r;
	}

	/* ********************************************************** */
	// AT identifierReference
	public static boolean methodReference(PsiBuilder b, int l) {
		if (!recursion_guard_(b, l, "methodReference")) return false;
		if (!nextTokenIs(b, AT)) return false;
		boolean r, p;
		Marker m = enter_section_(b, l, _NONE_, METHOD_REFERENCE, null);
		r = consumeToken(b, AT);
		p = r; // pin = 1
		r = r && identifierReference(b, l + 1);
		exit_section_(b, l, m, r, p, null);
		return r || p;
	}

	/* ********************************************************** */
	// identifier | METRIC_VALUE_KEY
	public static boolean metric(PsiBuilder b, int l) {
		if (!recursion_guard_(b, l, "metric")) return false;
		if (!nextTokenIs(b, "<metric>", IDENTIFIER_KEY, METRIC_VALUE_KEY)) return false;
		boolean r;
		Marker m = enter_section_(b, l, _NONE_, METRIC, "<metric>");
		r = identifier(b, l + 1);
		if (!r) r = consumeToken(b, METRIC_VALUE_KEY);
		exit_section_(b, l, m, r, false, null);
		return r;
	}

	/* ********************************************************** */
	// doc? signature body?
	public static boolean node(PsiBuilder b, int l) {
		if (!recursion_guard_(b, l, "node")) return false;
		boolean r, p;
		Marker m = enter_section_(b, l, _NONE_, NODE, "<node>");
		r = node_0(b, l + 1);
		r = r && signature(b, l + 1);
		p = r; // pin = 2
		r = r && node_2(b, l + 1);
		exit_section_(b, l, m, r, p, null);
		return r || p;
	}

	// doc?
	private static boolean node_0(PsiBuilder b, int l) {
		if (!recursion_guard_(b, l, "node_0")) return false;
		doc(b, l + 1);
		return true;
	}

	// body?
	private static boolean node_2(PsiBuilder b, int l) {
		if (!recursion_guard_(b, l, "node_2")) return false;
		body(b, l + 1);
		return true;
	}

	/* ********************************************************** */
	// varInit | variable | node | nodeReference
	static boolean nodeConstituents(PsiBuilder b, int l) {
		if (!recursion_guard_(b, l, "nodeConstituents")) return false;
		boolean r;
		r = varInit(b, l + 1);
		if (!r) r = variable(b, l + 1);
		if (!r) r = node(b, l + 1);
		if (!r) r = nodeReference(b, l + 1);
		return r;
	}

	/* ********************************************************** */
	// HAS ruleContainer* identifierReference tags?
	public static boolean nodeReference(PsiBuilder b, int l) {
		if (!recursion_guard_(b, l, "nodeReference")) return false;
		if (!nextTokenIs(b, HAS)) return false;
		boolean r, p;
		Marker m = enter_section_(b, l, _NONE_, NODE_REFERENCE, null);
		r = consumeToken(b, HAS);
		p = r; // pin = 1
		r = r && report_error_(b, nodeReference_1(b, l + 1));
		r = p && report_error_(b, identifierReference(b, l + 1)) && r;
		r = p && nodeReference_3(b, l + 1) && r;
		exit_section_(b, l, m, r, p, null);
		return r || p;
	}

	// ruleContainer*
	private static boolean nodeReference_1(PsiBuilder b, int l) {
		if (!recursion_guard_(b, l, "nodeReference_1")) return false;
		while (true) {
			int c = current_position_(b);
			if (!ruleContainer(b, l + 1)) break;
			if (!empty_element_parsed_guard_(b, "nodeReference_1", c)) break;
		}
		return true;
	}

	// tags?
	private static boolean nodeReference_3(PsiBuilder b, int l) {
		if (!recursion_guard_(b, l, "nodeReference_3")) return false;
		tags(b, l + 1);
		return true;
	}

	/* ********************************************************** */
	// (identifier EQUALS)? value
	public static boolean parameter(PsiBuilder b, int l) {
		if (!recursion_guard_(b, l, "parameter")) return false;
		boolean r;
		Marker m = enter_section_(b, l, _NONE_, PARAMETER, "<parameter>");
		r = parameter_0(b, l + 1);
		r = r && value(b, l + 1);
		exit_section_(b, l, m, r, false, null);
		return r;
	}

	// (identifier EQUALS)?
	private static boolean parameter_0(PsiBuilder b, int l) {
		if (!recursion_guard_(b, l, "parameter_0")) return false;
		parameter_0_0(b, l + 1);
		return true;
	}

	// identifier EQUALS
	private static boolean parameter_0_0(PsiBuilder b, int l) {
		if (!recursion_guard_(b, l, "parameter_0_0")) return false;
		boolean r;
		Marker m = enter_section_(b);
		r = identifier(b, l + 1);
		r = r && consumeToken(b, EQUALS);
		exit_section_(b, m, null, r);
		return r;
	}

	/* ********************************************************** */
	// LEFT_PARENTHESIS (parameter (COMMA parameter)*)? RIGHT_PARENTHESIS
	public static boolean parameters(PsiBuilder b, int l) {
		if (!recursion_guard_(b, l, "parameters")) return false;
		if (!nextTokenIs(b, LEFT_PARENTHESIS)) return false;
		boolean r, p;
		Marker m = enter_section_(b, l, _NONE_, PARAMETERS, null);
		r = consumeToken(b, LEFT_PARENTHESIS);
		p = r; // pin = 1
		r = r && report_error_(b, parameters_1(b, l + 1));
		r = p && consumeToken(b, RIGHT_PARENTHESIS) && r;
		exit_section_(b, l, m, r, p, null);
		return r || p;
	}

	// (parameter (COMMA parameter)*)?
	private static boolean parameters_1(PsiBuilder b, int l) {
		if (!recursion_guard_(b, l, "parameters_1")) return false;
		parameters_1_0(b, l + 1);
		return true;
	}

	// parameter (COMMA parameter)*
	private static boolean parameters_1_0(PsiBuilder b, int l) {
		if (!recursion_guard_(b, l, "parameters_1_0")) return false;
		boolean r;
		Marker m = enter_section_(b);
		r = parameter(b, l + 1);
		r = r && parameters_1_0_1(b, l + 1);
		exit_section_(b, m, null, r);
		return r;
	}

	// (COMMA parameter)*
	private static boolean parameters_1_0_1(PsiBuilder b, int l) {
		if (!recursion_guard_(b, l, "parameters_1_0_1")) return false;
		while (true) {
			int c = current_position_(b);
			if (!parameters_1_0_1_0(b, l + 1)) break;
			if (!empty_element_parsed_guard_(b, "parameters_1_0_1", c)) break;
		}
		return true;
	}

	// COMMA parameter
	private static boolean parameters_1_0_1_0(PsiBuilder b, int l) {
		if (!recursion_guard_(b, l, "parameters_1_0_1_0")) return false;
		boolean r;
		Marker m = enter_section_(b);
		r = consumeToken(b, COMMA);
		r = r && parameter(b, l + 1);
		exit_section_(b, m, null, r);
		return r;
	}

	/* ********************************************************** */
	// EXTENDS identifierReference
	static boolean parent(PsiBuilder b, int l) {
		if (!recursion_guard_(b, l, "parent")) return false;
		if (!nextTokenIs(b, EXTENDS)) return false;
		boolean r, p;
		Marker m = enter_section_(b, l, _NONE_);
		r = consumeToken(b, EXTENDS);
		p = r; // pin = 1
		r = r && identifierReference(b, l + 1);
		exit_section_(b, l, m, r, p, null);
		return r || p;
	}

	/* ********************************************************** */
	// (doubleValue | integerValue | STAR) (DOT DOT (doubleValue | integerValue | STAR))?
	public static boolean range(PsiBuilder b, int l) {
		if (!recursion_guard_(b, l, "range")) return false;
		boolean r;
		Marker m = enter_section_(b, l, _NONE_, RANGE, "<range>");
		r = range_0(b, l + 1);
		r = r && range_1(b, l + 1);
		exit_section_(b, l, m, r, false, null);
		return r;
	}

	// doubleValue | integerValue | STAR
	private static boolean range_0(PsiBuilder b, int l) {
		if (!recursion_guard_(b, l, "range_0")) return false;
		boolean r;
		r = doubleValue(b, l + 1);
		if (!r) r = integerValue(b, l + 1);
		if (!r) r = consumeToken(b, STAR);
		return r;
	}

	// (DOT DOT (doubleValue | integerValue | STAR))?
	private static boolean range_1(PsiBuilder b, int l) {
		if (!recursion_guard_(b, l, "range_1")) return false;
		range_1_0(b, l + 1);
		return true;
	}

	// DOT DOT (doubleValue | integerValue | STAR)
	private static boolean range_1_0(PsiBuilder b, int l) {
		if (!recursion_guard_(b, l, "range_1_0")) return false;
		boolean r;
		Marker m = enter_section_(b);
		r = consumeTokens(b, 0, DOT, DOT);
		r = r && range_1_0_2(b, l + 1);
		exit_section_(b, m, null, r);
		return r;
	}

	// doubleValue | integerValue | STAR
	private static boolean range_1_0_2(PsiBuilder b, int l) {
		if (!recursion_guard_(b, l, "range_1_0_2")) return false;
		boolean r;
		r = doubleValue(b, l + 1);
		if (!r) r = integerValue(b, l + 1);
		if (!r) r = consumeToken(b, STAR);
		return r;
	}

	/* ********************************************************** */
	// COMMENT? NEWLINE* (dslDeclaration NEWLINE+)? imports? (node NEWLINE+)*
	static boolean root(PsiBuilder b, int l) {
		if (!recursion_guard_(b, l, "root")) return false;
		boolean r;
		Marker m = enter_section_(b);
		r = root_0(b, l + 1);
		r = r && root_1(b, l + 1);
		r = r && root_2(b, l + 1);
		r = r && root_3(b, l + 1);
		r = r && root_4(b, l + 1);
		exit_section_(b, m, null, r);
		return r;
	}

	// COMMENT?
	private static boolean root_0(PsiBuilder b, int l) {
		if (!recursion_guard_(b, l, "root_0")) return false;
		consumeToken(b, COMMENT);
		return true;
	}

	// NEWLINE*
	private static boolean root_1(PsiBuilder b, int l) {
		if (!recursion_guard_(b, l, "root_1")) return false;
		while (true) {
			int c = current_position_(b);
			if (!consumeToken(b, NEWLINE)) break;
			if (!empty_element_parsed_guard_(b, "root_1", c)) break;
		}
		return true;
	}

	// (dslDeclaration NEWLINE+)?
	private static boolean root_2(PsiBuilder b, int l) {
		if (!recursion_guard_(b, l, "root_2")) return false;
		root_2_0(b, l + 1);
		return true;
	}

	// dslDeclaration NEWLINE+
	private static boolean root_2_0(PsiBuilder b, int l) {
		if (!recursion_guard_(b, l, "root_2_0")) return false;
		boolean r;
		Marker m = enter_section_(b);
		r = dslDeclaration(b, l + 1);
		r = r && root_2_0_1(b, l + 1);
		exit_section_(b, m, null, r);
		return r;
	}

	// NEWLINE+
	private static boolean root_2_0_1(PsiBuilder b, int l) {
		if (!recursion_guard_(b, l, "root_2_0_1")) return false;
		boolean r;
		Marker m = enter_section_(b);
		r = consumeToken(b, NEWLINE);
		while (r) {
			int c = current_position_(b);
			if (!consumeToken(b, NEWLINE)) break;
			if (!empty_element_parsed_guard_(b, "root_2_0_1", c)) break;
		}
		exit_section_(b, m, null, r);
		return r;
	}

	// imports?
	private static boolean root_3(PsiBuilder b, int l) {
		if (!recursion_guard_(b, l, "root_3")) return false;
		imports(b, l + 1);
		return true;
	}

	// (node NEWLINE+)*
	private static boolean root_4(PsiBuilder b, int l) {
		if (!recursion_guard_(b, l, "root_4")) return false;
		while (true) {
			int c = current_position_(b);
			if (!root_4_0(b, l + 1)) break;
			if (!empty_element_parsed_guard_(b, "root_4", c)) break;
		}
		return true;
	}

	// node NEWLINE+
	private static boolean root_4_0(PsiBuilder b, int l) {
		if (!recursion_guard_(b, l, "root_4_0")) return false;
		boolean r;
		Marker m = enter_section_(b);
		r = node(b, l + 1);
		r = r && root_4_0_1(b, l + 1);
		exit_section_(b, m, null, r);
		return r;
	}

	// NEWLINE+
	private static boolean root_4_0_1(PsiBuilder b, int l) {
		if (!recursion_guard_(b, l, "root_4_0_1")) return false;
		boolean r;
		Marker m = enter_section_(b);
		r = consumeToken(b, NEWLINE);
		while (r) {
			int c = current_position_(b);
			if (!consumeToken(b, NEWLINE)) break;
			if (!empty_element_parsed_guard_(b, "root_4_0_1", c)) break;
		}
		exit_section_(b, m, null, r);
		return r;
	}

	/* ********************************************************** */
	// (LEFT_CURLY (identifier+ | ((range | stringValue) metric?) | metric) RIGHT_CURLY) | (identifierReference CLASS_TYPE?)
	public static boolean rule(PsiBuilder b, int l) {
		if (!recursion_guard_(b, l, "rule")) return false;
		if (!nextTokenIs(b, "<rule>", IDENTIFIER_KEY, LEFT_CURLY)) return false;
		boolean r;
		Marker m = enter_section_(b, l, _NONE_, RULE, "<rule>");
		r = rule_0(b, l + 1);
		if (!r) r = rule_1(b, l + 1);
		exit_section_(b, l, m, r, false, null);
		return r;
	}

	// LEFT_CURLY (identifier+ | ((range | stringValue) metric?) | metric) RIGHT_CURLY
	private static boolean rule_0(PsiBuilder b, int l) {
		if (!recursion_guard_(b, l, "rule_0")) return false;
		boolean r;
		Marker m = enter_section_(b);
		r = consumeToken(b, LEFT_CURLY);
		r = r && rule_0_1(b, l + 1);
		r = r && consumeToken(b, RIGHT_CURLY);
		exit_section_(b, m, null, r);
		return r;
	}

	// identifier+ | ((range | stringValue) metric?) | metric
	private static boolean rule_0_1(PsiBuilder b, int l) {
		if (!recursion_guard_(b, l, "rule_0_1")) return false;
		boolean r;
		Marker m = enter_section_(b);
		r = rule_0_1_0(b, l + 1);
		if (!r) r = rule_0_1_1(b, l + 1);
		if (!r) r = metric(b, l + 1);
		exit_section_(b, m, null, r);
		return r;
	}

	// identifier+
	private static boolean rule_0_1_0(PsiBuilder b, int l) {
		if (!recursion_guard_(b, l, "rule_0_1_0")) return false;
		boolean r;
		Marker m = enter_section_(b);
		r = identifier(b, l + 1);
		while (r) {
			int c = current_position_(b);
			if (!identifier(b, l + 1)) break;
			if (!empty_element_parsed_guard_(b, "rule_0_1_0", c)) break;
		}
		exit_section_(b, m, null, r);
		return r;
	}

	// (range | stringValue) metric?
	private static boolean rule_0_1_1(PsiBuilder b, int l) {
		if (!recursion_guard_(b, l, "rule_0_1_1")) return false;
		boolean r;
		Marker m = enter_section_(b);
		r = rule_0_1_1_0(b, l + 1);
		r = r && rule_0_1_1_1(b, l + 1);
		exit_section_(b, m, null, r);
		return r;
	}

	// range | stringValue
	private static boolean rule_0_1_1_0(PsiBuilder b, int l) {
		if (!recursion_guard_(b, l, "rule_0_1_1_0")) return false;
		boolean r;
		r = range(b, l + 1);
		if (!r) r = stringValue(b, l + 1);
		return r;
	}

	// metric?
	private static boolean rule_0_1_1_1(PsiBuilder b, int l) {
		if (!recursion_guard_(b, l, "rule_0_1_1_1")) return false;
		metric(b, l + 1);
		return true;
	}

	// identifierReference CLASS_TYPE?
	private static boolean rule_1(PsiBuilder b, int l) {
		if (!recursion_guard_(b, l, "rule_1")) return false;
		boolean r;
		Marker m = enter_section_(b);
		r = identifierReference(b, l + 1);
		r = r && rule_1_1(b, l + 1);
		exit_section_(b, m, null, r);
		return r;
	}

	// CLASS_TYPE?
	private static boolean rule_1_1(PsiBuilder b, int l) {
		if (!recursion_guard_(b, l, "rule_1_1")) return false;
		consumeToken(b, CLASS_TYPE);
		return true;
	}

	/* ********************************************************** */
	// COLON rule
	public static boolean ruleContainer(PsiBuilder b, int l) {
		if (!recursion_guard_(b, l, "ruleContainer")) return false;
		if (!nextTokenIs(b, COLON)) return false;
		boolean r, p;
		Marker m = enter_section_(b, l, _NONE_, RULE_CONTAINER, null);
		r = consumeToken(b, COLON);
		p = r; // pin = 1
		r = r && rule(b, l + 1);
		exit_section_(b, l, m, r, p, null);
		return r || p;
	}

	/* ********************************************************** */
	// (subNode | (metaIdentifier ruleContainer* parameters? identifier? aspects? parent?)) (constraint? tags?)
	public static boolean signature(PsiBuilder b, int l) {
		if (!recursion_guard_(b, l, "signature")) return false;
		boolean r, p;
		Marker m = enter_section_(b, l, _NONE_, SIGNATURE, "<signature>");
		r = signature_0(b, l + 1);
		p = r; // pin = 1
		r = r && signature_1(b, l + 1);
		exit_section_(b, l, m, r, p, null);
		return r || p;
	}

	// subNode | (metaIdentifier ruleContainer* parameters? identifier? aspects? parent?)
	private static boolean signature_0(PsiBuilder b, int l) {
		if (!recursion_guard_(b, l, "signature_0")) return false;
		boolean r;
		Marker m = enter_section_(b);
		r = subNode(b, l + 1);
		if (!r) r = signature_0_1(b, l + 1);
		exit_section_(b, m, null, r);
		return r;
	}

	// metaIdentifier ruleContainer* parameters? identifier? aspects? parent?
	private static boolean signature_0_1(PsiBuilder b, int l) {
		if (!recursion_guard_(b, l, "signature_0_1")) return false;
		boolean r;
		Marker m = enter_section_(b);
		r = metaIdentifier(b, l + 1);
		r = r && signature_0_1_1(b, l + 1);
		r = r && signature_0_1_2(b, l + 1);
		r = r && signature_0_1_3(b, l + 1);
		r = r && signature_0_1_4(b, l + 1);
		r = r && signature_0_1_5(b, l + 1);
		exit_section_(b, m, null, r);
		return r;
	}

	// ruleContainer*
	private static boolean signature_0_1_1(PsiBuilder b, int l) {
		if (!recursion_guard_(b, l, "signature_0_1_1")) return false;
		while (true) {
			int c = current_position_(b);
			if (!ruleContainer(b, l + 1)) break;
			if (!empty_element_parsed_guard_(b, "signature_0_1_1", c)) break;
		}
		return true;
	}

	// parameters?
	private static boolean signature_0_1_2(PsiBuilder b, int l) {
		if (!recursion_guard_(b, l, "signature_0_1_2")) return false;
		parameters(b, l + 1);
		return true;
	}

	// identifier?
	private static boolean signature_0_1_3(PsiBuilder b, int l) {
		if (!recursion_guard_(b, l, "signature_0_1_3")) return false;
		identifier(b, l + 1);
		return true;
	}

	// aspects?
	private static boolean signature_0_1_4(PsiBuilder b, int l) {
		if (!recursion_guard_(b, l, "signature_0_1_4")) return false;
		aspects(b, l + 1);
		return true;
	}

	// parent?
	private static boolean signature_0_1_5(PsiBuilder b, int l) {
		if (!recursion_guard_(b, l, "signature_0_1_5")) return false;
		parent(b, l + 1);
		return true;
	}

	// constraint? tags?
	private static boolean signature_1(PsiBuilder b, int l) {
		if (!recursion_guard_(b, l, "signature_1")) return false;
		boolean r;
		Marker m = enter_section_(b);
		r = signature_1_0(b, l + 1);
		r = r && signature_1_1(b, l + 1);
		exit_section_(b, m, null, r);
		return r;
	}

	// constraint?
	private static boolean signature_1_0(PsiBuilder b, int l) {
		if (!recursion_guard_(b, l, "signature_1_0")) return false;
		constraint(b, l + 1);
		return true;
	}

	// tags?
	private static boolean signature_1_1(PsiBuilder b, int l) {
		if (!recursion_guard_(b, l, "signature_1_1")) return false;
		tags(b, l + 1);
		return true;
	}

	/* ********************************************************** */
	// NATURAL_VALUE_KEY | listRange
	public static boolean size(PsiBuilder b, int l) {
		if (!recursion_guard_(b, l, "size")) return false;
		if (!nextTokenIs(b, "<size>", NATURAL_VALUE_KEY, STAR)) return false;
		boolean r;
		Marker m = enter_section_(b, l, _NONE_, SIZE, "<size>");
		r = consumeToken(b, NATURAL_VALUE_KEY);
		if (!r) r = listRange(b, l + 1);
		exit_section_(b, l, m, r, false, null);
		return r;
	}

	/* ********************************************************** */
	// LEFT_SQUARE size? RIGHT_SQUARE
	public static boolean sizeRange(PsiBuilder b, int l) {
		if (!recursion_guard_(b, l, "sizeRange")) return false;
		if (!nextTokenIs(b, LEFT_SQUARE)) return false;
		boolean r;
		Marker m = enter_section_(b);
		r = consumeToken(b, LEFT_SQUARE);
		r = r && sizeRange_1(b, l + 1);
		r = r && consumeToken(b, RIGHT_SQUARE);
		exit_section_(b, m, SIZE_RANGE, r);
		return r;
	}

	// size?
	private static boolean sizeRange_1(PsiBuilder b, int l) {
		if (!recursion_guard_(b, l, "sizeRange_1")) return false;
		size(b, l + 1);
		return true;
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

	/* ********************************************************** */
	// SUB ruleContainer* parameters? identifier aspects?
	static boolean subNode(PsiBuilder b, int l) {
		if (!recursion_guard_(b, l, "subNode")) return false;
		if (!nextTokenIs(b, SUB)) return false;
		boolean r, p;
		Marker m = enter_section_(b, l, _NONE_);
		r = consumeToken(b, SUB);
		p = r; // pin = 1
		r = r && report_error_(b, subNode_1(b, l + 1));
		r = p && report_error_(b, subNode_2(b, l + 1)) && r;
		r = p && report_error_(b, identifier(b, l + 1)) && r;
		r = p && subNode_4(b, l + 1) && r;
		exit_section_(b, l, m, r, p, null);
		return r || p;
	}

	// ruleContainer*
	private static boolean subNode_1(PsiBuilder b, int l) {
		if (!recursion_guard_(b, l, "subNode_1")) return false;
		while (true) {
			int c = current_position_(b);
			if (!ruleContainer(b, l + 1)) break;
			if (!empty_element_parsed_guard_(b, "subNode_1", c)) break;
		}
		return true;
	}

	// parameters?
	private static boolean subNode_2(PsiBuilder b, int l) {
		if (!recursion_guard_(b, l, "subNode_2")) return false;
		parameters(b, l + 1);
		return true;
	}

	// aspects?
	private static boolean subNode_4(PsiBuilder b, int l) {
		if (!recursion_guard_(b, l, "subNode_4")) return false;
		aspects(b, l + 1);
		return true;
	}

	/* ********************************************************** */
	// flags? annotations?
	public static boolean tags(PsiBuilder b, int l) {
		if (!recursion_guard_(b, l, "tags")) return false;
		boolean r, p;
		Marker m = enter_section_(b, l, _NONE_, TAGS, "<tags>");
		r = tags_0(b, l + 1);
		p = r; // pin = 1
		r = r && tags_1(b, l + 1);
		exit_section_(b, l, m, r, p, null);
		return r || p;
	}

	// flags?
	private static boolean tags_0(PsiBuilder b, int l) {
		if (!recursion_guard_(b, l, "tags_0")) return false;
		flags(b, l + 1);
		return true;
	}

	// annotations?
	private static boolean tags_1(PsiBuilder b, int l) {
		if (!recursion_guard_(b, l, "tags_1")) return false;
		annotations(b, l + 1);
		return true;
	}

	/* ********************************************************** */
	// stringValue COLON doubleValue
	public static boolean tupleValue(PsiBuilder b, int l) {
		if (!recursion_guard_(b, l, "tupleValue")) return false;
		if (!nextTokenIs(b, QUOTE_BEGIN)) return false;
		boolean r;
		Marker m = enter_section_(b);
		r = stringValue(b, l + 1);
		r = r && consumeToken(b, COLON);
		r = r && doubleValue(b, l + 1);
		exit_section_(b, m, TUPLE_VALUE, r);
		return r;
	}

	/* ********************************************************** */
	// stringValue+
	//         | booleanValue+
	//         | tupleValue+
	//         | integerValue+ metric?
	//         | doubleValue+  metric?
	//         | expression+
	//         | emptyField
	//         | identifierReference+
	//         | methodReference+
	public static boolean value(PsiBuilder b, int l) {
		if (!recursion_guard_(b, l, "value")) return false;
		boolean r;
		Marker m = enter_section_(b, l, _NONE_, VALUE, "<value>");
		r = value_0(b, l + 1);
		if (!r) r = value_1(b, l + 1);
		if (!r) r = value_2(b, l + 1);
		if (!r) r = value_3(b, l + 1);
		if (!r) r = value_4(b, l + 1);
		if (!r) r = value_5(b, l + 1);
		if (!r) r = emptyField(b, l + 1);
		if (!r) r = value_7(b, l + 1);
		if (!r) r = value_8(b, l + 1);
		exit_section_(b, l, m, r, false, null);
		return r;
	}

	// stringValue+
	private static boolean value_0(PsiBuilder b, int l) {
		if (!recursion_guard_(b, l, "value_0")) return false;
		boolean r;
		Marker m = enter_section_(b);
		r = stringValue(b, l + 1);
		while (r) {
			int c = current_position_(b);
			if (!stringValue(b, l + 1)) break;
			if (!empty_element_parsed_guard_(b, "value_0", c)) break;
		}
		exit_section_(b, m, null, r);
		return r;
	}

	// booleanValue+
	private static boolean value_1(PsiBuilder b, int l) {
		if (!recursion_guard_(b, l, "value_1")) return false;
		boolean r;
		Marker m = enter_section_(b);
		r = booleanValue(b, l + 1);
		while (r) {
			int c = current_position_(b);
			if (!booleanValue(b, l + 1)) break;
			if (!empty_element_parsed_guard_(b, "value_1", c)) break;
		}
		exit_section_(b, m, null, r);
		return r;
	}

	// tupleValue+
	private static boolean value_2(PsiBuilder b, int l) {
		if (!recursion_guard_(b, l, "value_2")) return false;
		boolean r;
		Marker m = enter_section_(b);
		r = tupleValue(b, l + 1);
		while (r) {
			int c = current_position_(b);
			if (!tupleValue(b, l + 1)) break;
			if (!empty_element_parsed_guard_(b, "value_2", c)) break;
		}
		exit_section_(b, m, null, r);
		return r;
	}

	// integerValue+ metric?
	private static boolean value_3(PsiBuilder b, int l) {
		if (!recursion_guard_(b, l, "value_3")) return false;
		boolean r;
		Marker m = enter_section_(b);
		r = value_3_0(b, l + 1);
		r = r && value_3_1(b, l + 1);
		exit_section_(b, m, null, r);
		return r;
	}

	// integerValue+
	private static boolean value_3_0(PsiBuilder b, int l) {
		if (!recursion_guard_(b, l, "value_3_0")) return false;
		boolean r;
		Marker m = enter_section_(b);
		r = integerValue(b, l + 1);
		while (r) {
			int c = current_position_(b);
			if (!integerValue(b, l + 1)) break;
			if (!empty_element_parsed_guard_(b, "value_3_0", c)) break;
		}
		exit_section_(b, m, null, r);
		return r;
	}

	// metric?
	private static boolean value_3_1(PsiBuilder b, int l) {
		if (!recursion_guard_(b, l, "value_3_1")) return false;
		metric(b, l + 1);
		return true;
	}

	// doubleValue+  metric?
	private static boolean value_4(PsiBuilder b, int l) {
		if (!recursion_guard_(b, l, "value_4")) return false;
		boolean r;
		Marker m = enter_section_(b);
		r = value_4_0(b, l + 1);
		r = r && value_4_1(b, l + 1);
		exit_section_(b, m, null, r);
		return r;
	}

	// doubleValue+
	private static boolean value_4_0(PsiBuilder b, int l) {
		if (!recursion_guard_(b, l, "value_4_0")) return false;
		boolean r;
		Marker m = enter_section_(b);
		r = doubleValue(b, l + 1);
		while (r) {
			int c = current_position_(b);
			if (!doubleValue(b, l + 1)) break;
			if (!empty_element_parsed_guard_(b, "value_4_0", c)) break;
		}
		exit_section_(b, m, null, r);
		return r;
	}

	// metric?
	private static boolean value_4_1(PsiBuilder b, int l) {
		if (!recursion_guard_(b, l, "value_4_1")) return false;
		metric(b, l + 1);
		return true;
	}

	// expression+
	private static boolean value_5(PsiBuilder b, int l) {
		if (!recursion_guard_(b, l, "value_5")) return false;
		boolean r;
		Marker m = enter_section_(b);
		r = expression(b, l + 1);
		while (r) {
			int c = current_position_(b);
			if (!expression(b, l + 1)) break;
			if (!empty_element_parsed_guard_(b, "value_5", c)) break;
		}
		exit_section_(b, m, null, r);
		return r;
	}

	// identifierReference+
	private static boolean value_7(PsiBuilder b, int l) {
		if (!recursion_guard_(b, l, "value_7")) return false;
		boolean r;
		Marker m = enter_section_(b);
		r = identifierReference(b, l + 1);
		while (r) {
			int c = current_position_(b);
			if (!identifierReference(b, l + 1)) break;
			if (!empty_element_parsed_guard_(b, "value_7", c)) break;
		}
		exit_section_(b, m, null, r);
		return r;
	}

	// methodReference+
	private static boolean value_8(PsiBuilder b, int l) {
		if (!recursion_guard_(b, l, "value_8")) return false;
		boolean r;
		Marker m = enter_section_(b);
		r = methodReference(b, l + 1);
		while (r) {
			int c = current_position_(b);
			if (!methodReference(b, l + 1)) break;
			if (!empty_element_parsed_guard_(b, "value_8", c)) break;
		}
		exit_section_(b, m, null, r);
		return r;
	}

	/* ********************************************************** */
	// identifier ((EQUALS value) | bodyValue)
	public static boolean varInit(PsiBuilder b, int l) {
		if (!recursion_guard_(b, l, "varInit")) return false;
		if (!nextTokenIs(b, IDENTIFIER_KEY)) return false;
		boolean r;
		Marker m = enter_section_(b);
		r = identifier(b, l + 1);
		r = r && varInit_1(b, l + 1);
		exit_section_(b, m, VAR_INIT, r);
		return r;
	}

	// (EQUALS value) | bodyValue
	private static boolean varInit_1(PsiBuilder b, int l) {
		if (!recursion_guard_(b, l, "varInit_1")) return false;
		boolean r;
		Marker m = enter_section_(b);
		r = varInit_1_0(b, l + 1);
		if (!r) r = bodyValue(b, l + 1);
		exit_section_(b, m, null, r);
		return r;
	}

	// EQUALS value
	private static boolean varInit_1_0(PsiBuilder b, int l) {
		if (!recursion_guard_(b, l, "varInit_1_0")) return false;
		boolean r;
		Marker m = enter_section_(b);
		r = consumeToken(b, EQUALS);
		r = r && value(b, l + 1);
		exit_section_(b, m, null, r);
		return r;
	}

	/* ********************************************************** */
	// doc? VAR variableType sizeRange? ruleContainer? identifier (EQUALS value)? flags? bodyValue?
	public static boolean variable(PsiBuilder b, int l) {
		if (!recursion_guard_(b, l, "variable")) return false;
		if (!nextTokenIs(b, "<variable>", DOC_LINE, VAR)) return false;
		boolean r, p;
		Marker m = enter_section_(b, l, _NONE_, VARIABLE, "<variable>");
		r = variable_0(b, l + 1);
		r = r && consumeToken(b, VAR);
		p = r; // pin = 2
		r = r && report_error_(b, variableType(b, l + 1));
		r = p && report_error_(b, variable_3(b, l + 1)) && r;
		r = p && report_error_(b, variable_4(b, l + 1)) && r;
		r = p && report_error_(b, identifier(b, l + 1)) && r;
		r = p && report_error_(b, variable_6(b, l + 1)) && r;
		r = p && report_error_(b, variable_7(b, l + 1)) && r;
		r = p && variable_8(b, l + 1) && r;
		exit_section_(b, l, m, r, p, null);
		return r || p;
	}

	// doc?
	private static boolean variable_0(PsiBuilder b, int l) {
		if (!recursion_guard_(b, l, "variable_0")) return false;
		doc(b, l + 1);
		return true;
	}

	// sizeRange?
	private static boolean variable_3(PsiBuilder b, int l) {
		if (!recursion_guard_(b, l, "variable_3")) return false;
		sizeRange(b, l + 1);
		return true;
	}

	// ruleContainer?
	private static boolean variable_4(PsiBuilder b, int l) {
		if (!recursion_guard_(b, l, "variable_4")) return false;
		ruleContainer(b, l + 1);
		return true;
	}

	// (EQUALS value)?
	private static boolean variable_6(PsiBuilder b, int l) {
		if (!recursion_guard_(b, l, "variable_6")) return false;
		variable_6_0(b, l + 1);
		return true;
	}

	// EQUALS value
	private static boolean variable_6_0(PsiBuilder b, int l) {
		if (!recursion_guard_(b, l, "variable_6_0")) return false;
		boolean r;
		Marker m = enter_section_(b);
		r = consumeToken(b, EQUALS);
		r = r && value(b, l + 1);
		exit_section_(b, m, null, r);
		return r;
	}

	// flags?
	private static boolean variable_7(PsiBuilder b, int l) {
		if (!recursion_guard_(b, l, "variable_7")) return false;
		flags(b, l + 1);
		return true;
	}

	// bodyValue?
	private static boolean variable_8(PsiBuilder b, int l) {
		if (!recursion_guard_(b, l, "variable_8")) return false;
		bodyValue(b, l + 1);
		return true;
	}

	/* ********************************************************** */
	// FUNCTION_TYPE
	//                 | INT_TYPE
	//                 | LONG_TYPE
	//                 | DOUBLE_TYPE
	//                 | BOOLEAN_TYPE
	//                 | STRING_TYPE
	//                 | DATE_TYPE
	//                 | INSTANT_TYPE
	//                 | TIME_TYPE
	//                 | WORD_TYPE
	//                 | OBJECT_TYPE
	//                 | RESOURCE_TYPE
	//                 | identifierReference
	public static boolean variableType(PsiBuilder b, int l) {
		if (!recursion_guard_(b, l, "variableType")) return false;
		boolean r;
		Marker m = enter_section_(b, l, _NONE_, VARIABLE_TYPE, "<variable type>");
		r = consumeToken(b, FUNCTION_TYPE);
		if (!r) r = consumeToken(b, INT_TYPE);
		if (!r) r = consumeToken(b, LONG_TYPE);
		if (!r) r = consumeToken(b, DOUBLE_TYPE);
		if (!r) r = consumeToken(b, BOOLEAN_TYPE);
		if (!r) r = consumeToken(b, STRING_TYPE);
		if (!r) r = consumeToken(b, DATE_TYPE);
		if (!r) r = consumeToken(b, INSTANT_TYPE);
		if (!r) r = consumeToken(b, TIME_TYPE);
		if (!r) r = consumeToken(b, WORD_TYPE);
		if (!r) r = consumeToken(b, OBJECT_TYPE);
		if (!r) r = consumeToken(b, RESOURCE_TYPE);
		if (!r) r = identifierReference(b, l + 1);
		exit_section_(b, l, m, r, false, null);
		return r;
	}

}
