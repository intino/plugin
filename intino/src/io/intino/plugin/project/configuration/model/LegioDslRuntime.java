package io.intino.plugin.project.configuration.model;

import io.intino.Configuration;

import java.util.jar.Attributes;

import static io.intino.builder.BuildConstants.*;

public class LegioDslRuntime implements Configuration.Artifact.Dsl.Runtime {
	private final LegioDsl legioDsl;
	private final Attributes attributes;

	public LegioDslRuntime(LegioDsl legioDsl, Attributes attributes) {
		this.legioDsl = legioDsl;
		this.attributes = attributes;
	}

	@Override
	public String groupId() {
		return attributes == null ? null : attributes.getValue(normalizeForManifest(RUNTIME_GROUP_ID));
	}

	@Override
	public String artifactId() {
		return attributes == null ? null : attributes.getValue(normalizeForManifest(RUNTIME_ARTIFACT_ID));
	}

	@Override
	public String version() {
		return attributes == null ? null : attributes.getValue(normalizeForManifest(RUNTIME_VERSION));
	}

	@Override
	public Configuration.ConfigurationNode owner() {
		return legioDsl;
	}

	@Override
	public Configuration root() {
		return legioDsl.root();
	}

	@Override
	public void version(String version) {

	}
}