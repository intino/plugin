package io.intino.plugin.dependencyresolution.web;

import io.intino.itrules.template.Rule;
import io.intino.itrules.template.Template;

import java.util.ArrayList;
import java.util.List;

import static io.intino.itrules.template.condition.predicates.Predicates.*;
import static io.intino.itrules.template.outputs.Outputs.literal;
import static io.intino.itrules.template.outputs.Outputs.placeholder;

public class PomTemplate extends Template {

	public List<Rule> ruleSet() {
		List<Rule> rules = new ArrayList<>();
		rules.add(rule().condition(allTypes("pom")).output(literal("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<project xmlns=\"http://maven.apache.org/POM/4.0.0\"\n\t\t xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/maven-v4_0_0.xsd\">\n\t<modelVersion>4.0.0</modelVersion>\n\n\t<groupId>")).output(placeholder("groupId", "lowercase")).output(literal("</groupId>\n\t<artifactId>")).output(placeholder("artifactId", "lowercase")).output(literal("</artifactId>\n\t<version>")).output(placeholder("version")).output(literal("</version>\n\n\t<properties>\n\t\t<maven.compiler.source>17</maven.compiler.source>\n\t\t<maven.compiler.target>17</maven.compiler.target>\n\t\t<project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>\n\t</properties>\n\n\t<build>\n\t\t<plugins>\n\t\t\t<plugin>\n\t\t\t\t<groupId>com.github.eirslett</groupId>\n\t\t\t\t<artifactId>frontend-maven-plugin</artifactId>\n\t\t\t\t<version>1.11.0</version>\n\t\t\t\t<executions>\n\t\t\t\t    ")).output(placeholder("node")).output(literal("\n                    <execution>\n                        <id>npm install</id>\n                        <goals>\n                            <goal>npm</goal>\n                        </goals>\n                    </execution>\n\t\t\t\t</executions>\n\t\t\t\t<configuration>\n\t\t\t\t\t<nodeVersion>v12.16.0</nodeVersion>\n\t\t\t\t\t<installDirectory>${user.home}/</installDirectory>\n\t\t\t\t</configuration>\n\t\t\t</plugin>\n\t\t</plugins>\n\t</build>\n</project>")));
		rules.add(rule().condition(all(attribute("","node"), trigger("node"))).output(literal("<execution>\n    <id>install Mogram and npm</id>\n    <goals>\n        <goal>install-node-and-npm</goal>\n    </goals>\n    <phase>generate-resources</phase>\n</execution>")));
		return rules;
	}

	public String render(Object object) {
		return new io.intino.itrules.Engine(this).render(object);
	}
}