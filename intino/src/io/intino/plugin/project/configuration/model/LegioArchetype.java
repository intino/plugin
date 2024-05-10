package io.intino.plugin.project.configuration.model;

import io.intino.Configuration;
import io.intino.plugin.lang.psi.TaraMogram;
import org.eclipse.aether.util.artifact.JavaScopes;

public class LegioArchetype extends LegioDependency implements Configuration.Artifact.Dependency.Archetype {

	public LegioArchetype(LegioArtifact artifact, TaraMogram mogram) {
		super(artifact, mogram);
	}

	@Override
	public String scope() {
		return JavaScopes.COMPILE;
	}
}
