package io.intino.plugin.dependencyresolution.web;

import io.intino.itrules.RuleSet;
import io.intino.itrules.Template;

public class Package_jsonTemplate extends Template {

	public RuleSet ruleSet() {
		return new RuleSet().add(
				rule().condition((type("package"))).output(literal("{\n    \"name\": \"")).output(mark("artifactId")).output(literal("\",\n    \"version\": \"")).output(mark("version")).output(literal("\",\n    \"description\": \"\",\n    \"main\": \"src/App.js\",\n    \"scripts\": {\n        \"build\": \"webpack --mode production\",\n        \"dev\": \"webpack --mode development --watch\"\n    },\n    \"author\": \"\",\n    \"license\": \"ISC\",\n    \"devDependencies\": {\n        \"@babel/core\": \"^7.4.5\",\n        \"@babel/plugin-proposal-class-properties\": \"^7.4.4\",\n        \"@babel/plugin-syntax-dynamic-import\": \"^7.2.0\",\n        \"@babel/preset-env\": \"^7.4.5\",\n        \"@babel/preset-react\": \"^7.0.0\",\n        \"acorn\": \"^6.1.1\",\n        \"babel-loader\": \"^8.0.6\",\n        \"babel-plugin-dynamic-import-webpack\": \"^1.1.0\",\n        \"circular-dependency-plugin\": \"^5.0.2\",\n        \"copy-webpack-plugin\": \"^5.0.3\",\n        \"css-loader\": \"^2.1.1\",\n        \"html-loader\": \"^0.5.5\",\n        \"html-webpack-plugin\": \"^3.2.0\",\n        \"jss\": \"^9.8.7\",\n        \"prop-types\": \"^15.7.2\",\n        ")).output(mark("fstEvents")).output(literal("\n        \"style-loader\": \"^0.23.1\",\n        \"styled-components\": \"^4.3.1\",\n        \"webpack\": \"^4.34.0\",\n        \"webpack-cli\": \"^3.3.4\",\n        \"webpack-dev-server\": \"^3.7.1\"\n    },\n    \"dependencies\": {\n        ")).output(mark("dependency").multiple(",\n")).output(literal("\n    }\n}")),
				rule().condition((trigger("dependency"))).output(literal(" \"")).output(mark("name")).output(literal("\": \"")).output(mark("version")).output(literal("\"")),
				rule().condition((trigger("fsevents"))).output(literal("\"fsevents\": \"^2.1.2\","))
		);
	}
}