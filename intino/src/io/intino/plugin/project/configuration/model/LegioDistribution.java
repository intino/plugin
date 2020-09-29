package io.intino.plugin.project.configuration.model;

import io.intino.Configuration;
import io.intino.magritte.lang.model.Node;
import io.intino.plugin.lang.psi.TaraNode;
import io.intino.plugin.lang.psi.impl.TaraPsiUtil;

import static io.intino.plugin.lang.psi.impl.TaraPsiUtil.componentOfType;
import static io.intino.plugin.lang.psi.impl.TaraPsiUtil.parameterValue;

public class LegioDistribution implements Configuration.Distribution {
	private final LegioArtifact artifact;
	private final TaraNode node;

	public LegioDistribution(LegioArtifact artifact, TaraNode node) {
		this.artifact = artifact;
		this.node = node;
	}

	@Override
	public Configuration.Repository release() {
		Node artifactory = TaraPsiUtil.componentOfType(node, "Artifactory");
		if (artifactory == null) return null;
		Node release = TaraPsiUtil.componentOfType(artifactory, "Release");
		if (release == null) return null;
		return new LegioRepository.LegioReleaseRepository(artifact.root(), (TaraNode) release);
	}

	@Override
	public Configuration.Repository snapshot() {
		Node artifactory = TaraPsiUtil.componentOfType(node, "Artifactory");
		if (artifactory == null) return null;
		Node snapshot = TaraPsiUtil.componentOfType(artifactory, "Snapshot");
		if (snapshot == null) return null;
		return new LegioRepository.LegioSnapshotRepository(artifact.root(), (TaraNode) snapshot);
	}

	@Override
	public BitBucketDistribution onBitbucket() {
		Node onBitbucket = componentOfType(node, "OnBitbucket");
		if (onBitbucket == null) return null;
		return new BitBucketDistribution() {

			@Override
			public String owner() {
				return parameterValue(onBitbucket, "owner");
			}

			@Override
			public String slugName() {
				return parameterValue(onBitbucket, "slugName");
			}
		};
	}


}
