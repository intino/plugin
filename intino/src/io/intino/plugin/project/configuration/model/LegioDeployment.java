package io.intino.plugin.project.configuration.model;

import io.intino.Configuration;
import io.intino.magritte.lang.model.Node;
import io.intino.plugin.lang.psi.TaraNode;
import io.intino.plugin.lang.psi.impl.TaraPsiUtil;

import java.util.List;
import java.util.stream.Collectors;

import static io.intino.plugin.lang.psi.impl.TaraPsiUtil.*;

public class LegioDeployment implements Configuration.Deployment {
	private final LegioArtifact artifact;
	private final TaraNode node;

	public LegioDeployment(LegioArtifact artifact, TaraNode node) {
		this.artifact = artifact;
		this.node = node;
	}

	@Override
	public Configuration.Server server() {
		Node release = read(() -> referenceParameterValue(node, "server", 0));
		if (release == null) return null;
		return new LegioServer(artifact.root(), (TaraNode) release);
	}

	@Override
	public Configuration.RunConfiguration runConfiguration() {
		Node release = read(() -> referenceParameterValue(node, "runConfiguration", 1));
		if (release == null) return null;
		return new LegioRunConfiguration(artifact, release);
	}

	@Override
	public List<String> bugTrackingUsers() {
		return null;
	}

	@Override
	public Requirements requirements() {
		Node requirements = TaraPsiUtil.componentOfType(node, "Requirements");
		return requirements == null ? null : new Requirements() {
			@Override
			public int minHdd() {
				Node memoryNode = componentOfType(requirements, "HDD");
				if (memoryNode == null) return 0;
				String minHDD = parameterValue(memoryNode, "minHDD", 0);
				return minHDD == null ? 0 : Integer.parseInt(minHDD);

			}

			@Override
			public int minMemory() {
				Node memoryNode = componentOfType(requirements, "Memory");
				if (memoryNode == null) return 0;
				String minMemory = parameterValue(memoryNode, "min", 0);
				return minMemory == null ? 0 : Integer.parseInt(minMemory);
			}

			@Override
			public int maxMemory() {
				Node memoryNode = componentOfType(requirements, "Memory");
				if (memoryNode == null) return 0;
				String max = parameterValue(memoryNode, "max", 1);
				return max == null ? 0 : Integer.parseInt(max);
			}

			@Override
			public String jvmVersion() {
				Node jvm = componentOfType(node, "JVM");
				if (jvm == null) return "";
				String version = parameterValue(jvm, "version", 0);
				return version == null ? "" : version;
			}

			@Override
			public Sync sync() {
				Node sync = componentOfType(requirements, "SyncDirectories");
				if (sync == null) return null;
				return () -> read(() -> componentsOfType(sync, "To").stream().
						collect(Collectors.toMap(r -> parameterValue(r, "module", 0), r -> referenceParameterValue(r, "server", 1).name(), (a, b) -> b)));
			}
		};
	}
}
