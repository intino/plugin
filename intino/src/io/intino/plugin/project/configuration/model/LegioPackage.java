package io.intino.plugin.project.configuration.model;

import io.intino.Configuration;
import io.intino.magritte.lang.model.Aspect;
import io.intino.plugin.lang.psi.TaraNode;

import java.util.List;

import static io.intino.plugin.lang.psi.impl.TaraPsiUtil.parameterValue;
import static io.intino.plugin.lang.psi.impl.TaraPsiUtil.read;

public class LegioPackage implements Configuration.Artifact.Package {
	private final LegioArtifact artifact;
	private final TaraNode node;

	public LegioPackage(LegioArtifact artifact, TaraNode node) {
		this.artifact = artifact;
		this.node = node;
	}

	@Override
	public Mode mode() {
		String mode = read(() -> parameterValue(node, "mode", 0));
		return mode == null ? Mode.ModulesAndLibrariesLinkedByManifest : Mode.valueOf(mode);
	}

	@Override
	public boolean isRunnable() {
		return mainClass() != null;
	}

	@Override
	public boolean createPOMproject() {
		String createPOMproject = read(() -> parameterValue(node, "createPOMproject", 1));
		return createPOMproject != null && Boolean.parseBoolean(createPOMproject);
	}

	@Override
	public boolean attachSources() {
		String attachSources = read(() -> parameterValue(node, "attachSources", 2));
		return attachSources != null && Boolean.parseBoolean(attachSources);
	}

	@Override
	public boolean attachDoc() {
		String attachDoc = read(() -> parameterValue(node, "attachDoc", 3));
		return attachDoc != null && Boolean.parseBoolean(attachDoc);
	}

	@Override
	public boolean includeTests() {
		String includeTests = read(() -> parameterValue(node, "includeTests", 4));
		return includeTests != null && Boolean.parseBoolean(includeTests);
	}

	@Override
	public String classpathPrefix() {
		return read(() -> parameterValue(node, "classpathPrefix", 5));
	}

	@Override
	public String finalName() {
		return read(() -> parameterValue(node, "finalName", 7));
	}

	@Override
	public String defaultJVMOptions() {
		return read(() -> parameterValue(node, "defaultJVMOptions", 8));
	}

	@Override
	public String mainClass() {
		if (node == null) return null;
		List<Aspect> aspects = node.appliedAspects();
		if (aspects == null || aspects.isEmpty()) return null;
		Aspect runnable = aspects.stream().filter(a -> a.type().contains("Runnable")).findFirst().orElse(null);
		String mainClass = parameterValue(runnable, "mainClass", 0);
		return mainClass == null ? read(() -> parameterValue(node, "mainClass")) : mainClass;
	}

	@Override
	public MacOs macOsConfiguration() {
		//TODO
		return null;
	}

	@Override
	public Windows windowsConfiguration() {//TODO
		return null;
	}
}
