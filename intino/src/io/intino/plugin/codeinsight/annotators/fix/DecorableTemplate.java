package io.intino.plugin.codeinsight.annotators.fix;

import io.intino.itrules.template.Rule;
import io.intino.itrules.template.Template;

import java.util.ArrayList;
import java.util.List;

import static io.intino.itrules.template.condition.predicates.Predicates.allTypes;
import static io.intino.itrules.template.condition.predicates.Predicates.trigger;
import static io.intino.itrules.template.outputs.Outputs.*;

public class DecorableTemplate extends Template {

	public List<Rule> ruleSet() {
		List<Rule> rules = new ArrayList<>();
		rules.add(rule().condition(trigger("node")).output(literal("public ")).output(expression().output(placeholder("inner"))).output(literal(" ")).output(expression().output(placeholder("abstract"))).output(literal(" class ")).output(placeholder("name", "FirstUpperCase")).output(literal(" extends Abstract")).output(placeholder("name", "FirstUpperCase")).output(literal(" {\n\tpublic ")).output(placeholder("name", "FirstUpperCase")).output(literal("(io.intino.magritte.framework.Node node) {\n\t\tsuper(node);\n\t}\n\n\t")).output(expression().output(placeholder("node").multiple("\n\n"))).output(literal("\n}")));
		rules.add(rule().condition(trigger("nodegen")).output(literal("public ")).output(expression().output(placeholder("inner"))).output(literal(" abstract class Abstract")).output(placeholder("name", "FirstUpperCase")).output(literal(" extends io.intino.magritte.framework.Layer {\n\tpublic Abstract")).output(placeholder("name", "FirstUpperCase")).output(literal("(io.intino.magritte.framework.Node node) {\n\t\tsuper(node);\n\t}\n\n\t")).output(expression().output(placeholder("nodeGen").multiple("\n\n"))).output(literal("\n}")));
		rules.add(rule().condition(allTypes("decorable")).output(literal("package ")).output(placeholder("package", "lowercase")).output(expression().output(literal(".")).output(placeholder("container", "lowercase"))).output(literal(";\n\n")).output(placeholder("node")).output(literal("\n")).output(placeholder("nodeGen")));
		rules.add(rule().condition(allTypes("node")).output(literal("public ")).output(expression().output(placeholder("inner"))).output(literal(" ")).output(expression().output(placeholder("abstract"))).output(literal(" class ")).output(placeholder("name", "FirstUpperCase")).output(literal(" extends Abstract")).output(placeholder("name", "FirstUpperCase")).output(literal(" {\n\tpublic ")).output(placeholder("name", "FirstUpperCase")).output(literal("(io.intino.magritte.framework.Node node) {\n\t\tsuper(node);\n\t}\n\n\t")).output(expression().output(placeholder("node").multiple("\n\n"))).output(literal("\n}")));
		rules.add(rule().condition(allTypes("nodeGen")).output(literal("public ")).output(expression().output(placeholder("inner"))).output(literal(" abstract class Abstract")).output(placeholder("name", "FirstUpperCase")).output(literal(" extends io.intino.magritte.framework.Layer {\n\tpublic Abstract")).output(placeholder("name", "FirstUpperCase")).output(literal("(io.intino.magritte.framework.Node node) {\n\t\tsuper(node);\n\t}\n\n\t")).output(expression().output(placeholder("nodeGen").multiple("\n\n"))).output(literal("\n}")));
		return rules;
	}

	public String render(Object object) {
		return new io.intino.itrules.Engine(this).render(object);
	}

	public String render(Object object, java.util.Map<String, io.intino.itrules.Formatter> formatters) {
		return new io.intino.itrules.Engine(this).addAll(formatters).render(object);
	}
}