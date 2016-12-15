package io.intino.plugin.dependencyresolution.web;

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
			rule().add((condition("type", "pom"))).add(literal("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<project xmlns=\"http://maven.apache.org/POM/4.0.0\"\n\t\t xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/maven-v4_0_0.xsd\">\n\t<modelVersion>4.0.0</modelVersion>\n\n\t<groupId>")).add(mark("groupId", "lowercase")).add(literal("</groupId>\n\t<artifactId>")).add(mark("artifactId", "lowercase")).add(literal("</artifactId>\n\t<version>")).add(mark("version")).add(literal("</version>\n\n\t<properties>\n\t\t<maven.compiler.source>1.8</maven.compiler.source>\n\t\t<maven.compiler.target>1.8</maven.compiler.target>\n\t\t<project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>\n\t</properties>\n\n\t<build>\n\t\t<plugins>\n\t\t\t<plugin>\n\t\t\t\t<groupId>com.github.eirslett</groupId>\n\t\t\t\t<artifactId>frontend-maven-plugin</artifactId>\n\t\t\t\t<version>1.3</version>\n\t\t\t\t<executions>\n\t\t\t\t\t<execution>\n\t\t\t\t\t\t<id>install node and npm</id>\n\t\t\t\t\t\t<goals>\n\t\t\t\t\t\t\t<goal>install-node-and-npm</goal>\n\t\t\t\t\t\t</goals>\n\t\t\t\t\t\t<phase>generate-resources</phase>\n\t\t\t\t\t</execution>\n\t\t\t\t\t<execution>\n                        <id>npm install</id>\n                        <goals>\n                            <goal>npm</goal>\n                        </goals>\n                        <configuration>\n\t\t\t\t\t\t\t<workingDirectory>${user.home}/.node/node</workingDirectory>\n\t\t\t\t\t\t</configuration>\n                    </execution>\n\t\t\t\t\t<execution>\n\t\t\t\t\t\t<id>bower install</id>\n\t\t\t\t\t\t<goals>\n\t\t\t\t\t\t\t<goal>bower</goal>\n\t\t\t\t\t\t</goals>\n\t\t\t\t\t\t<configuration>\n\t\t\t\t\t\t\t<workingDirectory>${user.home}/.node/node</workingDirectory>\n\t\t\t\t\t\t</configuration>\n\t\t\t\t\t</execution>\n\t\t\t\t</executions>\n\t\t\t\t<configuration>\n\t\t\t\t\t<nodeVersion>v6.9.2</nodeVersion>\n\t\t\t\t\t<installDirectory>${user.home}/.node</installDirectory>\n\t\t\t\t</configuration>\n\n\t\t\t</plugin>\n\t\t</plugins>\n\t</build>\n</project>"))
		);
		return this;
	}
}