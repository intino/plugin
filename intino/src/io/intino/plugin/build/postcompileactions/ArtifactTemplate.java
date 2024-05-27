package io.intino.plugin.build.postcompileactions;

import io.intino.itrules.template.Rule;
import io.intino.itrules.template.Template;

import java.util.ArrayList;
import java.util.List;

import static io.intino.itrules.template.condition.predicates.Predicates.allTypes;
import static io.intino.itrules.template.condition.predicates.Predicates.trigger;
import static io.intino.itrules.template.outputs.Outputs.literal;
import static io.intino.itrules.template.outputs.Outputs.placeholder;

public class ArtifactTemplate extends Template {

	public List<Rule> ruleSet() {
		List<Rule> rules = new ArrayList<>();
		rules.add(rule().condition(allTypes("artifact", "legio")).output(literal("dsl Legio\n\nArtifact(groupId = \"")).output(placeholder("groupId", "lowercase")).output(literal("\", version = \"1.0.0\") ")).output(placeholder("artifactId", "lowercase")).output(literal("\n\tWebImports\n\t\tWebArtifact(\"io.intino.alexandria\", \"ui-framework-elements\", \"")).output(placeholder("uiversion")).output(literal("\") alexandria-ui-elements\n\n")).output(placeholder("repository").multiple("\n\n")));
		rules.add(rule().condition(allTypes("repository")).output(literal("Repository(\"")).output(placeholder("id")).output(literal("\")\n\t")).output(placeholder("url").multiple("\n")));
		rules.add(rule().condition(trigger("url")).output(literal("Release(\"")).output(placeholder("")).output(literal("\")")));
		return rules;
	}

	public String render(Object object) {
		return new io.intino.itrules.Engine(this).render(object);
	}

	public String render(Object object, java.util.Map<String, io.intino.itrules.Formatter> formatters) {
		return new io.intino.itrules.Engine(this).addAll(formatters).render(object);
	}
}