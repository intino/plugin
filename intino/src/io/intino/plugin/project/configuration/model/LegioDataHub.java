package io.intino.plugin.project.configuration.model;

import io.intino.Configuration;
import io.intino.plugin.lang.psi.TaraMogram;

public class LegioDataHub extends LegioDependency implements Configuration.Artifact.Dependency.DataHub {

	public LegioDataHub(LegioArtifact artifact, TaraMogram node) {
		super(node);
	}

	@Override
	public String scope() {
		return "Compile";
	}
}
