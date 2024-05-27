package io.intino.plugin.build.maven;

import io.intino.itrules.template.Rule;
import io.intino.itrules.template.Template;

import java.util.ArrayList;
import java.util.List;

import static io.intino.itrules.template.condition.predicates.Predicates.*;
import static io.intino.itrules.template.outputs.Outputs.*;

public class UIAccessorPomTemplate extends Template {

	public List<Rule> ruleSet() {
		List<Rule> rules = new ArrayList<>();
		rules.add(rule().condition(allTypes("pom")).output(literal("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<project xmlns=\"http://maven.apache.org/POM/4.0.0\"\n\t\t xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/maven-v4_0_0.xsd\">\n\t<modelVersion>4.0.0</modelVersion>\n\n\t<groupId>")).output(placeholder("groupId", "lowercase")).output(literal("</groupId>\n\t<artifactId>")).output(placeholder("artifactId", "lowercase")).output(literal("</artifactId>\n\t<version>")).output(placeholder("version")).output(literal("</version>\n\t")).output(expression().output(literal("<licenses")).output(literal(">")).output(literal("\n")).output(literal("\t")).output(placeholder("license").multiple("\n")).output(literal("\n")).output(literal("</licenses")).output(literal(">"))).output(literal("\n\n\t<properties>\n\t\t<maven.compiler.source>1.8</maven.compiler.source>\n\t\t<maven.compiler.target>1.8</maven.compiler.target>\n\t\t<project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>\n\t</properties>\n\n\t<build>\n\t\t<outputDirectory>")).output(placeholder("outDirectory")).output(placeholder("artifactId", "lowercase")).output(literal("</outputDirectory>\n\t\t<directory>")).output(placeholder("buildDirectory")).output(placeholder("artifactId", "lowercase")).output(literal("</directory>\n\t\t<plugins>\n\t\t    ")).output(placeholder("build")).output(literal("\n\t\t\t<plugin>\n\t\t\t\t<groupId>org.codehaus.mojo</groupId>\n\t\t\t\t<artifactId>build-helper-maven-plugin</artifactId>\n\t\t\t\t<version>1.7</version>\n\t\t\t\t<executions>\n\t\t\t\t\t<execution>\n\t\t\t\t\t\t<id>add-source</id>\n\t\t\t\t\t\t<phase>generate-resources</phase>\n\t\t\t\t\t\t<goals>\n\t\t\t\t\t\t\t<goal>add-resource</goal>\n\t\t\t\t\t\t</goals>\n\t\t\t\t\t\t<configuration>\n\t\t\t\t\t\t\t<resources>\n                                <resource>\n                                    <directory>./</directory>\n                                    <includes>\n                                        <include>package.json</include>\n                                        <include>src/**/*</include>\n                                        <include>gen/**/*</include>\n                                        <include>res/**/*</include>\n                                    </includes>\n                                </resource>\n                            </resources>\n\t\t\t\t\t\t</configuration>\n\t\t\t\t\t</execution>\n\t\t\t\t</executions>\n\t\t\t</plugin>\n\t\t\t<plugin>\n\t\t\t\t<groupId>org.apache.maven.plugins</groupId>\n\t\t\t\t<artifactId>maven-source-plugin</artifactId>\n\t\t\t\t<version>3.0.1</version>\n\t\t\t\t<executions>\n\t\t\t\t\t<execution>\n\t\t\t\t\t\t<id>attach-sources</id>\n\t\t\t\t\t\t<goals>\n\t\t\t\t\t\t\t<goal>jar-no-fork</goal>\n\t\t\t\t\t\t</goals>\n\t\t\t\t\t</execution>\n\t\t\t\t</executions>\n\t\t\t\t<configuration>\n\t\t\t\t\t<useDefaultExcludes>false</useDefaultExcludes>\n\t\t\t\t</configuration>\n\t\t\t</plugin>\n\t\t</plugins>\n\t</build>\n\n\t<distributionManagement>\n\t\t")).output(placeholder("repository", "distribution").multiple("\n")).output(literal("\n\t</distributionManagement>\n</project>")));
		rules.add(rule().condition(all(allTypes("package"), trigger("build"))).output(literal("<plugin>\n    <groupId>com.github.eirslett</groupId>\n    <artifactId>frontend-maven-plugin</artifactId>\n    <version>1.6</version>\n    <executions>\n        ")).output(placeholder("nodeInstalled")).output(literal("\n        <execution>\n            <id>npm install</id>\n            <goals>\n                <goal>npm</goal>\n            </goals>\n            <configuration>\n                <arguments>run-script build</arguments>\n            </configuration>\n        </execution>\n    </executions>\n    <configuration>\n        <nodeVersion>v11.2.0</nodeVersion>\n        <installDirectory>${user.home}/</installDirectory>\n    </configuration>\n</plugin>")));
		rules.add(rule().condition(all(allTypes("repository", "Distribution"), trigger("distribution"))).output(literal("<repository>\n\t<id>")).output(placeholder("name")).output(literal("</id>\n\t<name>")).output(placeholder("name")).output(literal("</name>\n\t<url>")).output(placeholder("url")).output(literal("</url>\n</repository>")));
		rules.add(rule().condition(all(attribute("","false"), trigger("nodeinstalled"))).output(literal("<execution>\n    <id>install node and npm</id>\n    <goals>\n        <goal>install-node-and-npm</goal>\n    </goals>\n    <phase>generate-resources</phase>\n</execution>")));
		rules.add(rule().condition(trigger("nodeinstalled")));
		return rules;
	}

	public String render(Object object) {
		return new io.intino.itrules.Engine(this).render(object);
	}

	public String render(Object object, java.util.Map<String, io.intino.itrules.Formatter> formatters) {
		return new io.intino.itrules.Engine(this).addAll(formatters).render(object);
	}
}