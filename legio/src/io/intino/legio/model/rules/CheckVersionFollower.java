package io.intino.legio.model.rules;

import io.intino.tara.language.model.Mogram;
import io.intino.tara.language.model.rules.MogramRule;

public class CheckVersionFollower implements MogramRule {


	public boolean accept(Mogram mogram) {
		boolean artifactVersionFollower = mogram.appliedFacets().stream().anyMatch(a -> a.type().equals("ArtifactVersionFollower"));
		boolean versionAttribute = hasVersionAttribute(mogram);
		if (versionAttribute) return !artifactVersionFollower;
		else return artifactVersionFollower;
	}

	private boolean hasVersionAttribute(Mogram mogram) {
		return mogram.parameters().stream().anyMatch(p -> p.name().equals("version")) || mogram.parameters().size() == 3;
	}

	@Override
	public String errorMessage() {
		return "Should include a version parameter or add Facet 'ArtifactVersionFollower'";
	}
}
