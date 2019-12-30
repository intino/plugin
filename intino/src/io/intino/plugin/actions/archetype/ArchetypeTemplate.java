package io.intino.plugin.actions.archetype;

import io.intino.itrules.RuleSet;
import io.intino.itrules.Template;

public class ArchetypeTemplate extends Template {

	public RuleSet ruleSet() {
		return new RuleSet().add(
				rule().condition((type("archetype"))).output(literal("package ")).output(mark("package")).output(literal(";\n\nimport java.io.File;\n\npublic class Archetype {\n\tprivate final File root;\n\n\tpublic Archetype(File root) {\n\t\tthis.root = root;\n\t}\n\n\tpublic File root() {\n\t\treturn this.root;\n\t}\n\n\t")).output(expression().output(mark("node", "getter").multiple("\n\n"))).output(literal("\n\n\t")).output(expression().output(mark("node", "class").multiple("\n\n"))).output(literal("\n}")),
				rule().condition((allTypes("node", "leaf", "list", "regex")), (attribute("parameter")), (trigger("getter"))).output(literal("public java.util.List<File> ")).output(mark("name", "snakeCaseToCamelCase", "firstLowerCase")).output(literal("(")).output(mark("parameter", "signature").multiple(", ")).output(literal(") {\n\treturn java.util.Arrays.stream(new File(root, \"")).output(mark("filePath")).output(literal("\").listFiles()).filter(f-> ")).output(expression().output(mark("with"))).output(mark("parameter", "replace").multiple("")).output(literal(".matches(f.getName())).collect(java.util.stream.Collectors.toList());\n}")),
				rule().condition((allTypes("node", "leaf", "list")), (attribute("parameter")), (trigger("getter"))).output(literal("public java.util.List<File> ")).output(mark("name", "snakeCaseToCamelCase", "firstLowerCase")).output(literal("(")).output(mark("parameter", "signature").multiple(", ")).output(literal(") {\n\treturn java.util.Arrays.stream(new File(root, \"")).output(mark("filePath")).output(literal("\").listFiles()).filter(f-> f.getName().contains(")).output(expression().output(mark("with"))).output(mark("parameter", "replace").multiple("")).output(literal(")).collect(java.util.stream.Collectors.toList());\n}")),
				rule().condition((allTypes("node", "leaf")), (attribute("parameter")), (trigger("getter"))).output(literal("public File ")).output(mark("name", "snakeCaseToCamelCase", "firstLowerCase")).output(literal("(")).output(mark("parameter", "signature").multiple(", ")).output(literal(") {\n\treturn new File(root, \"")).output(mark("filePath")).output(literal("\"")).output(expression().output(mark("parameter", "replace").multiple(""))).output(literal(");\n}")),
				rule().condition((allTypes("node", "leaf")), (trigger("getter"))).output(literal("public File ")).output(mark("name", "snakeCaseToCamelCase", "firstLowerCase")).output(literal("() {\n\treturn new File(root, \"")).output(mark("filePath")).output(literal("\");\n}")),
				rule().condition((type("node")), (trigger("getter"))).output(literal("public ")).output(mark("name", "snakeCaseToCamelCase", "firstUpperCase")).output(literal(" ")).output(mark("name", "snakeCaseToCamelCase", "firstLowerCase")).output(literal("() {\n\treturn new ")).output(mark("name", "snakeCaseToCamelCase", "firstUpperCase")).output(literal("(root);\n}")),
				rule().condition((type("node")), not(type("leaf")), (trigger("class"))).output(literal("public static class ")).output(mark("name", "snakeCaseToCamelCase", "firstUpperCase")).output(literal(" {\n\tprivate final File root;\n\n\tpublic ")).output(mark("name", "snakeCaseToCamelCase", "firstUpperCase")).output(literal("(File parent) {\n\t\tthis.root = new File(parent, \"")).output(mark("filePath")).output(literal("\");\n\t}\n\n\tpublic File root() {\n\t\treturn this.root;\n\t}\n\n\t")).output(expression().output(mark("node", "getter").multiple("\n\n"))).output(literal("\n\n\t")).output(expression().output(mark("node", "class").multiple("\n\n"))).output(literal("\n}")),
				rule().condition((type("timetag")), (trigger("signature"))).output(literal("io.intino.alexandria.Timetag ")).output(mark("value")),
				rule().condition((trigger("signature"))).output(literal("String ")).output(mark("value")),
				rule().condition((type("timetag")), (trigger("replace"))).output(literal(".replace(\"{")).output(mark("value")).output(literal("}\", ")).output(mark("value")).output(literal(".toString())")),
				rule().condition((trigger("replace"))).output(literal(".replace(\"{")).output(mark("value")).output(literal("}\", ")).output(mark("value")).output(literal(")"))
		);
	}
}