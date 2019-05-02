package io.intino.plugin.dependencyresolution.web;

import io.intino.itrules.RuleSet;
import io.intino.itrules.Template;

public class Package_jsonTemplate extends Template {

	public RuleSet ruleSet() {
		return new RuleSet().add(
			rule().condition((type("package"))).output(literal("{\n\t\"name\": \"")).output(mark("artifactID")).output(literal("\",\n\t\"version\": \"")).output(mark("version")).output(literal("\",\n\t\"dependencies\": {},\n\t\"devDependencies\": {\n\t\t\"babel-preset-env\": \"^1.6.1\",\n        \"bower\": \"^1.7.7\",\n        \"del\": \"^2.2.2\",\n        \"fs-extra\": \"^5.0.0\",\n        \"eslint-plugin-html\": \"^1.4.0\",\n        \"gulp\": \"^3.9.1\",\n        \"gulp-autoprefixer\": \"^2.1.0\",\n        \"gulp-babel\": \"^6.1.2\",\n        \"gulp-changed\": \"^1.0.0\",\n        \"gulp-clean-css\": \"^3.9.2\",\n        \"gulp-concat\": \"^2.6.0\",\n        \"gulp-crisper\": \"1.0.0\",\n        \"gulp-eslint\": \"^2.0.0\",\n        \"gulp-htmlmin\": \"^4.0.0\",\n        \"gulp-if\": \"^1.2.1\",\n        \"gulp-imagemin\": \"^2.2.1\",\n        \"gulp-livereload\": \"^3.8.1\",\n        \"gulp-load-plugins\": \"^0.10.0\",\n        \"gulp-rename\": \"^1.2.0\",\n        \"gulp-replace\": \"^0.5.3\",\n        \"gulp-rsync\": \"0.0.8\",\n        \"gulp-size\": \"^2.0.0\",\n        \"gulp-sourcemaps\": \"^1.6.0\",\n        \"gulp-uglify\": \"^1.2.0\",\n        \"gulp-useref\": \"^1.1.2\",\n        \"gulp-vulcanize\": \"^6.0.0\",\n        \"merge-stream\": \"^0.1.7\",\n        \"run-sequence\": \"^1.0.2\",\n        \"web-component-tester\": \"^4.2.0\"\n\t},\n\t\"scripts\": {\n\t},\n\t\"engines\": {\n\t\t\"node\": \">=0.10.0\"\n\t}\n}"))
		);
	}
}