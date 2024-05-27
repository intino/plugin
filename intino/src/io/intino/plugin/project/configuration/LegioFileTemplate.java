package io.intino.plugin.project.configuration;

import io.intino.itrules.template.Rule;
import io.intino.itrules.template.Template;

import java.util.ArrayList;
import java.util.List;

import static io.intino.itrules.template.condition.predicates.Predicates.*;
import static io.intino.itrules.template.outputs.Outputs.*;

public class LegioFileTemplate extends Template {

	public List<Rule> ruleSet() {
		List<Rule> rules = new ArrayList<>();
		rules.add(rule().condition(allTypes("legio", "project")).output(literal("dsl Legio\n\nProject(\"describes here your project\") ")).output(placeholder("name")).output(literal("\n\tRepository(\"intino-maven\")\n\t\tRelease(\"https://artifactory.intino.io/artifactory/releases\")\n")));
		rules.add(rule().condition(allTypes("legio", "empty")).output(literal("dsl Legio\n\nArtifact(groupId = \"")).output(expression().output(placeholder("groupId")).next(expression().output(literal("org.example")))).output(literal("\", version = \"1.0.0\") ")).output(placeholder("name")).output(literal("\n\t")).output(expression().output(placeholder("dsl").multiple("\n"))).output(literal("\n\tImports\n\t\tTest(groupId = \"junit\", artifactId = \"junit\", version = \"4.13\")\n\tPackage(mode = ModulesAndLibrariesLinkedByManifest)\nRunConfiguration local")));
		rules.add(rule().condition(all(allTypes("legio"), not(allTypes("empty")))).output(literal("dsl Legio\n\nArtifact(groupId = \"")).output(placeholder("groupId")).output(literal("\", version = \"")).output(placeholder("version")).output(literal("\") ")).output(placeholder("artifactId")).output(literal("\n\t")).output(expression().output(placeholder("dsl").multiple("\n"))).output(literal("\n\tImports")).output(expression().output(literal("\n")).output(literal("\t")).output(placeholder("dependency").multiple("\n"))).output(literal("\n\tPackage(mode = ModulesAndLibrariesLinkedByManifest)\n\t")).output(expression().output(placeholder("distribution").multiple("\n"))).output(literal("\n\n")).output(expression().output(placeholder("repository").multiple("\n"))));
		rules.add(rule().condition(all(allTypes("release"), trigger("repository"))).output(literal("Repository(identifier = \"")).output(placeholder("id")).output(literal("\")\n\tRelease(url = \"")).output(placeholder("url")).output(literal("\")")));
		rules.add(rule().condition(all(allTypes("snapshot"), trigger("repository"))).output(literal("Repository(identifier = \"")).output(placeholder("id")).output(literal("\")\n\tSnapshot(url = \"")).output(placeholder("url")).output(literal("\")")));
		rules.add(rule().condition(allTypes("dependency")).output(placeholder("type", "FirstUpperCase")).output(literal("(groupId = \"")).output(placeholder("groupId")).output(literal("\", artifactId = \"")).output(placeholder("artifactId")).output(literal("\", version = \"")).output(placeholder("version")).output(literal("\")")));
		rules.add(rule().condition(trigger("dsl")).output(literal("Dsl(name = \"")).output(placeholder("name")).output(literal("\", version = \"")).output(placeholder("version")).output(literal("\")")));
		rules.add(rule().condition(allTypes("distribution")).output(literal("Distribution(")).output(placeholder("id")).output(literal(")")));
		return rules;
	}

	public String render(Object object) {
		return new io.intino.itrules.Engine(this).render(object);
	}

	public String render(Object object, java.util.Map<String, io.intino.itrules.Formatter> formatters) {
		return new io.intino.itrules.Engine(this).addAll(formatters).render(object);
	}
}