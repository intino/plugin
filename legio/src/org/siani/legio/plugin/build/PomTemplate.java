package org.siani.legio.plugin.build;

import org.siani.itrules.*;

import java.util.Locale;

import static org.siani.itrules.LineSeparator.*;

public class PomTemplate extends Template {

	protected PomTemplate(Locale locale, LineSeparator separator) {
		super(locale, separator);
	}

	public static Template create() {
		return new PomTemplate(Locale.ENGLISH, LF).define();
	}

	public Template define() {
		add(
			rule().add((condition("type", "pom"))).add(literal("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<project xmlns=\"http://maven.apache.org/POM/4.0.0\"\n         xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/maven-v4_0_0.xsd\">\n    <modelVersion>4.0.0</modelVersion>\n\n    <groupId>")).add(mark("groupId", "lowercase")).add(literal("</groupId>\n    <artifactId>")).add(mark("artifactId")).add(literal("</artifactId>\n    <version>")).add(mark("version")).add(literal("</version>\n\n    <properties>\n        <maven.compiler.source>1.8</maven.compiler.source>\n        <maven.compiler.target>1.8</maven.compiler.target>\n    </properties>\n\n\t<build>\n\t\t<sourceDirectory>src</sourceDirectory>\n\t\t<outputDirectory>out/production/")).add(mark("artifactId", "lowercase")).add(literal("</outputDirectory>\n\t\t<testOutputDirectory>out/test/")).add(mark("artifactId", "lowercase")).add(literal("</testOutputDirectory>\n\t\t<directory>out/build/")).add(mark("artifactId", "lowercase")).add(literal("</directory>\n\t\t<plugins>\n\t\t\t<plugin>\n\t\t\t\t<groupId>org.apache.maven.plugins</groupId>\n\t\t\t\t<artifactId>maven-source-plugin</artifactId>\n\t\t\t\t<executions>\n\t\t\t\t\t<execution>\n\t\t\t\t\t\t<id>attach-sources</id>\n\t\t\t\t\t\t<goals>\n\t\t\t\t\t\t\t<goal>jar</goal>\n\t\t\t\t\t\t</goals>\n\t\t\t\t\t</execution>\n\t\t\t\t</executions>\n\t\t\t</plugin>\n\t\t</plugins>\n\t</build>\n\n\t<repositories>\n        ")).add(mark("repository")).add(literal("\n    </repositories>\n\n    <dependencies>\n        ")).add(mark("dependency").multiple("\n")).add(literal("\n    </dependencies>\n</project>")),
			rule().add((condition("type", "repository"))).add(literal("<repository>\n\t<id>")).add(mark("id")).add(literal("</id>\n\t<name>")).add(mark("id")).add(literal("-")).add(mark("type")).add(literal("</name>\n\t<url>")).add(mark("url")).add(literal("</url>\n</repository>")),
			rule().add((condition("type", "dependency")), (condition("trigger", "dependency"))).add(literal("<dependency>\n    <groupId>")).add(mark("groupId")).add(literal("</groupId>\n    <artifactId>")).add(mark("artifactId")).add(literal("</artifactId>\n    <version>")).add(mark("version")).add(literal("</version>\n</dependency>"))
		);
		return this;
	}
}