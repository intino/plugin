package io.intino.plugin.project.configuration.model;

import io.intino.Configuration;
import io.intino.magritte.lang.model.Node;
import io.intino.magritte.lang.model.Primitive;
import io.intino.plugin.lang.psi.TaraNode;
import io.intino.plugin.lang.psi.impl.TaraPsiUtil;

import static io.intino.plugin.lang.psi.impl.TaraPsiUtil.*;

public class LegioDistribution implements Configuration.Distribution {
	private final LegioArtifact artifact;
	private final TaraNode node;

	public LegioDistribution(LegioArtifact artifact, TaraNode node) {
		this.artifact = artifact;
		this.node = node;
	}

	@Override
	public Configuration.Repository release() {
		Primitive.Reference release = TaraPsiUtil.read(() -> referenceParameterValue(node, "release", 0));
		if (release == null) return null;
		return new LegioRepository.LegioReleaseRepository(artifact.root(), (TaraNode) release.reference());
	}

	@Override
	public Configuration.Repository language() {
		Primitive.Reference release = TaraPsiUtil.read(() -> referenceParameterValue(node, "language", 1));
		if (release == null) return null;
		return new LegioRepository.LegioLanguageRepository(artifact.root(), (TaraNode) release.reference());
	}

	@Override
	public Configuration.Repository snapshot() {
		Primitive.Reference release = TaraPsiUtil.read(() -> referenceParameterValue(node, "snapshot", 2));
		if (release == null) return null;
		return new LegioRepository.LegioSnapshotRepository(artifact.root(), (TaraNode) release.reference());
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
