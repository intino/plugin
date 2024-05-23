package io.intino.plugin.dependencyresolution.web;

import io.intino.itrules.template.Rule;
import io.intino.itrules.template.Template;

import java.util.ArrayList;
import java.util.List;

import static io.intino.itrules.template.condition.predicates.Predicates.allTypes;
import static io.intino.itrules.template.condition.predicates.Predicates.trigger;
import static io.intino.itrules.template.outputs.Outputs.literal;
import static io.intino.itrules.template.outputs.Outputs.placeholder;

public class Package_jsonTemplate extends Template {

	public List<Rule> ruleSet() {
		List<Rule> rules = new ArrayList<>();
		rules.add(rule().condition(allTypes("package")).output(literal("{\n    \"name\": \"")).output(placeholder("artifactId")).output(literal("\",\n    \"version\": \"")).output(placeholder("version")).output(literal("\",\n    \"description\": \"\",\n    \"main\": \"src/App.js\",\n    \"scripts\": {\n        \"build\": \"NODE_OPTIONS=--openssl-legacy-provider webpack --mode production\",\n        \"dev\": \"NODE_OPTIONS=--openssl-legacy-provider webpack --mode development --watch\"\n    },\n    \"author\": \"\",\n    \"license\": \"ISC\",\n    \"devDependencies\": {\n        \"@babel/core\": \"^7.4.5\",\n        \"@babel/plugin-proposal-class-properties\": \"^7.4.4\",\n        \"@babel/plugin-syntax-dynamic-import\": \"^7.2.0\",\n        \"@babel/preset-env\": \"^7.4.5\",\n        \"@babel/preset-react\": \"^7.0.0\",\n        \"acorn\": \"^6.1.1\",\n        \"babel-loader\": \"^8.0.6\",\n        \"babel-plugin-dynamic-import-webpack\": \"^1.1.0\",\n        \"circular-dependency-plugin\": \"^5.0.2\",\n        \"copy-webpack-plugin\": \"^5.0.3\",\n        \"css-loader\": \"^2.1.1\",\n        \"html-loader\": \"^0.5.5\",\n        \"html-webpack-plugin\": \"^3.2.0\",\n        \"jss\": \"^9.8.7\",\n        \"prop-types\": \"^15.7.2\",\n        ")).output(placeholder("fsevents")).output(literal("\n        \"style-loader\": \"^0.23.1\",\n        \"styled-components\": \"^4.3.1\",\n        \"webpack\": \"^4.34.0\",\n        \"webpack-cli\": \"^3.3.4\",\n        \"webpack-dev-server\": \"^3.7.1\"\n    },\n    \"dependencies\": {\n        ")).output(placeholder("dependency").multiple(",\n")).output(literal("\n    },\n    \"resolutions\" : {\n    \t")).output(placeholder("resolution").multiple(",\n")).output(literal("\n    }\n}")));
		rules.add(rule().condition(trigger("dependency")).output(literal(" \"")).output(placeholder("name")).output(literal("\": \"")).output(placeholder("version")).output(literal("\"")));
		rules.add(rule().condition(trigger("resolution")).output(literal(" \"")).output(placeholder("name")).output(literal("\": \"")).output(placeholder("version")).output(literal("\"")));
		rules.add(rule().condition(trigger("fsevents")).output(literal("\"fsevents\": \"^2.1.2\",")));
		return rules;
	}

	public String render(Object object) {
		return new io.intino.itrules.Engine(this).render(object);
	}
}