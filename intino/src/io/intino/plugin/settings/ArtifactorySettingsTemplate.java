package io.intino.plugin.settings;

import io.intino.itrules.RuleSet;
import io.intino.itrules.Template;

public class ArtifactorySettingsTemplate extends Template {

	public RuleSet ruleSet() {
		return new RuleSet().add(
			rule().condition((type("artifactory"))).output(literal("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n<settings xmlns=\"http://maven.apache.org/SETTINGS/1.0.0\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://maven.apache.org/SETTINGS/1.0.0\nhttps://maven.apache.org/xsd/settings-1.0.0.xsd\">\n\t<servers>\n\t\t")).output(mark("server").multiple("\n")).output(literal("\n\t</servers>\n</settings>")),
			rule().condition((type("server"))).output(literal("<server>\n\t<id>")).output(mark("name")).output(literal("</id>\n\t<username>")).output(mark("username")).output(literal("</username>\n\t<password>")).output(mark("password")).output(literal("</password>\n\t<configuration>\n        <timeout>5000</timeout>\n    </configuration>\n</server>"))
		);
	}
}