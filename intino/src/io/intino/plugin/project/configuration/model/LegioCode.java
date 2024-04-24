package io.intino.plugin.project.configuration.model;

import io.intino.Configuration;
import io.intino.plugin.lang.psi.TaraMogram;
import org.jetbrains.annotations.NotNull;

import static io.intino.plugin.lang.psi.impl.TaraPsiUtil.parameterValue;

class LegioCode implements Configuration.Artifact.Code {
	private final LegioArtifact artifact;
	private final TaraMogram code;

	public LegioCode(LegioArtifact artifact, TaraMogram code) {
		this.artifact = artifact;
		this.code = code;
	}

	@Override
	public String generationPackage() {
		String defaultPackage = defaultPackage();
		if (code == null) return defaultPackage;
		String targetPackage = parameterValue(code, "targetPackage", 0);
		return targetPackage != null ? targetPackage : defaultPackage;
	}

	@Override
	public String nativeLanguage() {
		return "Java";
	}

	@NotNull
	private String defaultPackage() {
		return (artifact.groupId() + "." + artifact.name()).replace("-", "").replace("_", "");
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
