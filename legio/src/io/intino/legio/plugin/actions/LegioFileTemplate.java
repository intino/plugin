package io.intino.legio.plugin.actions;

import org.siani.itrules.LineSeparator;
import org.siani.itrules.Template;

import java.util.Locale;

import static org.siani.itrules.LineSeparator.LF;

public class LegioFileTemplate extends Template {

	protected LegioFileTemplate(Locale locale, LineSeparator separator) {
		super(locale, separator);
	}

	public static Template create() {
		return new LegioFileTemplate(Locale.ENGLISH, LF).define();
	}

	public Template define() {
		add(
				rule().add((condition("type", "legio"))).add(literal("dsl Legio\n\nProject(groupId = \"")).add(mark("groupId")).add(literal("\", version = \"")).add(mark("version")).add(literal("\") ")).add(mark("artifactId")).add(literal("\n\tRepositories")).add(expression().add(literal("\n")).add(literal("\t\tLanguage(url = \"https://artifactory.siani.es/artifactory/languages-release\", \"siani-maven\")")).add(mark("isTara"))).add(expression().add(literal("\n")).add(literal("\t\t")).add(mark("repository").multiple("\n"))).add(expression().add(literal("\n")).add(literal("\t\t")).add(mark("distribution"))).add(literal("\n\tDependencies")).add(expression().add(literal("\n")).add(literal("\t\t")).add(mark("dependency").multiple("\n"))).add(expression().add(literal("\n")).add(literal("\t")).add(mark("factory"))),
				rule().add((condition("type", "release")), (condition("trigger", "repository"))).add(literal("Release(url = \"")).add(mark("url")).add(literal("\", \"")).add(mark("id")).add(literal("\")")),
				rule().add((condition("type", "snapshot")), (condition("trigger", "repository"))).add(literal("Snapshot(url = \"")).add(mark("url")).add(literal("\", \"")).add(mark("id")).add(literal("\")")),
				rule().add((condition("type", "distribution"))).add(literal("Distribution(url = \"")).add(mark("url")).add(literal("\", \"")).add(mark("id")).add(literal("\")")),
				rule().add((condition("type", "dependency"))).add(mark("type", "FirstUpperCase")).add(literal("(groupId = \"")).add(mark("groupId")).add(literal("\", artifactId = \"")).add(mark("artifactId")).add(literal("\", version = \"")).add(mark("version")).add(literal("\")")),
				rule().add((condition("type", "factory"))).add(literal("Factory as ")).add(mark("level", "firstUpperCase")).add(literal("\n\tgenerationPackage = \"")).add(mark("workingPackage")).add(literal("\"\n\tModeling(language=\"")).add(mark("dsl")).add(literal("\", version=\"")).add(mark("dslVersion")).add(literal("\")"))
		);
		return this;
	}
}