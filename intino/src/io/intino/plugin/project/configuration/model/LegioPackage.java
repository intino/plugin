package io.intino.plugin.project.configuration.model;

import io.intino.plugin.lang.psi.TaraNode;
import io.intino.tara.compiler.shared.Configuration;
import io.intino.tara.lang.model.Aspect;

import java.util.List;

import static io.intino.plugin.lang.psi.impl.TaraPsiUtil.parameterValue;

public class LegioPackage implements Configuration.Artifact.Package {
	private final LegioArtifact artifact;
	private final TaraNode node;

	public LegioPackage(LegioArtifact artifact, TaraNode node) {
		this.artifact = artifact;
		this.node = node;
	}

	@Override
	public Mode mode() {
		String mode = parameterValue(node, "mode", 0);
		return mode == null ? Mode.ModulesAndLibrariesLinkedByManifest : Mode.valueOf(mode);
	}

	@Override
	public boolean isRunnable() {
		return mainClass() != null;
	}

	@Override
	public boolean createPOMproject() {
		String createPOMproject = parameterValue(node, "createPOMproject", 1);
		return createPOMproject != null && Boolean.parseBoolean(createPOMproject);
	}

	@Override
	public boolean attachSources() {
		String attachSources = parameterValue(node, "attachSources", 2);
		return attachSources != null && Boolean.parseBoolean(attachSources);
	}

	@Override
	public boolean attachDoc() {
		String attachDoc = parameterValue(node, "attachDoc", 3);
		return attachDoc != null && Boolean.parseBoolean(attachDoc);
	}

	@Override
	public boolean includeTests() {
		String includeTests = parameterValue(node, "includeTests", 4);
		return includeTests != null && Boolean.parseBoolean(includeTests);
	}

	@Override
	public String classpathPrefix() {
		return parameterValue(node, "classpathPrefix", 5);
	}

	@Override
	public String finalName() {
		return parameterValue(node, "finalName", 7);
	}

	@Override
	public String defaultJVMOptions() {
		return parameterValue(node, "defaultJVMOptions", 8);
	}

	@Override
	public String mainClass() {
		List<Aspect> aspects = node.appliedAspects();
		if (aspects == null || aspects.isEmpty()) return null;
		Aspect runnable = aspects.stream().filter(a -> a.type().contains("Runnable")).findFirst().orElse(null);
		String mainClass = parameterValue(runnable, "mainClass", 0);
		return mainClass == null ? parameterValue(node, "mainClass") : mainClass;
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
