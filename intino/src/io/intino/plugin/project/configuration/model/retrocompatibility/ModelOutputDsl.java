package io.intino.plugin.project.configuration.model.retrocompatibility;

import io.intino.Configuration;
import io.intino.plugin.lang.psi.TaraMogram;
import io.intino.plugin.lang.psi.impl.TaraPsiUtil;
import io.intino.plugin.project.configuration.model.LegioArtifact;

import java.util.jar.Attributes;

public class ModelOutputDsl implements Configuration.Artifact.Dsl.OutputDsl {
	private final LegioArtifact artifact;
	private final TaraMogram mogram;
	private final Attributes parameters;

	public ModelOutputDsl(LegioArtifact artifact, TaraMogram mogram, Attributes parameters) {
		this.artifact = artifact;
		this.mogram = mogram;
		this.parameters = parameters;
	}

	@Override
	public String name() {
		String outLanguage = TaraPsiUtil.parameterValue(mogram, "outLanguage");
		return outLanguage == null ? artifact.name() : outLanguage;
	}

	@Override
	public String version() {
		return artifact.version();
	}

	@Override
	public Configuration.Artifact.Dsl.OutputBuilder builder() {
		return null;
	}

	@Override
	public Configuration.Artifact.Dsl.Runtime runtime() {
		return null;
	}

	@Override
	public Configuration root() {
		return artifact.root();
	}

	@Override
	public Configuration.ConfigurationNode owner() {
		return artifact;
	}
}
