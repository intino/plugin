package io.intino.legio.model.rules;

import io.intino.tara.language.model.Mogram;
import io.intino.tara.language.model.rules.NodeRule;

public class Named implements NodeRule {

    @Override
    public boolean accept(Mogram node) {
        return !node.isAnonymous();
    }

    @Override
	public String errorMessage() {
		return "This element must have name";
	}
}
