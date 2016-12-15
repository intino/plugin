package io.intino.plugin.dependencyresolution.web;

import org.siani.itrules.*;

import java.util.Locale;

import static org.siani.itrules.LineSeparator.*;

public class BowerTemplate extends Template {

	protected BowerTemplate(Locale locale, LineSeparator separator) {
		super(locale, separator);
	}

	public static Template create() {
		return new BowerTemplate(Locale.ENGLISH, LF).define();
	}

	public Template define() {
		add(
			rule().add((condition("type", "bower"))).add(literal("{\n  \"name\": \"")).add(mark("artifactID", "lowercase")).add(literal("\",\n  \"version\": \"")).add(mark("version")).add(literal("\",\n  \"dependencies\": {\n  \t\t")).add(mark("dependency").multiple(",\n")).add(literal("\n\n  },\n  \"resolutions\": {\n\t\t")).add(mark("resolution").multiple(",\n")).add(literal("\n  }\n}")),
			rule().add((condition("trigger", "dependency"))).add(literal("\"")).add(mark("name")).add(literal("\": \"")).add(expression().add(mark("url")).add(literal("#"))).add(mark("version")).add(literal("\"")),
			rule().add((condition("trigger", "resolution"))).add(literal("\"")).add(mark("name")).add(literal("\": \"")).add(mark("version")).add(literal("\""))
		);
		return this;
	}
}