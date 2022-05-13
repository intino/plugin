package io.intino.plugin.project.configuration.model;

import io.intino.Configuration;
import io.intino.magritte.lang.model.Node;
import io.intino.plugin.lang.psi.TaraNode;
import io.intino.plugin.project.configuration.LegioConfiguration;
import io.intino.plugin.settings.ArtifactoryCredential;
import io.intino.plugin.settings.IntinoSettings;
import org.sonatype.aether.repository.Authentication;

import static io.intino.plugin.lang.psi.impl.TaraPsiUtil.parameterValue;

public abstract class LegioRepository implements Configuration.Repository {
	private final LegioConfiguration configuration;
	private final Node node;
	private final IntinoSettings settings;

	public LegioRepository(LegioConfiguration configuration, Node node) {
		this.configuration = configuration;
		this.node = node;
		this.settings = IntinoSettings.getSafeInstance(configuration.module().getProject());

	}

	@Override
	public String identifier() {
		return parameterValue(node.container(), "identifier", 0);
	}

	@Override
	public String url() {
		return parameterValue(node, "url", 0);
	}

	@Override
	public Configuration root() {
		return configuration;
	}

	@Override
	public Configuration.ConfigurationNode owner() {
		return null;
	}

	public Node getNode() {
		return node;
	}

	@Override
	public String user() {
		final ArtifactoryCredential repository = repository();
		return repository == null ? null : repository.username;
	}

	@Override
	public String password() {
		final ArtifactoryCredential repository = repository();
		return repository == null ? null : repository.password;
	}

	private ArtifactoryCredential repository() {
		final String identifier = identifier();
		return settings.artifactories().stream().filter(credential -> credential.serverId.equals(identifier)).findFirst().orElse(null);
	}

	public static class LegioReleaseRepository extends LegioRepository implements Configuration.Repository.Release {
		public LegioReleaseRepository(LegioConfiguration configuration, TaraNode node) {
			super(configuration, node);
		}
	}

	public static class LegioSnapshotRepository extends LegioRepository implements Configuration.Repository.Snapshot {
		public LegioSnapshotRepository(LegioConfiguration configuration, TaraNode node) {
			super(configuration, node);
		}
	}

}
