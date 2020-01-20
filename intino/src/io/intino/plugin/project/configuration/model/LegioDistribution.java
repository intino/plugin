package io.intino.plugin.project.configuration.model;

import io.intino.plugin.lang.psi.TaraNode;
import io.intino.tara.compiler.shared.Configuration;
import io.intino.tara.lang.model.Node;
import io.intino.tara.lang.model.Primitive;

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
		Primitive.Reference release = referenceParameterValue(node, "release", 0);
		if (release == null) return null;
		return new LegioRepository.LegioReleaseRepository(artifact.root(), (TaraNode) release.reference());
	}

	@Override
	public Configuration.Repository language() {
		Primitive.Reference release = referenceParameterValue(node, "language", 1);
		if (release == null) return null;
		return new LegioRepository.LegioLanguageRepository(artifact.root(), (TaraNode) release.reference());
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
