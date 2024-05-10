package io.intino.plugin.project.configuration.model;

import io.intino.Configuration;
import io.intino.plugin.lang.psi.TaraMogram;
import org.eclipse.aether.util.artifact.JavaScopes;

public class LegioDataHub extends LegioDependency implements Configuration.Artifact.Dependency.DataHub {

	public LegioDataHub(LegioArtifact artifact, TaraMogram node) {
		super(artifact, node);
	}

	@Override
	public String scope() {
		return JavaScopes.COMPILE;
	}
}
