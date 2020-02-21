package io.intino.plugin.project.configuration.model;

import io.intino.Configuration;
import io.intino.tara.lang.model.Node;

import static io.intino.plugin.lang.psi.impl.TaraPsiUtil.parameterValue;

public class LegioBox implements Configuration.Artifact.Box {
	private final LegioArtifact artifact;
	private final Node node;
	private String version;

	public LegioBox(LegioArtifact artifact, Node node) {
		this.artifact = artifact;
		this.node = node;
	}

	@Override
	public String language() {
		return parameterValue(node, "language", 0);
	}

	@Override
	public String version() {
		return version == null ? version = parameterValue(node, "version", 1) : version;
	}

	@Override
	public String effectiveVersion() {
		//TODO
		return "";
	}

	@Override
	public void effectiveVersion(String s) {

	}

	@Override
	public String targetPackage() {
		String targetPackage = parameterValue(node, "targetPackage");
		return targetPackage == null ? "box" : targetPackage;
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
