package io.intino.plugin.project;

import org.siani.itrules.*;

import java.util.Locale;

import static org.siani.itrules.LineSeparator.*;

public class LegioFileTemplate extends Template {

	protected LegioFileTemplate(Locale locale, LineSeparator separator) {
		super(locale, separator);
	}

	public static Template create() {
		return new LegioFileTemplate(Locale.ENGLISH, LF).define();
	}

	public Template define() {
		add(
			rule().add((condition("type", "legio & empty"))).add(literal("dsl Legio\n\nArtifact(groupId = \"org.example\", version = \"1.0.0\") ")).add(mark("name")).add(literal(" as Platform\n\tModeling(version = \"2.0.0\") > Language(name = \"Verso\", version = \"LATEST\")\n\tBoxing(language = \"Konos\", version = \"2.0.0\")\n\tImports\n\t\tTest(groupId = \"junit\", artifactId = \"junit\", version = \"LATEST\")\n\n\tPack(mode = ModulesAndLibrariesLinkedByManifest)\n\nRepository(identifier = \"intino-maven\")\n\tLanguage(url = \"https://artifactory.intino.io/artifactory/releases\")\n\tRelease(url = \"https://artifactory.intino.io/artifactory/releases\")")),
			rule().add((condition("type", "legio")), not(condition("type", "empty"))).add(literal("dsl Legio\n\nArtifact(groupId = \"")).add(mark("groupId")).add(literal("\", version = \"")).add(mark("version")).add(literal("\") ")).add(mark("artifactId")).add(expression().add(literal(" as ")).add(mark("level", "firstUpperCase"))).add(literal("\n\t")).add(expression().add(mark("factory")).add(literal("\n")).add(literal("\t"))).add(literal("\n\tImports")).add(expression().add(literal("\n")).add(literal("\t\t")).add(mark("dependency").multiple("\n"))).add(literal("\n\tPack(mode = ModulesAndLibrariesLinkedByManifest)\n\t")).add(expression().add(mark("distribution").multiple("\n"))).add(literal("\n\n")).add(expression().add(mark("isIntino")).add(literal("\n")).add(literal("Repository(identifier = \"intino-maven\")")).add(literal("\n")).add(literal("\tRelease(url = \"https://artifactory.intino.io/artifactory/releases\")")).add(literal("\n")).add(literal("\tLanguage(url = \"https://artifactory.intino.io/artifactory/releases\")"))).add(literal("\n")).add(expression().add(mark("repository").multiple("\n"))),
			rule().add((condition("type", "release")), (condition("trigger", "repository"))).add(literal("Repository(identifier = \"")).add(mark("id")).add(literal("\")\n\tRelease(url = \"")).add(mark("url")).add(literal("\")")),
			rule().add((condition("type", "snapshot")), (condition("trigger", "repository"))).add(literal("Repository(identifier = \"")).add(mark("id")).add(literal("\")\n\tSnapshot(url = \"")).add(mark("url")).add(literal("\")")),
			rule().add((condition("type", "dependency"))).add(mark("type", "FirstUpperCase")).add(literal("(groupId = \"")).add(mark("groupId")).add(literal("\", artifactId = \"")).add(mark("artifactId")).add(literal("\", version = \"")).add(mark("version")).add(literal("\")")),
			rule().add((condition("type", "factory"))).add(literal("Modeling(version = \"2.0.0\")\n\tLanguage(name = \"")).add(mark("dsl")).add(literal("\", version = \"")).add(mark("dslVersion")).add(literal("\")")),
			rule().add((condition("type", "distribution"))).add(literal("Distribution(\"")).add(mark("id")).add(literal("\")\n\tRelease(\"")).add(mark("url")).add(literal("\")"))
		);
		return this;
	}
}