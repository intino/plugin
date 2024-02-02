package io.intino.plugin.dependencyresolution.web;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import io.intino.Configuration.Artifact;
import io.intino.Configuration.Repository;
import io.intino.itrules.Frame;
import io.intino.itrules.FrameBuilder;
import org.apache.commons.lang3.SystemProperties;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

public class PackageJsonCreator {
	private static final Logger logger = Logger.getInstance(PackageJsonCreator.class.getName());
	private final Artifact artifact;
	private final List<Artifact.WebComponent> webComponents;
	private final List<Artifact.WebResolution> resolutions;
	private final WebArtifactResolver webArtifactResolver;

	public PackageJsonCreator(Module module, Artifact artifact, List<Repository> repositories, File destination) {
		this.artifact = artifact;
		this.webComponents = artifact.webComponents();
		this.resolutions = artifact.webResolutions();
		this.webArtifactResolver = new WebArtifactResolver(module, artifact, repositories, destination);

	}

	public void createPackageFile(File rootDirectory) {
		write(new Package_jsonTemplate().render(packageFrame().toFrame()), new File(rootDirectory, "package.json"));
	}

	public void extractArtifacts() {
		if (isWindows()) webArtifactResolver.extractArtifacts();
	}

	@NotNull
	private FrameBuilder packageFrame() {
		List<JsonObject> packages = webArtifactResolver.resolveArtifacts();
		FrameBuilder builder = baseFrame().add("package");
		if (isMacOS()) builder.add("fsevents", "");
		Map<String, String> dependencies = collectDependencies(packages);
		dependencies.forEach((key, value) -> builder.add("dependency", new FrameBuilder().add("name", key).add("version", value)));
		resolutions.forEach(resolution -> builder.add("resolution", resolutionFrameFrom(resolution)));
		packages.stream().map(this::resolutionFrameFrom).filter(Objects::nonNull).forEach(frames -> builder.add("resolution", frames));
		return builder;
	}

	private static boolean isWindows() {
		return SystemProperties.getOsName().toLowerCase().startsWith("windows");
	}

	private static boolean isMacOS() {
		return SystemProperties.getOsName().toLowerCase().startsWith("mac");
	}

	@NotNull
	private Map<String, String> collectDependencies(List<JsonObject> packages) {
		Map<String, String> dependencies = new LinkedHashMap<>();
		webComponents.forEach(c -> dependencies.putIfAbsent(c.name(), c.version()));
		packages.forEach(p -> dependenciesFrom(p).forEach(dependencies::putIfAbsent));
		return dependencies;
	}

	private FrameBuilder baseFrame() {
		return new FrameBuilder().add("groupId", artifact.groupId()).add("artifactId", artifact.name()).add("version", artifact.version());
	}

	private Frame[] resolutionFrameFrom(JsonObject object) {
		List<Frame> frames = new ArrayList<>();
		if (object.get("resolutions") == null) return null;
		final JsonObject dependencies = object.get("resolutions").getAsJsonObject();
		for (Map.Entry<String, JsonElement> entry : dependencies.entrySet())
			frames.add(new FrameBuilder().add("name", entry.getKey()).add("version", entry.getValue().toString().replaceAll("\"", "")).toFrame());
		return frames.toArray(new Frame[0]);
	}

	private Frame resolutionFrameFrom(Artifact.WebResolution resolution) {
		return new FrameBuilder().add("name", resolution.name()).add("version", resolution.version()).toFrame();
	}


	private Map<String, String> dependenciesFrom(JsonObject object) {
		Map<String, String> map = new HashMap<>();
		if (object.get("dependencies") == null) return map;
		final JsonObject dependencies = object.get("dependencies").getAsJsonObject();
		for (Map.Entry<String, JsonElement> entry : dependencies.entrySet())
			map.put(entry.getKey(), entry.getValue().getAsString());
		return map;
	}

	private void write(String content, File destiny) {
		try {
			Files.write(destiny.toPath(), content.getBytes()).toFile();
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
	}
}
