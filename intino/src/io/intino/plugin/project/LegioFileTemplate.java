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
			rule().add((condition("type", "legio & empty"))).add(literal("dsl Legio\n\nProject(groupId = \"org.sample\", version = \"1.0.0\") ")).add(mark("name")).add(literal("\n\tRepositories\n\t\tRelease(\"http://artifactory.siani.es/artifactory/libs-release\", \"siani-maven\")\n\t\tLanguage(\"https://artifactory.siani.es/artifactory/languages-release\", \"siani-maven\")\n\tDependencies\n\t\tTest(groupId = \"junit\", artifactId = \"junit\", version = \"LATEST\")\n\tFactory(\"")).add(mark("name", "firstLowerCase")).add(literal("\") as Platform(\"Verso\", \"LATEST\")\n\nLifeCycle\n\tPackage(type = ModulesAndLibrariesLinkedByManifest)\n\tDistribution\n\t\tRelease(\"http://artifactory.siani.es/artifactory/libs-release-local\", \"siani-maven\")")),
			rule().add((condition("type", "legio & !empty"))).add(literal("dsl Legio\n\nProject(groupId = \"")).add(mark("groupId")).add(literal("\", version = \"")).add(mark("version")).add(literal("\") ")).add(mark("artifactId")).add(literal("\n\tRepositories")).add(expression().add(literal("\n")).add(literal("\t\tLanguage(url = \"https://artifactory.siani.es/artifactory/languages-release\", \"siani-maven\")")).add(literal("\n")).add(literal("\t\tRelease(url = \"http://artifactory.siani.es/artifactory/libs-release\", \"siani-maven\")")).add(mark("isIntino")).add(literal("\t"))).add(expression().add(literal("\n")).add(literal("\t\t")).add(mark("repository").multiple("\n"))).add(literal("\n\tDependencies")).add(expression().add(literal("\n")).add(literal("\t\t")).add(mark("dependency").multiple("\n"))).add(expression().add(literal("\n")).add(literal("\t")).add(mark("factory"))).add(literal("\n\nLifeCycle\n\tPackage(ModulesAndLibrariesLinkedByManifest)\n\t")).add(expression().add(mark("distribution").multiple("\n"))),
			rule().add((condition("type", "release")), (condition("trigger", "repository"))).add(literal("Release(url = \"")).add(mark("url")).add(literal("\", \"")).add(mark("id")).add(literal("\")")),
			rule().add((condition("type", "snapshot")), (condition("trigger", "repository"))).add(literal("Snapshot(url = \"")).add(mark("url")).add(literal("\", \"")).add(mark("id")).add(literal("\")")),
			rule().add((condition("type", "dependency"))).add(mark("type", "FirstUpperCase")).add(literal("(groupId = \"")).add(mark("groupId")).add(literal("\", artifactId = \"")).add(mark("artifactId")).add(literal("\", version = \"")).add(mark("version")).add(literal("\")")),
			rule().add((condition("type", "factory"))).add(literal("Factory(\"")).add(mark("workingPackage")).add(literal("\") as ")).add(mark("level", "firstUpperCase")).add(literal("(\"")).add(mark("dsl")).add(literal("\", \"")).add(mark("dslVersion")).add(literal("\")")),
			rule().add((condition("type", "distribution"))).add(literal("Distribution\n\tRelease(\"")).add(mark("url")).add(literal("\", \"")).add(mark("id")).add(literal("\")"))
		);
		return this;
	}
}