package org.siani.legio.plugin.project;

import org.siani.itrules.*;

import java.util.Locale;

import static org.siani.itrules.LineSeparator.*;

public class LegioTemplate extends Template {

	protected LegioTemplate(Locale locale, LineSeparator separator) {
		super(locale, separator);
	}

	public static Template create() {
		return new LegioTemplate(Locale.ENGLISH, LF).define();
	}

	public Template define() {
		add(
			rule().add((condition("type", "legio"))).add(literal("dsl Legio\n\nProject ")).add(mark("name")).add(literal(" as Platform //TODO Change for your project\n\tgroupId = \"org.sample\"\n\tversion = \"1.0.0\"\n\tDSL Proteo\n\tOutDSL SampleApplication\n\n    Dependencies\n        Test\n\tDependency(\"junit:junit:LATEST\", Test)"))
		);
		return this;
	}
}