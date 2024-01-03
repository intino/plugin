package io.intino.legio.model.rules;

import io.intino.tara.language.model.Mogram;
import io.intino.tara.language.model.rules.NodeRule;

public class MustHaveDistribution implements NodeRule {
	public boolean accept(Mogram node) {
		return node.container().components().stream().anyMatch(n -> n.type().contains("Distribution"));
	}

	@Override
	public String errorMessage() {
		return "Deployment need a distribution configuration";
	}
}