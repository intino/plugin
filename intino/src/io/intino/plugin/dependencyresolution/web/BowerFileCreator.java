package io.intino.plugin.dependencyresolution.web;

import com.intellij.openapi.diagnostic.Logger;
import io.intino.legio.graph.Artifact;
import io.intino.legio.graph.Artifact.WebImports.Resolution;
import io.intino.legio.graph.Artifact.WebImports.WebComponent;
import io.intino.plugin.dependencyresolution.WebDependencyResolver;
import org.siani.itrules.model.Frame;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

public class BowerFileCreator {
	private static final Logger logger = Logger.getInstance(WebDependencyResolver.class.getName());
	private final Artifact artifact;
	private final List<WebComponent> webComponents;
	private final List<Resolution> resolutions;

	public BowerFileCreator(Artifact artifact) {
		this.artifact = artifact;
		this.webComponents = artifact.webImports().webComponentList();
		this.resolutions = artifact.webImports().resolutionList();
	}

	public File createBowerFile(File destination) {
		final Frame frame = fill(new Frame().addTypes("bower"));
		for (WebComponent webComponent : webComponents) frame.addSlot("dependency", dependencyFrameOf(webComponent));
		for (Resolution resolution : resolutions) frame.addSlot("resolution", resolutionFrameOf(resolution));
		return write(BowerTemplate.create().format(frame), new File(destination, "bower.json"));
	}

	private Frame dependencyFrameOf(WebComponent webComponent) {
		final Frame dependency = new Frame().addSlot("name", webComponent.name$()).addSlot("version", webComponent.version());
		if (webComponent.url() != null && !webComponent.url().isEmpty())
			dependency.addSlot("url", webComponent.url());
		return dependency;
	}

	private Frame resolutionFrameOf(Resolution resolution) {
		return new Frame().addSlot("name", resolution.name()).addSlot("version", resolution.version());
	}

	public File createBowerrcFile(File libComponentsDirectory, File destination) {
		return write("{\"directory\": \"" + libComponentsDirectory.getAbsolutePath().replace("\\", "/") + "\"}", new File(destination, ".bowerrc"));
	}

	private Frame fill(Frame frame) {
		return frame.addSlot("groupId", artifact.groupId()).addSlot("artifactId", artifact.name$()).addSlot("version", artifact.version());
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
