package io.intino.plugin.project.configuration.model;

import io.intino.Configuration;
import io.intino.plugin.dependencyresolution.DependencyAuditor;
import io.intino.plugin.lang.psi.TaraNode;

public class LegioDataHub extends LegioDependency implements Configuration.Artifact.Dependency.DataHub {

	public LegioDataHub(LegioArtifact artifact, DependencyAuditor auditor, TaraNode node) {
		super(artifact, auditor, node);
	}

	@Override
	public String scope() {
		return "Compile";
	}
}
