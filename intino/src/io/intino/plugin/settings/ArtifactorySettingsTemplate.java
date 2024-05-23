package io.intino.plugin.settings;

import io.intino.itrules.template.Rule;
import io.intino.itrules.template.Template;

import java.util.ArrayList;
import java.util.List;

import static io.intino.itrules.template.condition.predicates.Predicates.allTypes;
import static io.intino.itrules.template.outputs.Outputs.literal;
import static io.intino.itrules.template.outputs.Outputs.placeholder;

public class ArtifactorySettingsTemplate extends Template {

	public List<Rule> ruleSet() {
		List<Rule> rules = new ArrayList<>();
		rules.add(rule().condition(allTypes("artifactory")).output(literal("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n<settings xmlns=\"http://maven.apache.org/SETTINGS/1.0.0\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://maven.apache.org/SETTINGS/1.0.0\nhttps://maven.apache.org/xsd/settings-1.0.0.xsd\">\n\t<servers>\n\t\t")).output(placeholder("server").multiple("\n")).output(literal("\n\t</servers>\n</settings>")));
		rules.add(rule().condition(allTypes("server")).output(literal("<server>\n\t<id>")).output(placeholder("name")).output(literal("</id>\n\t<username>")).output(placeholder("username")).output(literal("</username>\n\t<password>")).output(placeholder("password")).output(literal("</password>\n\t<configuration>\n        <timeout>5000</timeout>\n    </configuration>\n</server>")));
		return rules;
	}

	public String render(Object object) {
		return new io.intino.itrules.Engine(this).render(object);
	}
}