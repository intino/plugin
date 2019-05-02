package io.intino.plugin.dependencyresolution.web;

import io.intino.itrules.RuleSet;
import io.intino.itrules.Template;

public class BowerTemplate extends Template {

	public RuleSet ruleSet() {
		return new RuleSet().add(
			rule().condition((type("bower"))).output(literal("{\n  \"name\": \"")).output(mark("artifactID", "lowercase")).output(literal("\",\n  \"version\": \"")).output(mark("version")).output(literal("\",\n  \"dependencies\": {\n  \t\t")).output(mark("dependency").multiple(",\n")).output(literal("\n\n  },\n  \"resolutions\": {\n\t\t")).output(mark("resolution").multiple(",\n")).output(literal("\n  }\n}")),
			rule().condition((type("artifact")), (trigger("dependency"))).output(literal("\"")).output(mark("name")).output(literal("\": ")).output(mark("url")),
			rule().condition((trigger("dependency"))).output(literal("\"")).output(mark("name")).output(literal("\": \"")).output(expression().output(mark("url")).output(literal("#"))).output(mark("version")).output(literal("\"")),
			rule().condition((trigger("resolution"))).output(literal("\"")).output(mark("name")).output(literal("\": \"")).output(mark("version")).output(literal("\""))
		);
	}
}