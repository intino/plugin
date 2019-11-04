package io.intino.plugin.project.configuration;

import io.intino.legio.graph.Artifact;
import io.intino.tara.compiler.shared.Configuration;

public class LegioBox implements Configuration.Box {

	private final Artifact.Box box;

	public LegioBox(Artifact.Box box) {
		this.box = box;
	}

	@Override
	public String language() {
		return box.language();
	}

	@Override
	public String version() {
		return box.effectiveVersion().isEmpty() ? box.effectiveVersion() : box.version();
	}

	@Override
	public String effectiveVersion() {
		return box.effectiveVersion();
	}

	@Override
	public String targetPackage() {
		return box.targetPackage();
	}
}
