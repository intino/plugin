package io.intino.legio.rules;

import tara.lang.model.Node;
import tara.lang.model.rules.composition.NodeRule;

import java.util.List;

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
