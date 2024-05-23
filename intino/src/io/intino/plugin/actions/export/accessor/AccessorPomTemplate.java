package io.intino.plugin.actions.export.accessor;

import io.intino.itrules.template.Rule;
import io.intino.itrules.template.Template;

import java.util.ArrayList;
import java.util.List;

import static io.intino.itrules.template.condition.predicates.Predicates.*;
import static io.intino.itrules.template.outputs.Outputs.*;

public class AccessorPomTemplate extends Template {

	public List<Rule> ruleSet() {
		List<Rule> rules = new ArrayList<>();
		rules.add(rule().condition(allTypes("pom")).output(literal("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n<project xmlns=\"http://maven.apache.org/POM/4.0.0\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/maven-v4_0_0.xsd\">\n<modelVersion>4.0.0</modelVersion>\n\n<groupId>")).output(placeholder("group", "lowercase")).output(literal("</groupId>\n<artifactId>")).output(placeholder("artifact", "lowercase")).output(literal("</artifactId>\n<version>")).output(placeholder("version")).output(literal("</version>\n")).output(expression().output(literal("<licenses")).output(literal(">")).output(literal("\n")).output(literal("\t")).output(placeholder("license").multiple("\n")).output(literal("\n")).output(literal("</licenses")).output(literal(">"))).output(literal("\n<properties>\n\t<maven.compiler.source>11</maven.compiler.source>\n\t<maven.compiler.target>11</maven.compiler.target>\n</properties>\n\n<build>\n\t<sourceDirectory>src</sourceDirectory>\n\t<outputDirectory>out/production/")).output(placeholder("artifact", "lowercase")).output(literal("</outputDirectory>\n\t<testOutputDirectory>out/test/")).output(placeholder("artifact", "lowercase")).output(literal("</testOutputDirectory>\n\t<directory>out/build/")).output(placeholder("artifact", "lowercase")).output(literal("</directory>\n\t<resources>\n\t\t<resource><directory>res</directory></resource>\n\t</resources>\n\t<plugins>\n\t\t<plugin>\n\t\t\t<groupId>org.apache.maven.plugins</groupId>\n\t\t\t<artifactId>maven-source-plugin</artifactId>\n\t\t\t<version>3.0.1</version>\n\t\t\t<executions>\n\t\t\t\t<execution>\n\t\t\t\t\t<id>attach-sources</id>\n\t\t\t\t\t<goals>\n\t\t\t\t\t\t<goal>jar-no-fork</goal>\n\t\t\t\t\t</goals>\n\t\t\t\t</execution>\n\t\t\t</executions>\n\t\t</plugin>\n\t</plugins>\n</build>\n\n<repositories>\n    ")).output(placeholder("repository", "release").multiple("\n")).output(literal("\n    <repository>\n        <id>intino-maven</id>\n        <name>intino-maven-releases</name>\n        <url>https://artifactory.intino.io/artifactory/releases</url>\n    </repository>\n</repositories>\n\n<distributionManagement>\n    ")).output(placeholder("repository", "distribution").multiple("\n")).output(literal("\n</distributionManagement>\n\n<dependencies>\n\t")).output(placeholder("dependency").multiple("\n")).output(literal("\n</dependencies>\n</project>")));
		rules.add(rule().condition(all(allTypes("repository", "Distribution", "snapshot"), trigger("distribution"))).output(literal("<snapshotRepository>\n\t<id>")).output(placeholder("name")).output(literal("</id>\n\t<url>")).output(placeholder("url")).output(literal("</url>\n</snapshotRepository>")));
		rules.add(rule().condition(all(allTypes("repository", "Distribution"), trigger("distribution"))).output(literal("<repository>\n\t<id>")).output(placeholder("name")).output(literal("</id>\n\t<url>")).output(placeholder("url")).output(literal("</url>\n</repository>")));
		rules.add(rule().condition(trigger("distribution")));
		rules.add(rule().condition(all(all(allTypes("repository"), not(allTypes("distribution"))), trigger("release"))).output(literal("<repository>\n\t<id>")).output(placeholder("name")).output(literal("-")).output(placeholder("random")).output(literal("</id>\n\t<url>")).output(placeholder("url")).output(literal("</url>\n</repository>")));
		rules.add(rule().condition(all(allTypes("GPL"), trigger("license"))).output(literal("<license>\n\t<name>The GNU General Public License v3.0</name>\n\t<url>https://www.gnu.org/licenses/gpl-3.0.txt</url>\n</license>")));
		rules.add(rule().condition(all(allTypes("BSD"), trigger("license"))).output(literal("<license>\n\t<name>BSD 3-Clause License</name>\n\t<url>https://opensource.org/licenses/BSD-3-Clause</url>\n</license>")));
		rules.add(rule().condition(all(allTypes("rest"), trigger("dependency"))).output(literal("<dependency>\n\t<groupId>io.intino.alexandria</groupId>\n\t<artifactId>rest-accessor</artifactId>\n\t<version>")).output(placeholder("version")).output(literal("</version>\n</dependency>")));
		rules.add(rule().condition(all(allTypes("jmx"), trigger("dependency"))).output(literal("<dependency>\n\t<groupId>io.intino.alexandria</groupId>\n\t<artifactId>jmx</artifactId>\n\t<version>1.0.2</version>\n</dependency>\n<dependency>\n    <groupId>io.intino.alexandria</groupId>\n    <artifactId>exceptions</artifactId>\n    <version>2.0.1</version>\n</dependency>")));
		rules.add(rule().condition(all(allTypes("messaging"), trigger("dependency"))).output(literal("<dependency>\n\t<groupId>io.intino.alexandria</groupId>\n\t<artifactId>terminal-jms</artifactId>\n\t<version>")).output(placeholder("version")).output(literal("</version>\n</dependency>\n<dependency>\n\t<groupId>io.intino.alexandria</groupId>\n\t<artifactId>json</artifactId>\n\t<version>[1.0.0, 2.0.0)</version>\n</dependency>")));
		rules.add(rule().condition(all(allTypes("analytic"), trigger("dependency"))).output(literal("<dependency>\n\t<groupId>io.intino.alexandria</groupId>\n\t<artifactId>led</artifactId>\n\t<version>")).output(placeholder("version")).output(literal("</version>\n</dependency>\n<dependency>\n\t<groupId>io.intino.alexandria</groupId>\n\t<artifactId>session</artifactId>\n\t<version>1.1.0</version>\n</dependency>\n<dependency>\n\t<groupId>commons-io</groupId>\n\t<artifactId>commons-io</artifactId>\n\t<version>2.8.0</version>\n</dependency>")));
		return rules;
	}

	public String render(Object object) {
		return new io.intino.itrules.Engine(this).render(object);
	}
}