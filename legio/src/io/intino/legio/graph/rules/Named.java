package io.intino.legio.graph.rules;

import io.intino.tara.lang.model.Node;
import io.intino.tara.lang.model.rules.NodeRule;

public class Named implements NodeRule {

    @Override
    public boolean accept(Node node) {
        return !node.isAnonymous();
    }

    @Override
	public String errorMessage() {
		return "This element must have name";
	}
}
