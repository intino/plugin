package io.intino.plugin.annotator.fix;

import io.intino.itrules.RuleSet;
import io.intino.itrules.Template;

public class DecorableTemplate extends Template {

	public RuleSet ruleSet() {
		return new RuleSet().add(
				rule().condition((trigger("node"))).output(literal("public ")).output(expression().output(mark("inner"))).output(literal(" ")).output(expression().output(mark("abstract"))).output(literal(" class ")).output(mark("name", "FirstUpperCase")).output(literal(" extends Abstract")).output(mark("name", "FirstUpperCase")).output(literal(" {\n\n\tpublic ")).output(mark("name", "FirstUpperCase")).output(literal("(io.intino.magritte.framework.Node node) {\n\t\tsuper(node);\n\t}\n\n\t")).output(mark("node").multiple("\n\n")).output(literal("\n}")),
				rule().condition((trigger("nodegen"))).output(literal("public ")).output(expression().output(mark("inner"))).output(literal(" abstract class Abstract")).output(mark("name", "FirstUpperCase")).output(literal(" extends io.intino.magritte.framework.Layer {\n\n\tpublic Abstract")).output(mark("name", "FirstUpperCase")).output(literal("(io.intino.magritte.framework.Node node) {\n\t\tsuper(node);\n\t}\n\n\t")).output(mark("nodeGen").multiple("\n\n")).output(literal("\n}")),
				rule().condition((type("decorable"))).output(literal("package ")).output(mark("package", "lowercase")).output(expression().output(literal(".")).output(mark("container", "lowercase"))).output(literal(";\n\n")).output(mark("node")).output(literal("\n")).output(mark("nodeGen"))
		);
	}
}