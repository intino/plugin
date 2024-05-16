package io.intino.plugin.project.configuration.model.retrocompatibility;

import io.intino.Configuration;
import org.jetbrains.annotations.NotNull;

import java.util.jar.Attributes;

import static io.intino.builder.BuildConstants.*;

public class LegioModelRuntime implements Configuration.Artifact.Dsl.Runtime {
	public static final String GROUP_ID = "io.intino.magritte";
	public static final String ARTIFACT_ID = "framework";
	private final LegioModel model;
	private final String[] coors;

	public LegioModelRuntime(LegioModel model, Attributes parameters) {
		this.model = model;
		this.coors = runtimeCoors(parameters).split(":");
	}

	@Override
	public String groupId() {
		return coors[0];
	}

	@Override
	public String artifactId() {
		return coors[1];
	}

	@Override
	public String version() {
		return coors[2];
	}

	@Override
	public void version(String version) {

	}

	@Override
	public Configuration root() {
		return model.root();
	}

	@Override
	public Configuration.ConfigurationNode owner() {
		return model;
	}

	private String runtimeCoors(Attributes parameters) {
		if (isMagritteLibrary(model.name())) return magritteID(model.realVersion());
		String framework = parameters.getValue("framework");
		return framework != null ? framework : coors(parameters);
	}

	@NotNull
	private static String magritteID(String version) {
		return GROUP_ID + ":" + ARTIFACT_ID + ":" + version;
	}

	private static boolean isMagritteLibrary(String language) {
		return "Proteo".equals(language) || "Meta".equals(language);
	}

	private static String coors(Attributes tara) {
		return String.join(":", tara.getValue(normalizeForManifest(RUNTIME_GROUP_ID)),
				tara.getValue(normalizeForManifest(RUNTIME_ARTIFACT_ID)),
				tara.getValue(normalizeForManifest(RUNTIME_VERSION)));
	}
}
