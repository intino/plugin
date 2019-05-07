package io.intino.plugin.dependencyresolution.web;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.intellij.openapi.diagnostic.Logger;
import io.intino.itrules.Frame;
import io.intino.itrules.FrameBuilder;
import io.intino.legio.graph.Artifact;
import io.intino.legio.graph.Artifact.WebImports.Resolution;
import io.intino.legio.graph.Artifact.WebImports.WebComponent;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

public class BowerFileCreator {
	private static final Logger logger = Logger.getInstance(BowerFileCreator.class);
	private final Artifact artifact;
	private final List<WebComponent> webComponents;
	private final List<Resolution> resolutions;
	private List<JsonObject> bowers;

	public BowerFileCreator(Artifact artifact) {
		this(artifact, emptyList());
	}

	public BowerFileCreator(Artifact artifact, List<File> artifactBowers) {
		this.artifact = artifact;
		this.webComponents = artifact.webImports() == null ? emptyList() : artifact.webImports().webComponentList();
		this.resolutions = artifact.webImports() == null ? emptyList() : artifact.webImports().resolutionList();
		this.bowers = artifactBowers.stream().filter(File::exists).map(this::readBower).collect(toList());
	}

	public File createBowerFile(File destination) {
		final FrameBuilder builder = fill(new FrameBuilder("bower"));
		webComponents.forEach(webComponent -> builder.add("dependency", dependencyFrameOf(webComponent)));
		for (JsonObject bower : bowers) {
			final Frame[] frames = dependencyFrameOf(bower);
			if (frames != null) builder.add("dependency", frames);
		}
		for (Resolution resolution : resolutions) builder.add("resolution", resolutionFrameOf(resolution));
		for (JsonObject bower : bowers) {
			final Frame[] frames = resolutionFrameOf(bower);
			if (frames != null) builder.add("resolution", frames);
		}
		final File bowerFile = write(new BowerTemplate().render(builder), new File(destination, "bower.json"));
		try {
			Files.copy(bowerFile.toPath(), new File(bowerFile.getParentFile(), ".bower.json").toPath(), StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
		return isEmpty(builder) ? null : bowerFile;
	}

	private boolean isEmpty(FrameBuilder builder) {
		return !builder.contains("dependency") && !builder.contains("resolution");
	}

	private Frame dependencyFrameOf(WebComponent webComponent) {
		final FrameBuilder frameBuilder = new FrameBuilder().add("name", webComponent.name$()).add("version", webComponent.version());
		if (webComponent.url() != null && !webComponent.url().isEmpty())
			frameBuilder.add("url", webComponent.url());
		return frameBuilder.toFrame();
	}

	private Frame[] dependencyFrameOf(JsonObject object) {
		List<Frame> frames = new ArrayList<>();
		if (object.get("dependencies") == null) return null;
		final JsonObject dependencies = object.get("dependencies").getAsJsonObject();
		for (Map.Entry<String, JsonElement> entry : dependencies.entrySet())
			frames.add(new FrameBuilder("artifact").add("name", entry.getKey()).add("url", entry.getValue().toString()).toFrame());
		return frames.toArray(new Frame[0]);
	}

	private Frame resolutionFrameOf(Resolution resolution) {
		return new FrameBuilder().add("name", resolution.name()).add("version", resolution.version()).toFrame();
	}

	private Frame[] resolutionFrameOf(JsonObject object) {
		List<Frame> frames = new ArrayList<>();
		if (object.get("resolutions") == null) return null;
		final JsonObject dependencies = object.get("resolutions").getAsJsonObject();
		for (Map.Entry<String, JsonElement> entry : dependencies.entrySet())
			frames.add(new FrameBuilder().add("name", entry.getKey()).add("version", entry.getValue().toString().replaceAll("\"", "")).toFrame());
		return frames.toArray(new Frame[0]);
	}

	public File createBowerrcFile(File libComponentsDirectory, File destination) {
		return write("{\"directory\": \"" + libComponentsDirectory.getAbsolutePath().replace("\\", "/") + "\"}", new File(destination, ".bowerrc"));
	}

	private FrameBuilder fill(FrameBuilder frame) {
		return frame.add("groupId", artifact.groupId()).add("artifactId", artifact.name$()).add("version", artifact.version());
	}

	private JsonObject readBower(File file) {
		try {
			return new JsonParser().parse(new String(Files.readAllBytes(file.toPath()))).getAsJsonObject();
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
			return null;
		}
	}

	private File write(String content, File destiny) {
		try {
			return Files.write(destiny.toPath(), content.getBytes()).toFile();
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
			return destiny;
		}
	}

}
