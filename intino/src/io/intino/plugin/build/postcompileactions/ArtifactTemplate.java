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
		rules.add(rule().condition(allTypes("artifact", "legio")).output(literal("dsl Legio\n\nArtifact(groupId = \"")).output(placeholder("groupId", "lowercase")).output(literal("\", version = \"1.0.0\") ")).output(placeholder("artifactId", "lowercase")).output(literal("\n\tWebImports\n\t\tPackDependency(\"@babel/core\", \"^7.4.5\")\n\t\tPackDependency(\"@babel/plugin-proposal-class-properties\", \"^7.4.4\")\n\t\tPackDependency(\"@babel/plugin-syntax-dynamic-import\", \"^7.2.0\")\n\t\tPackDependency(\"@babel/preset-env\", \"^7.4.5\")\n\t\tPackDependency(\"@babel/preset-react\", \"^7.0.0\")\n\t\tPackDependency(\"acorn\", \"^6.1.1\")\n\t\tPackDependency(\"babel-loader\", \"^8.0.6\")\n\t\tPackDependency(\"babel-plugin-dynamic-import-webpack\", \"^1.1.0\")\n\t\tPackDependency(\"circular-dependency-plugin\", \"^5.0.2\")\n\t\tPackDependency(\"copy-webpack-plugin\", \"^14.0.0\")\n\t\tPackDependency(\"css-loader\", \"^7.1.4\")\n\t\tPackDependency(\"fsevents\", \"^2.1.2\")\n\t\tPackDependency(\"html-loader\", \"^5.1.0\")\n\t\tPackDependency(\"html-webpack-plugin\", \"^5.6.7\")\n\t\tPackDependency(\"jss\", \"^9.8.7\")\n\t\tPackDependency(\"prop-types\", \"^15.7.2\")\n\t\tPackDependency(\"style-loader\", \"^0.23.1\")\n\t\tPackDependency(\"styled-components\", \"^4.3.1\")\n\t\tPackDependency(\"webpack\", \"^5.106.2\")\n\t\tPackDependency(\"webpack-cli\", \"^7.0.2\")\n\t\tPackDependency(\"webpack-dev-server\", \"^5.2.4\")\n\t\tWebArtifact(\"io.intino.alexandria\", \"ui-framework-elements\", \"")).output(placeholder("uiversion")).output(literal("\") alexandria-ui-elements\n\n")).output(placeholder("repository").multiple("\n\n")));
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