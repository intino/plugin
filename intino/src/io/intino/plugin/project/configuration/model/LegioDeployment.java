package io.intino.plugin.project.configuration.model;

import io.intino.plugin.lang.psi.TaraNode;
import io.intino.plugin.lang.psi.impl.TaraPsiUtil;
import io.intino.tara.compiler.shared.Configuration;
import io.intino.tara.lang.model.Node;
import io.intino.tara.lang.model.Primitive;

import java.util.List;

import static io.intino.plugin.lang.psi.impl.TaraPsiUtil.parameterValue;
import static io.intino.plugin.lang.psi.impl.TaraPsiUtil.referenceParameterValue;

public class LegioDeployment implements Configuration.Deployment {
	private final LegioArtifact artifact;
	private final TaraNode node;

	public LegioDeployment(LegioArtifact artifact, TaraNode node) {
		this.artifact = artifact;
		this.node = node;
	}

	@Override
	public Configuration.Server server() {
		Primitive.Reference release = referenceParameterValue(node, "server", 0);
		if (release == null) return null;
		return new LegioServer(artifact.root(), (TaraNode) release.reference());
	}

	@Override
	public Configuration.RunConfiguration runConfiguration() {
		Primitive.Reference release = referenceParameterValue(node, "runConfiguration", 0);
		if (release == null) return null;
		return new LegioRunConfiguration(artifact, release.reference());
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
				String minHdd = parameterValue(node, "minHdd", 0);
				return minHdd == null ? 0 : Integer.parseInt(minHdd);
			}

			@Override
			public int minMemory() {
				String minMemory = parameterValue(node, "minMemory", 0);
				return minMemory == null ? 0 : Integer.parseInt(minMemory);
			}

			@Override
			public String jvmVersion() {
				return parameterValue(node, "jvmVersion", 0);
			}
		};
	}
}