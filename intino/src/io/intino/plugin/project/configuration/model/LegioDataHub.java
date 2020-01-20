package io.intino.plugin.project.configuration.model;

import io.intino.plugin.dependencyresolution.DependencyAuditor;
import io.intino.plugin.lang.psi.TaraNode;
import io.intino.tara.compiler.shared.Configuration;

public class LegioDataHub extends LegioDependency implements Configuration.Artifact.Dependency.DataHub {

	public LegioDataHub(LegioArtifact artifact, DependencyAuditor auditor, TaraNode node) {
		super(artifact, auditor, node);
	}
}
