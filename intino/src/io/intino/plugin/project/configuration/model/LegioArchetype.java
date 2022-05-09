package io.intino.plugin.project.configuration.model;

import io.intino.Configuration;
import io.intino.plugin.dependencyresolution.DependencyAuditor;
import io.intino.plugin.lang.psi.TaraNode;

public class LegioArchetype extends LegioDependency implements Configuration.Artifact.Dependency.Archetype {

	public LegioArchetype(LegioArtifact artifact, DependencyAuditor auditor, TaraNode node) {
		super(artifact, auditor, node);
	}

	@Override
	public String scope() {
		return "Compile";
	}
}
