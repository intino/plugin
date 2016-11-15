package io.intino.legio.plugin.project;

import org.siani.itrules.*;

import java.util.Locale;

import static org.siani.itrules.LineSeparator.*;

public class LegioTemplate extends Template {

	protected LegioTemplate(Locale locale, LineSeparator separator) {
		super(locale, separator);
	}

	public static Template create() {
		return new LegioTemplate(Locale.ENGLISH, LF).define();
	}

	public Template define() {
		add(
			rule().add((condition("type", "legio"))).add(literal("dsl Legio\n\nProject(groupId = \"org.sample\", version = \"1.0.0\") ")).add(mark("name")).add(literal("\n\tRepositories\n\t\tRelease(url = \"http://artifactory.siani.es/artifactory/libs-release\", \"siani-maven\")\n\t\tLanguage(url = \"https://artifactory.siani.es/artifactory/languages-release\", \"siani-maven\")\n\tDependencies\n\t\tTest(groupId = \"junit\", artifactId = \"junit\", version = \"LATEST\")\n\tFactory as Platform\n\t\tLanguage(version = \"LATEST\") Verso\n\t\tGeneration > inPackage = \"")).add(mark("name", "firstLowerCase")).add(literal("\"\n\nLifeCycle\n\tPackage(type = AllDependenciesLinkedByManifest)\n\tDistribution(url = \"http://artifactory.siani.es/artifactory/libs-release-local\", \"siani-maven\")\n"))
		);
		return this;
	}
}