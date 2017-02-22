package io.intino.plugin.build.maven;

import org.siani.itrules.*;

import java.util.Locale;

import static org.siani.itrules.LineSeparator.*;

public class ActivityPomTemplate extends Template {

	protected ActivityPomTemplate(Locale locale, LineSeparator separator) {
		super(locale, separator);
	}

	public static Template create() {
		return new ActivityPomTemplate(Locale.ENGLISH, LF).define();
	}

	public Template define() {
		add(
			rule().add((condition("type", "pom"))).add(literal("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<project xmlns=\"http://maven.apache.org/POM/4.0.0\"\n\t\t xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/maven-v4_0_0.xsd\">\n\t<modelVersion>4.0.0</modelVersion>\n\n\t<groupId>")).add(mark("groupId", "lowercase")).add(literal("</groupId>\n\t<artifactId>")).add(mark("artifactId", "lowercase")).add(literal("</artifactId>\n\t<version>")).add(mark("version")).add(literal("</version>\n\t")).add(expression().add(literal("<licenses>")).add(literal("\n")).add(literal("\t\t")).add(mark("license").multiple("\n")).add(literal("\n")).add(literal("\t</licenses>"))).add(literal("\n\n\t<properties>\n\t\t<maven.compiler.source>1.8</maven.compiler.source>\n\t\t<maven.compiler.target>1.8</maven.compiler.target>\n\t\t<project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>\n\t</properties>\n\n\t<build>\n\t\t<outputDirectory>")).add(mark("outDirectory")).add(mark("artifactId", "lowercase")).add(literal("</outputDirectory>\n\t\t<directory>")).add(mark("buildDirectory")).add(mark("artifactId", "lowercase")).add(literal("</directory>\n\t\t<plugins>\n\t\t\t<plugin>\n\t\t\t\t<groupId>org.codehaus.mojo</groupId>\n\t\t\t\t<artifactId>build-helper-maven-plugin</artifactId>\n\t\t\t\t<version>1.7</version>\n\t\t\t\t<executions>\n\t\t\t\t\t<execution>\n\t\t\t\t\t\t<id>add-source</id>\n\t\t\t\t\t\t<phase>generate-sources</phase>\n\t\t\t\t\t\t<goals>\n\t\t\t\t\t\t\t<goal>add-source</goal>\n\t\t\t\t\t\t</goals>\n\t\t\t\t\t\t<configuration>\n\t\t\t\t\t\t\t<sources>\n\t\t\t\t\t\t\t\t<source>./src/widgets/</source>\n\t\t\t\t\t\t\t</sources>\n\t\t\t\t\t\t</configuration>\n\t\t\t\t\t</execution>\n\t\t\t\t</executions>\n\t\t\t</plugin>\n\t\t\t<plugin>\n\t\t\t\t<groupId>org.apache.maven.plugins</groupId>\n\t\t\t\t<artifactId>maven-source-plugin</artifactId>\n\t\t\t\t<version>3.0.1</version>\n\t\t\t\t<executions>\n\t\t\t\t\t<execution>\n\t\t\t\t\t\t<id>attach-sources</id>\n\t\t\t\t\t\t<goals>\n\t\t\t\t\t\t\t<goal>jar-no-fork</goal>\n\t\t\t\t\t\t</goals>\n\t\t\t\t\t</execution>\n\t\t\t\t</executions>\n\t\t\t\t<configuration>\n\t\t\t\t\t<useDefaultExcludes>false</useDefaultExcludes>\n\t\t\t\t</configuration>\n\t\t\t</plugin>\n\t\t</plugins>\n\t</build>\n\n\t<distributionManagement>\n\t\t")).add(mark("repository", "distribution").multiple("\n")).add(literal("\n\t</distributionManagement>\n</project>")),
			rule().add((condition("type", "repository")), (condition("type", "Distribution")), (condition("trigger", "distribution"))).add(literal("<repository>\n\t<id>")).add(mark("name")).add(literal("</id>\n\t<name>")).add(mark("name")).add(literal("</name>\n\t<url>")).add(mark("url")).add(literal("</url>\n</repository>"))
		);
		return this;
	}
}