package io.intino.legio.model.rules;

import io.intino.tara.language.model.Mogram;
import io.intino.tara.language.model.rules.MogramRule;

public class Named implements MogramRule {

    @Override
    public boolean accept(Mogram node) {
        return !node.isAnonymous();
    }

    @Override
	public String errorMessage() {
		return "This element must have name";
	}
}
