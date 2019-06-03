package io.intino.plugin.project;

import io.intino.itrules.RuleSet;
import io.intino.itrules.Template;

public class LegioFileTemplate extends Template {

	public RuleSet ruleSet() {
		return new RuleSet().add(
				rule().condition((allTypes("legio", "empty"))).output(literal("dsl Legio\n\nArtifact(groupId = \"org.example\", version = \"1.0.0\") ")).output(mark("name")).output(literal(" as Product\n\tModel(language = \"Pfroteo\", version = \"LATEST\", sdk = \"LATEST\")\n\tBox(language = \"Konos\", version = \"LATEST\", sdk = \"LATEST\")\n\tImports\n\t\tTest(groupId = \"junit\", artifactId = \"junit\", version = \"LATEST\")\n\n\tPackage(mode = ModulesAndLibrariesLinkedByManifest)\n\nRepository(identifier = \"intino-maven\")\n\tLanguage(url = \"https://artifactory.intino.io/artifactory/releases\")\n\tRelease(url = \"https://artifactory.intino.io/artifactory/releases\")")),
				rule().condition((type("legio")), not(type("empty"))).output(literal("dsl Legio\n\nArtifact(groupId = \"")).output(mark("groupId")).output(literal("\", version = \"")).output(mark("version")).output(literal("\") ")).output(mark("artifactId")).output(expression().output(literal(" as ")).output(mark("level", "firstUpperCase"))).output(literal("\n\t")).output(expression().output(mark("factory")).output(literal("\n")).output(literal(""))).output(literal("\n\tImports")).output(expression().output(literal("\n")).output(literal("\t\t")).output(mark("dependency").multiple("\n"))).output(literal("\n\tPackage(mode = ModulesAndLibrariesLinkedByManifest)\n\t")).output(expression().output(mark("distribution").multiple("\n"))).output(literal("\n\n")).output(expression().output(mark("isIntino")).output(literal("\n")).output(literal("Repository(identifier = \"intino-maven\")")).output(literal("\n")).output(literal("\tRelease(url = \"https://artifactory.intino.io/artifactory/releases\")")).output(literal("\n")).output(literal("\tLanguage(url = \"https://artifactory.intino.io/artifactory/releases\")"))).output(literal("\n")).output(expression().output(mark("repository").multiple("\n"))),
				rule().condition((type("release")), (trigger("repository"))).output(literal("Repository(identifier = \"")).output(mark("id")).output(literal("\")\n\tRelease(url = \"")).output(mark("url")).output(literal("\")")),
				rule().condition((type("snapshot")), (trigger("repository"))).output(literal("Repository(identifier = \"")).output(mark("id")).output(literal("\")\n\tSnapshot(url = \"")).output(mark("url")).output(literal("\")")),
				rule().condition((type("dependency"))).output(mark("type", "FirstUpperCase")).output(literal("(groupId = \"")).output(mark("groupId")).output(literal("\", artifactId = \"")).output(mark("artifactId")).output(literal("\", version = \"")).output(mark("version")).output(literal("\")")),
				rule().condition((type("factory"))).output(literal("Model(language = \"")).output(mark("dsl")).output(literal("\", version = \"")).output(mark("dslVersion")).output(literal("\", sdk = \"LATEST\")")),
				rule().condition((type("distribution"))).output(literal("Distribution(")).output(mark("id")).output(literal(")"))
		);
	}
}