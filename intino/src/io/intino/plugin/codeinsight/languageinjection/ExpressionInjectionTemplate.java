package io.intino.plugin.codeinsight.languageinjection;

import io.intino.itrules.template.Rule;
import io.intino.itrules.template.Template;

import java.util.ArrayList;
import java.util.List;

import static io.intino.itrules.template.condition.predicates.Predicates.*;
import static io.intino.itrules.template.outputs.Outputs.*;

public class ExpressionInjectionTemplate extends Template {

	public List<Rule> ruleSet() {
		List<Rule> rules = new ArrayList<>();
		rules.add(rule().condition(allTypes("function")).output(literal("package ")).output(placeholder("generatedLanguage", "lowerCase")).output(literal(".natives;\n\n")).output(placeholder("imports").multiple("\n")).output(literal("\n\nclass ")).output(placeholder("name", "FirstUpperCase")).output(literal(" implements")).output(expression().output(literal(" ")).output(placeholder("scope", "lowercase")).output(literal(".functions."))).output(placeholder("rule", "FirstUpperCase")).output(literal(", io.intino.magritte.framework.Function {\n\t")).output(placeholder("nativeContainer")).output(literal(" self;\n\n\t@Override\n\t")).output(placeholder("signature")).output(literal(" {\n\t\t")).output(placeholder("return")));
		rules.add(rule().condition(allTypes("reactive")).output(literal("package ")).output(placeholder("generatedLanguage", "lowerCase")).output(literal(".natives;\n\n")).output(placeholder("imports").multiple("\n")).output(literal("\n\nclass ")).output(placeholder("name", "FirstUpperCase")).output(literal(" implements io.intino.magritte.framework.Expression<")).output(placeholder("type", "format")).output(literal("> {\n\t")).output(placeholder("nativeContainer")).output(literal(" self;\n\n\tpublic ")).output(placeholder("type", "format")).output(literal(" value() {\n\t\t")).output(placeholder("return")).output(literal("\n")));
		rules.add(rule().condition(all(allTypes("list"), trigger("format"))).output(literal("java.util.List<")).output(placeholder("value", "javaType")).output(literal(">")));
		rules.add(rule().condition(trigger("format")).output(placeholder("value", "javaType")));
		rules.add(rule().condition(all(attribute("","instant"), trigger("javatype"))).output(literal("java.time.Instant")));
		rules.add(rule().condition(all(attribute("","Instant"), trigger("javatype"))).output(literal("java.time.Instant")));
		rules.add(rule().condition(all(attribute("","Date"), trigger("javatype"))).output(literal("Date")));
		rules.add(rule().condition(all(attribute("","date"), trigger("javatype"))).output(literal("Date")));
		rules.add(rule().condition(all(attribute("","time"), trigger("javatype"))).output(literal("java.time.LocalTime")));
		rules.add(rule().condition(all(attribute("","Time"), trigger("javatype"))).output(literal("java.time.LocalTime")));
		rules.add(rule().condition(all(attribute("","Resource"), trigger("javatype"))).output(literal("java.net.URL")));
		rules.add(rule().condition(all(attribute("","resource"), trigger("javatype"))).output(literal("java.net.URL")));
		return rules;
	}

	public String render(Object object) {
		return new io.intino.itrules.Engine(this).render(object);
	}
}