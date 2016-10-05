package org.siani.legio.rules;

import tara.lang.model.Node;
import tara.lang.model.rules.composition.NodeRule;

import java.util.List;

public class Named implements NodeRule {

	public int min() {
		return 1;
	}

	public int max() {
		return 1;
	}

	public boolean accept(List<Node> nodes) {
		for (Node node : nodes) if (node.isAnonymous()) return false;
		return true;
	}

	@Override
	public String errorMessage() {
		return "This element must have name";
	}
}
