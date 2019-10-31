package io.intino.legio.graph.rules;

import io.intino.tara.lang.model.Aspect;
import io.intino.tara.lang.model.Node;
import io.intino.tara.lang.model.Parameter;
import io.intino.tara.lang.model.rules.NodeRule;

import java.util.List;

public class CheckLevel implements NodeRule {

	public boolean accept(Node node) {
		final boolean needs = needsLanguageRepository(node);
		return !(needs && !hasLanguageRepository(node));
	}

	private boolean hasLanguageRepository(Node node) {
		for (Parameter parameter : node.parameters())
			if ((parameter.name().equals("language") || parameter.position() == 1) && !parameter.values().isEmpty())
				return true;
		return false;
	}

	private boolean needsLanguageRepository(Node node) {
		final List<Aspect> aspects = node.container().appliedAspects();
		for (Aspect aspect : aspects)
			if (aspect.type().equals("Platform") || aspect.type().equals("Product")) return true;
		return false;
	}

	@Override
	public String errorMessage() {
		return "Distribution needs language repository";
	}
}
