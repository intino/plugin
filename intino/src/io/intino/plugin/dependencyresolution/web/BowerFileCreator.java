package io.intino.plugin.dependencyresolution.web;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.intellij.openapi.diagnostic.Logger;
import io.intino.legio.graph.Artifact;
import io.intino.legio.graph.Artifact.WebImports.Resolution;
import io.intino.legio.graph.Artifact.WebImports.WebComponent;
import io.intino.plugin.dependencyresolution.WebDependencyResolver;
import org.siani.itrules.model.Frame;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

public class BowerFileCreator {
	private static final Logger logger = Logger.getInstance(WebDependencyResolver.class.getName());
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
		final Frame frame = fill(new Frame().addTypes("bower"));
		for (WebComponent webComponent : webComponents) frame.addSlot("dependency", dependencyFrameOf(webComponent));
		for (JsonObject bower : bowers) {
			final Frame[] frames = dependencyFrameOf(bower);
			if (frames != null) frame.addSlot("dependency", frames);
		}
		for (Resolution resolution : resolutions) frame.addSlot("resolution", resolutionFrameOf(resolution));
		for (JsonObject bower : bowers) {
			final Frame[] frames = resolutionFrameOf(bower);
			if (frames != null) frame.addSlot("resolution", frames);
		}
		final File bowerFile = write(BowerTemplate.create().format(frame), new File(destination, "bower.json"));
		final List<String> slots = asList(frame.slots());
		return !slots.contains("dependency") && !slots.contains("resolution") ? null : bowerFile;
	}

	private Frame dependencyFrameOf(WebComponent webComponent) {
		final Frame dependency = new Frame().addSlot("name", webComponent.name$()).addSlot("version", webComponent.version());
		if (webComponent.url() != null && !webComponent.url().isEmpty())
			dependency.addSlot("url", webComponent.url());
		return dependency;
	}

	private Frame[] dependencyFrameOf(JsonObject object) {
		List<Frame> frames = new ArrayList<>();
		if (object.get("dependencies") == null) return null;
		final JsonObject dependencies = object.get("dependencies").getAsJsonObject();
		for (Map.Entry<String, JsonElement> entry : dependencies.entrySet())
			frames.add(new Frame("artifact").addSlot("name", entry.getKey()).addSlot("url", entry.getValue().toString()));
		return frames.toArray(new Frame[0]);
	}

	private Frame resolutionFrameOf(Resolution resolution) {
		return new Frame().addSlot("name", resolution.name()).addSlot("version", resolution.version());
	}

	private Frame[] resolutionFrameOf(JsonObject object) {
		List<Frame> frames = new ArrayList<>();
		if (object.get("resolutions") == null) return null;
		final JsonObject dependencies = object.get("resolutions").getAsJsonObject();
		for (Map.Entry<String, JsonElement> entry : dependencies.entrySet())
			frames.add(new Frame().addSlot("name", entry.getKey()).addSlot("version", entry.getValue().toString().replaceAll("\"", "")));
		return frames.toArray(new Frame[0]);
	}

	public File createBowerrcFile(File libComponentsDirectory, File destination) {
		return write("{\"directory\": \"" + libComponentsDirectory.getAbsolutePath().replace("\\", "/") + "\"}", new File(destination, ".bowerrc"));
	}

	private Frame fill(Frame frame) {
		return frame.addSlot("groupId", artifact.groupId()).addSlot("artifactId", artifact.name$()).addSlot("version", artifact.version());
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
