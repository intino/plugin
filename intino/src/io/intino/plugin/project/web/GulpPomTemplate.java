package io.intino.plugin.project.web;

import io.intino.itrules.RuleSet;
import io.intino.itrules.Template;

public class GulpPomTemplate extends Template {

	public RuleSet ruleSet() {
		return new RuleSet().add(
			rule().condition((type("pom"))).output(literal("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<project xmlns=\"http://maven.apache.org/POM/4.0.0\"\n\t\t xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/maven-v4_0_0.xsd\">\n\t<modelVersion>4.0.0</modelVersion>\n\n\t<groupId>")).output(mark("groupId", "lowercase")).output(literal("</groupId>\n\t<artifactId>")).output(mark("artifactId", "lowercase")).output(literal("</artifactId>\n\t<version>")).output(mark("version")).output(literal("</version>\n\n\t<properties>\n\t\t<maven.compiler.source>1.8</maven.compiler.source>\n\t\t<maven.compiler.target>1.8</maven.compiler.target>\n\t\t<project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>\n\t</properties>\n\t<build>\n\t\t<plugins>\n\t\t\t<plugin>\n\t\t\t\t<groupId>com.github.eirslett</groupId>\n\t\t\t\t<artifactId>frontend-maven-plugin</artifactId>\n\t\t\t\t<version>1.6</version>\n\t\t\t\t<executions>\n\t\t\t\t\t<execution>\n                        <id>gulp build</id>\n                        <goals>\n                            <goal>gulp</goal>\n                        </goals>\n                        <configuration>\n\t\t\t\t\t\t\t<arguments>")).output(mark("task")).output(literal("</arguments>\n\t\t\t\t\t\t\t<workingDirectory>${user.home}/</workingDirectory>\n                            <installDirectory>${user.home}/</installDirectory>\n                            <nodeVersion>v6.9.2</nodeVersion>\n\t\t\t\t\t\t</configuration>\n                    </execution>\n\t\t\t\t</executions>\n\t\t\t</plugin>\n\t\t</plugins>\n\t</build>\n</project>"))
		);
	}
}