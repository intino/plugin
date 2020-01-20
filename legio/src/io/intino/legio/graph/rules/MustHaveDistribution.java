package io.intino.legio.graph.rules;

import io.intino.tara.lang.model.Node;
import io.intino.tara.lang.model.rules.NodeRule;

public class MustHaveDistribution implements NodeRule {
	public boolean accept(Node node) {
		return node.container().components().stream().anyMatch(n -> n.type().contains("Distribution"));
	}

	@Override
	public String errorMessage() {
		return "Deployment need a distribution configuration";
	}
}