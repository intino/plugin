package io.intino.plugin.project.configuration.model;

import io.intino.Configuration;
import io.intino.plugin.lang.psi.TaraMogram;

public class LegioArchetype extends LegioDependency implements Configuration.Artifact.Dependency.Archetype {

	public LegioArchetype(LegioArtifact artifact, TaraMogram mogram) {
		super(mogram);
	}
}
