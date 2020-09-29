package io.intino.plugin.codeinsight.languageinjection;

import io.intino.itrules.RuleSet;
import io.intino.itrules.Template;

public class ExpressionInjectionTemplate extends Template {

	public RuleSet ruleSet() {
		return new RuleSet().add(
				rule().condition((type("function"))).output(literal("package ")).output(mark("generatedLanguage", "lowerCase")).output(literal(".natives;\n\n")).output(mark("imports").multiple("\n")).output(literal("\n\nclass ")).output(mark("name", "FirstUpperCase")).output(literal(" implements")).output(expression().output(literal(" ")).output(mark("scope", "lowercase")).output(literal(".functions.")).next(expression().output(literal(" ")))).output(mark("rule", "FirstUpperCase")).output(literal(", io.intino.magritte.framework.Function {\n\t")).output(mark("nativeContainer")).output(literal(" self;\n\n\t@Override\n\t")).output(mark("signature")).output(literal(" {\n\t\t")).output(mark("return")),
				rule().condition((type("reactive"))).output(literal("package ")).output(mark("generatedLanguage", "lowerCase")).output(literal(".natives;\n\n")).output(mark("imports").multiple("\n")).output(literal("\n\nclass ")).output(mark("name", "FirstUpperCase")).output(literal(" implements io.intino.magritte.framework.Expression<")).output(mark("type", "format")).output(literal("> {\n\t")).output(mark("nativeContainer")).output(literal(" self;\n\n\tpublic ")).output(mark("type", "format")).output(literal(" value() {\n\t\t")).output(mark("return")).output(literal("\n")),
				rule().condition((type("list")), (trigger("format"))).output(literal("java.util.List<")).output(mark("value", "javaType")).output(literal(">")),
				rule().condition((trigger("format"))).output(mark("value", "javaType")),
				rule().condition((attribute("", "instant")), (trigger("javatype"))).output(literal("java.time.Instant")),
				rule().condition((attribute("", "Instant")), (trigger("javatype"))).output(literal("java.time.Instant")),
				rule().condition((attribute("", "Date")), (trigger("javatype"))).output(literal("Date")),
				rule().condition((attribute("", "date")), (trigger("javatype"))).output(literal("Date")),
				rule().condition((attribute("", "time")), (trigger("javatype"))).output(literal("java.time.LocalTime")),
				rule().condition((attribute("", "Time")), (trigger("javatype"))).output(literal("java.time.LocalTime")),
				rule().condition((attribute("", "Resource")), (trigger("javatype"))).output(literal("java.net.URL")),
				rule().condition((attribute("", "resource")), (trigger("javatype"))).output(literal("java.net.URL"))
		);
	}
}