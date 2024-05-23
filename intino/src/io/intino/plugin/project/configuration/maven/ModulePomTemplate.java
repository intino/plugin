package io.intino.plugin.project.configuration.maven;

import io.intino.itrules.template.Rule;
import io.intino.itrules.template.Template;

import java.util.ArrayList;
import java.util.List;

import static io.intino.itrules.template.condition.predicates.Predicates.*;
import static io.intino.itrules.template.outputs.Outputs.*;

public class ModulePomTemplate extends Template {

	public List<Rule> ruleSet() {
		List<Rule> rules = new ArrayList<>();
		rules.add(rule().condition(allTypes("pom")).output(literal("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<project xmlns=\"http://maven.apache.org/POM/4.0.0\"\n\t\t xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/maven-v4_0_0.xsd\">\n\t<modelVersion>4.0.0</modelVersion>\n\n\t<groupId>")).output(placeholder("project")).output(literal("</groupId>\n\t<artifactId>")).output(placeholder("name")).output(literal("</artifactId>\n\t<version>")).output(placeholder("version")).output(literal("</version>\n\n\t<properties>\n\t\t<maven.compiler.source>1.8</maven.compiler.source>\n\t\t<maven.compiler.target>1.8</maven.compiler.target>\n\t\t<tara.platform.dsl>Proteo</tara.platform.dsl>\n\t\t<tara.application.dsl></tara.application.dsl>\n\t\t<tara.system.dsl></tara.system.dsl>\n\t\t<tara.application.dsl.from.artifactory></tara.application.dsl.from.artifactory>\n\t\t<tara.system.dsl.from.artifactory></tara.system.dsl.from.artifactory>\n\t\t<tara.supported.languages>tara</tara.supported.languages>\n\t</properties>\n\n\t<build>\n\t\t<outputDirectory>")).output(expression().output(placeholder("default"))).output(literal("out/production/")).output(placeholder("name")).output(literal("</outputDirectory>\n\t\t<testOutputDirectory>")).output(expression().output(placeholder("default"))).output(literal("out/test/")).output(placeholder("name")).output(literal("</testOutputDirectory>\n\t\t<directory>")).output(expression().output(placeholder("default"))).output(literal("out/build/")).output(placeholder("name")).output(literal("</directory>\n\t\t<resources>\n\t\t\t<resource>\n\t\t\t\t<directory>${basedir}/../.tara/refactors</directory>\n\t\t\t</resource>\n\t\t</resources>\n\t</build>\n\t<repositories>\n\t\t<repository>\n\t\t\t<id>siani-maven</id>\n\t\t\t<name>siani-maven-releases</name>\n\t\t\t<url>http://artifactory.siani.es/artifactory/libs-release</url>\n\t\t</repository>\n\t</repositories>\n\n\t<distributionManagement>\n\t\t<repository>\n\t\t\t<id>siani-maven</id>\n\t\t\t<name>siani-maven-releases</name>\n\t\t\t<url>https://artifactory.siani.es/artifactory/libs-release-local</url>\n\t\t</repository>\n\t</distributionManagement>\n\n\t<dependencies>\n\t\t")).output(placeholder("magritte").multiple("\n")).output(literal("\n\t\t")).output(placeholder("parentModule")).output(literal("\n\t\t<dependency>\n\t\t\t<groupId>junit</groupId>\n\t\t\t<artifactId>junit</artifactId>\n\t\t\t<scope>test</scope>\n\t\t\t<version>LATEST</version>\n\t\t</dependency>\n\t</dependencies>\n</project>")));
		rules.add(rule().condition(all(allTypes("parent"), trigger("parentmodule"))).output(literal("<dependency>\n    <groupId>")).output(placeholder("groupId")).output(literal("</groupId>\n    <artifactId>")).output(placeholder("artifactId")).output(literal("</artifactId>\n    <version>")).output(placeholder("version")).output(literal("</version>\n</dependency>")));
		rules.add(rule().condition(all(allTypes("magritte"), trigger("magritte"))).output(literal("<dependency>\n    <groupId>io.intino.magritte</groupId>\n    <artifactId>framework</artifactId>\n    <version>LATEST</version>\n</dependency>")));
		return rules;
	}

	public String render(Object object) {
		return new io.intino.itrules.Engine(this).render(object);
	}
}