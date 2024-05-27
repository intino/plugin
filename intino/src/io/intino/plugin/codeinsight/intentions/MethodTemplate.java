package io.intino.plugin.codeinsight.intentions;

import io.intino.itrules.template.Rule;
import io.intino.itrules.template.Template;

import java.util.ArrayList;
import java.util.List;

import static io.intino.itrules.template.condition.predicates.Predicates.*;
import static io.intino.itrules.template.outputs.Outputs.*;

public class MethodTemplate extends Template {

	public List<Rule> ruleSet() {
		List<Rule> rules = new ArrayList<>();
		rules.add(rule().condition(allTypes("method", "multiple")).output(literal("public static java.util.List<")).output(placeholder("type")).output(literal("> ")).output(placeholder("name")).output(literal("(")).output(placeholder("scope")).output(literal(" self")).output(expression().output(literal(", ")).output(placeholder("parameter").multiple(","))).output(literal(") {\n\t")).output(placeholder("body")).output(literal("\n}")));
		rules.add(rule().condition(allTypes("method")).output(literal("public static ")).output(placeholder("type", "list")).output(literal(" ")).output(placeholder("name")).output(literal("(")).output(placeholder("scope")).output(literal(" self")).output(expression().output(literal(", ")).output(placeholder("parameter").multiple(","))).output(literal(") {\n\t")).output(placeholder("body")).output(literal("\n}")));
		rules.add(rule().condition(all(attribute("","instant"), trigger("type"))).output(literal("java.time.Instant")));
		rules.add(rule().condition(all(attribute("","date"), trigger("type"))).output(literal("Date")));
		rules.add(rule().condition(all(attribute("","time"), trigger("type"))).output(literal("java.time.LocalTime")));
		rules.add(rule().condition(all(attribute("","Double"), trigger("type"))).output(literal("double")));
		rules.add(rule().condition(all(attribute("","INTEGER"), trigger("type"))).output(literal("int")));
		rules.add(rule().condition(all(attribute("","OBJECT"), trigger("type"))).output(literal("Object")));
		rules.add(rule().condition(all(attribute("","RESOURCE"), trigger("type"))).output(literal("java.net.URL")));
		rules.add(rule().condition(all(attribute("","resource"), trigger("type"))).output(literal("java.net.URL")));
		rules.add(rule().condition(all(attribute("","string"), trigger("type"))).output(literal("String")));
		rules.add(rule().condition(all(attribute("","boolean"), trigger("type"))).output(literal("boolean")));
		rules.add(rule().condition(all(attribute("","int"), trigger("list"))).output(literal("Integer")));
		rules.add(rule().condition(all(attribute("","instant"), trigger("list"))).output(literal("java.time.Instant")));
		rules.add(rule().condition(all(attribute("","date"), trigger("list"))).output(literal("Date")));
		rules.add(rule().condition(all(attribute("","time"), trigger("list"))).output(literal("java.time.LocalTime")));
		rules.add(rule().condition(all(attribute("","double"), trigger("list"))).output(literal("Double")));
		rules.add(rule().condition(all(attribute("","OBJECT"), trigger("list"))).output(literal("Object")));
		rules.add(rule().condition(all(attribute("","boolean"), trigger("list"))).output(literal("Boolean")));
		rules.add(rule().condition(all(attribute("","string"), trigger("list"))).output(literal("String")));
		rules.add(rule().condition(all(attribute("","resource"), trigger("list"))).output(literal("java.net.URL")));
		rules.add(rule().condition(all(attribute("","type"), trigger("list"))).output(literal("Concept")));
		return rules;
	}

	public String render(Object object) {
		return new io.intino.itrules.Engine(this).render(object);
	}

	public String render(Object object, java.util.Map<String, io.intino.itrules.Formatter> formatters) {
		return new io.intino.itrules.Engine(this).addAll(formatters).render(object);
	}
}