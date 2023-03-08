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
import static io.intino.plugin.lang.psi.impl.TaraPsiUtil.*;

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
		String attachSources = read(() -> parameterValue(node, "attachSources", 3));
		return Boolean.parseBoolean(attachSources);
	}

	@Override
	public List<String> mavenPlugins() {
		List<Node> mavenPlugins = TaraPsiUtil.componentsOfType(node, "MavenPlugin");
		return mavenPlugins.stream().map(n -> read(() -> parameterValue(n, "code", 0).replace("=", ""))).collect(Collectors.toList());
	}

	@Override
	public boolean attachDoc() {
		String attachDoc = read(() -> parameterValue(node, "attachDoc", 4));
		return Boolean.parseBoolean(attachDoc);
	}

	@Override
	public boolean includeTests() {
		String includeTests = read(() -> parameterValue(node, "includeTests", 5));
		return Boolean.parseBoolean(includeTests);
	}

	@Override
	public boolean signArtifactWithGpg() {
		String signArtifactWitGpg = read(() -> parameterValue(node, "signArtifactWithGpg", 6));
		return Boolean.parseBoolean(signArtifactWitGpg);
	}

	@Override
	public String classpathPrefix() {
		return read(() -> parameterValue(node, "classpathPrefix", 7));
	}

	@Override
	public String finalName() {
		return read(() -> parameterValue(node, "finalName", 8));
	}

	@Override
	public String defaultJVMOptions() {
		return read(() -> parameterValue(node, "defaultJVMOptions", 9));
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

	@Override
	public LinuxService linuxService() {
		if (node == null) return null;
		List<Aspect> aspects = node.appliedAspects();
		if (aspects == null || aspects.isEmpty()) return null;
		Aspect linuxService = aspects.stream().filter(a -> a.type().contains("LinuxService")).findFirst().orElse(null);
		if (linuxService == null) return null;
		return new LegioPackageAsLinuxService(linuxService, artifact);
	}

	public static class LegioPackageAsLinuxService implements LinuxService {
		private final Aspect node;
		private final Configuration.Artifact artifact;

		public LegioPackageAsLinuxService(Aspect node, Configuration.Artifact artifact) {
			this.node = node;
			this.artifact = artifact;
		}

		@Override
		public String user() {
			return read(() -> parameterValue(node, "user", 0));
		}

		@Override
		public Configuration.RunConfiguration runConfiguration() {
			Node runConfiguration = read(() -> referenceParameterValue(node.parameters(), "runConfiguration", 1));
			if (runConfiguration == null) return null;
			return new LegioRunConfiguration((LegioArtifact) artifact, runConfiguration);
		}

		@Override
		public boolean restartOnFailure() {
			return Boolean.parseBoolean(read(() -> parameterValue(node, "restartOnFailure", 2)));
		}

		@Override
		public int managementPort() {
			return Integer.parseInt(read(() -> parameterValue(node, "managementPort", 3)));
		}
	}
}
