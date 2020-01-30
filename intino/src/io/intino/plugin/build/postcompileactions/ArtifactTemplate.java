package io.intino.plugin.build.postcompileactions;

import io.intino.itrules.RuleSet;
import io.intino.itrules.Template;

public class ArtifactTemplate extends Template {

	public RuleSet ruleSet() {
		return new RuleSet().add(
			rule().condition((allTypes("artifact","legio"))).output(literal("dsl Legio\n\nArtifact(groupId = \"")).output(mark("groupId", "lowercase")).output(literal("\", version = \"1.0.0\") ")).output(mark("artifactId", "lowercase")).output(literal("\n\tWebImports\n\t\tWebArtifact(\"io.intino.alexandria\", \"ui-framework-elements\", \"LATEST\") alexandria-ui-elements\n\n")).output(mark("repository").multiple("\n\n")),
			rule().condition((type("repository"))).output(literal("Repository(\"")).output(mark("id")).output(literal("\")\n\t")).output(mark("url").multiple("\n")),
			rule().condition((trigger("url"))).output(literal("Release(\"")).output(mark("")).output(literal("\")"))
		);
	}
}