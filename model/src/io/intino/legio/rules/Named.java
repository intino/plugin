package io.intino.legio.rules;

import tara.lang.model.Node;
import tara.lang.model.rules.NodeRule;

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
