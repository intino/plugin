package io.intino.plugin.project.configuration.model;

import io.intino.Configuration;
import io.intino.magritte.lang.model.Aspect;
import io.intino.magritte.lang.model.Node;
import io.intino.plugin.lang.psi.TaraAspectApply;
import io.intino.plugin.lang.psi.TaraNode;
import io.intino.plugin.lang.psi.impl.TaraPsiUtil;

import java.util.List;
import java.util.stream.Collectors;

import static com.intellij.openapi.command.WriteCommandAction.writeCommandAction;
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
	public boolean createMavenPom() {
		String createPOMproject = read(() -> parameterValue(node, "createMavenPom", 1));
		return Boolean.parseBoolean(createPOMproject);
	}

	@Override
	public boolean attachSources() {
		String attachSources = read(() -> parameterValue(node, "attachSources", 2));
		return Boolean.parseBoolean(attachSources);
	}

	@Override
	public List<String> mavenPlugins() {
		List<Node> mavenPlugins = TaraPsiUtil.componentsOfType(node, "MavenPlugin");
		return mavenPlugins.stream().map(n -> read(() -> parameterValue(n, "code", 0).replace("=", ""))).collect(Collectors.toList());
	}

	@Override
	public boolean attachDoc() {
		String attachDoc = read(() -> parameterValue(node, "attachDoc", 3));
		return Boolean.parseBoolean(attachDoc);
	}

	@Override
	public boolean includeTests() {
		String includeTests = read(() -> parameterValue(node, "includeTests", 4));
		return Boolean.parseBoolean(includeTests);
	}

	@Override
	public boolean signArtifactWithGpg() {
		String signArtifactWitGpg = read(() -> parameterValue(node, "signArtifactWithGpg", 5));
		return Boolean.parseBoolean(signArtifactWitGpg);
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

	public void mainClass(String qualifiedName) {
		writeCommandAction(node.getProject(), node.getContainingFile()).run(() -> {
			if (this.node.appliedAspects().isEmpty()) this.node.applyAspect("Runnable");
		});
		writeCommandAction(node.getProject(), node.getContainingFile()).run(() -> {
			TaraAspectApply runnable = (TaraAspectApply) this.node.appliedAspects().get(0);
			runnable.addParameter("mainClass", 0, List.of(qualifiedName));
		});
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
