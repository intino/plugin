package io.intino.plugin.project.configuration.model;

import io.intino.Configuration;
import io.intino.plugin.lang.psi.TaraNode;

import static io.intino.plugin.lang.psi.impl.TaraPsiUtil.parameterValue;

class LegioCode implements Configuration.Artifact.Code {
	private final LegioArtifact artifact;
	private final TaraNode code;

	public LegioCode(LegioArtifact artifact, TaraNode code) {
		this.artifact = artifact;
		this.code = code;
	}

	@Override
	public String generationPackage() {
		if (code == null) return (artifact.groupId() + "." + artifact.name()).replace("-", "").replace("_", "");
		return parameterValue(code, "targetPackage", 0);
	}

	@Override
	public String modelPackage() {
		String modelPackage = parameterValue(code, "modelSubPackage", 1);
		if (code == null || modelPackage == null) return Configuration.Artifact.Code.super.modelPackage();
		return modelPackage;
	}

	@Override
	public String boxPackage() {
		String boxPackage = parameterValue(code, "boxSubPackage", 1);
		if (code == null || boxPackage == null) return Configuration.Artifact.Code.super.boxPackage();
		return boxPackage;
	}

	@Override
	public String nativeLanguage() {
		return "Java";
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
