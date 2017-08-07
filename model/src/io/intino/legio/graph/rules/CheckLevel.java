package io.intino.legio.graph.rules;

import io.intino.tara.lang.model.Facet;
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
		final List<Facet> facets = node.container().facets();
		for (Facet facet : facets) if (facet.type().equals("Platform") || facet.type().equals("Product")) return true;
		return false;
	}

	@Override
	public String errorMessage() {
		return "Distribution needs language repository";
	}
}
