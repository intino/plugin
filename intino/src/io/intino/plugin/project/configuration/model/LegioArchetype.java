package io.intino.plugin.project.configuration.model;

import io.intino.Configuration;
import io.intino.plugin.lang.psi.TaraMogram;

public class LegioArchetype extends LegioDependency implements Configuration.Artifact.Dependency.Archetype {

	public LegioArchetype(LegioArtifact artifact, TaraMogram node) {
		super(node);
	}

	@Override
	public String scope() {
		return "Compile";
	}
}
