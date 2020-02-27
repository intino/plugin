package io.intino.plugin.annotator.fix;

import io.intino.itrules.RuleSet;
import io.intino.itrules.Template;

public class DecorableTemplate extends Template {

	public RuleSet ruleSet() {
		return new RuleSet().add(
				rule().condition((trigger("node"))).output(literal("package ")).output(mark("package", "lowercase")).output(expression().output(literal(".")).output(mark("container", "lowercase"))).output(literal(";\n\n\tpublic ")).output(expression().output(mark("abstract"))).output(literal(" class ")).output(mark("name", "FirstUpperCase")).output(literal(" extends Abstract")).output(mark("name", "FirstUpperCase")).output(literal(" {\n\n\t\tpublic ")).output(mark("name", "FirstUpperCase")).output(literal("(io.intino.magritte.framework.Node node) {\n\t\t\tsuper(node);\n\t\t}\n\t}\n\t")).output(mark("node").multiple("\n\n")).output(literal("\n"))
		);
	}
}