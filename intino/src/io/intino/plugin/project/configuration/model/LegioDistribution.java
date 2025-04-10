package io.intino.plugin.project.configuration.model;

import io.intino.Configuration;
import io.intino.plugin.lang.psi.TaraMogram;
import io.intino.plugin.lang.psi.impl.TaraPsiUtil;
import io.intino.tara.language.model.Mogram;

import static io.intino.plugin.lang.psi.impl.TaraPsiUtil.componentOfType;
import static io.intino.plugin.lang.psi.impl.TaraPsiUtil.parameterValue;

public class LegioDistribution implements Configuration.Distribution {
	private final LegioArtifact artifact;
	private final TaraMogram node;

	public LegioDistribution(LegioArtifact artifact, TaraMogram node) {
		this.artifact = artifact;
		this.node = node;
	}

	@Override
	public ArtifactoryDistribution onArtifactory() {
		Mogram artifactory = componentOfType(node, "Artifactory");
		if (artifactory == null) return null;
		return new ArtifactoryDistribution() {
			@Override
			public Configuration.Repository release() {
				Mogram release = TaraPsiUtil.componentOfType(artifactory, "Release");
				if (release == null) return null;
				return new LegioRepository.LegioReleaseRepository(artifact.root(), (TaraMogram) release);
			}

			@Override
			public Configuration.Repository snapshot() {
				Mogram snapshot = TaraPsiUtil.componentOfType(artifactory, "Snapshot");
				if (snapshot == null) return null;
				return new LegioRepository.LegioSnapshotRepository(artifact.root(), (TaraMogram) snapshot);
			}
		};
	}

	@Override
	public SonatypeDistribution onSonatype() {
		Mogram sonatype = componentOfType(node, "Sonatype");
		if (sonatype == null) return null;
		return () -> parameterValue(sonatype, "identifier", 0);
	}

	@Override
	public BitBucketDistribution onBitbucket() {
		Mogram onBitbucket = componentOfType(node, "Bitbucket");
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

	@Override
	public boolean distributeLanguage() {
		String distributeLanguage = parameterValue(node, "distributeLanguage");
		return distributeLanguage == null || Boolean.parseBoolean(distributeLanguage);
	}
}
