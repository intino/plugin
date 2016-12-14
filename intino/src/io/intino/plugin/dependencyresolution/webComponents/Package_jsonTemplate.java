package io.intino.plugin.dependencyresolution.webComponents;

import org.siani.itrules.*;

import java.util.Locale;

import static org.siani.itrules.LineSeparator.*;

public class Package_jsonTemplate extends Template {

	protected Package_jsonTemplate(Locale locale, LineSeparator separator) {
		super(locale, separator);
	}

	public static Template create() {
		return new Package_jsonTemplate(Locale.ENGLISH, LF).define();
	}

	public Template define() {
		add(
			rule().add((condition("type", "package"))).add(literal("{\n\t\"name\": \"")).add(mark("artifactID")).add(literal("\",\n\t\"version\": \"")).add(mark("version")).add(literal("\",\n\t\"dependencies\": {},\n\t\"devDependencies\": {\n\t\t\"babel-preset-es2015\": \"^6.6.0\",\n\t\t\"bower\": \"^1.7.7\",\n\t\t\"del\": \"^2.0.2\",\n\t\t\"eslint-plugin-html\": \"^1.4.0\",\n\t\t\"gulp\": \"^3.9.1\",\n\t\t\"gulp-autoprefixer\": \"^2.1.0\",\n\t\t\"gulp-babel\": \"^6.1.2\",\n\t\t\"gulp-changed\": \"^1.0.0\",\n\t\t\"gulp-concat\": \"^2.6.0\",\n\t\t\"gulp-crisper\": \"1.0.0\",\n\t\t\"gulp-eslint\": \"^2.0.0\",\n\t\t\"gulp-htmlmin\": \"^1.3.0\",\n\t\t\"gulp-if\": \"^1.2.1\",\n\t\t\"gulp-imagemin\": \"^2.2.1\",\n\t\t\"gulp-livereload\": \"^3.8.1\",\n\t\t\"gulp-load-plugins\": \"^0.10.0\",\n\t\t\"gulp-minify-css\": \"^1.2.1\",\n\t\t\"gulp-rename\": \"^1.2.0\",\n\t\t\"gulp-replace\": \"^0.5.3\",\n\t\t\"gulp-size\": \"^2.0.0\",\n\t\t\"gulp-sourcemaps\": \"^1.6.0\",\n\t\t\"gulp-uglify\": \"^1.2.0\",\n\t\t\"gulp-useref\": \"^1.1.2\",\n\t\t\"gulp-vulcanize\": \"^6.0.0\",\n\t\t\"merge-stream\": \"^0.1.7\",\n\t\t\"run-sequence\": \"^1.0.2\",\n\t\t\"web-component-tester\": \"^4.2.0\"\n\t},\n\t\"scripts\": {\n\t},\n\t\"engines\": {\n\t\t\"node\": \">=0.10.0\"\n\t}\n}"))
		);
		return this;
	}
}