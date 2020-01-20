package io.intino.plugin.codeinsight.intentions;

import io.intino.itrules.RuleSet;
import io.intino.itrules.Template;

public class MethodTemplate extends Template {

	public RuleSet ruleSet() {
		return new RuleSet().add(
				rule().condition((allTypes("method", "multiple"))).output(literal("public static java.util.List<")).output(mark("type")).output(literal("> ")).output(mark("name")).output(literal("(")).output(mark("scope")).output(literal(" self")).output(expression().output(literal(", ")).output(mark("parameter").multiple(","))).output(literal(") {\n\t")).output(mark("body")).output(literal("\n}")),
				rule().condition((type("method"))).output(literal("public static ")).output(mark("type", "list")).output(literal(" ")).output(mark("name")).output(literal("(")).output(mark("scope")).output(literal(" self")).output(expression().output(literal(", ")).output(mark("parameter").multiple(","))).output(literal(") {\n\t")).output(mark("body")).output(literal("\n}")),
				rule().condition((attribute("value", "instant")), (trigger("type"))).output(literal("java.time.Instant")),
				rule().condition((attribute("value", "date")), (trigger("type"))).output(literal("Date")),
				rule().condition((attribute("value", "time")), (trigger("type"))).output(literal("java.time.LocalTime")),
				rule().condition((attribute("value", "Double")), (trigger("type"))).output(literal("double")),
				rule().condition((attribute("value", "INTEGER")), (trigger("type"))).output(literal("int")),
				rule().condition((attribute("value", "OBJECT")), (trigger("type"))).output(literal("Object")),
				rule().condition((attribute("value", "RESOURCE")), (trigger("type"))).output(literal("java.net.URL")),
				rule().condition((attribute("value", "resource")), (trigger("type"))).output(literal("java.net.URL")),
				rule().condition((attribute("value", "string")), (trigger("type"))).output(literal("String")),
				rule().condition((attribute("value", "boolean")), (trigger("type"))).output(literal("boolean")),
				rule().condition((attribute("value", "int")), (trigger("list"))).output(literal("Integer")),
				rule().condition((attribute("value", "instant")), (trigger("list"))).output(literal("java.time.Instant")),
				rule().condition((attribute("value", "date")), (trigger("list"))).output(literal("Date")),
				rule().condition((attribute("value", "time")), (trigger("list"))).output(literal("java.time.LocalTime")),
				rule().condition((attribute("value", "double")), (trigger("list"))).output(literal("Double")),
				rule().condition((attribute("value", "OBJECT")), (trigger("list"))).output(literal("Object")),
				rule().condition((attribute("value", "boolean")), (trigger("list"))).output(literal("Boolean")),
				rule().condition((attribute("value", "string")), (trigger("list"))).output(literal("String")),
				rule().condition((attribute("value", "resource")), (trigger("list"))).output(literal("java.net.URL")),
				rule().condition((attribute("value", "type")), (trigger("list"))).output(literal("Concept"))
		);
	}
}