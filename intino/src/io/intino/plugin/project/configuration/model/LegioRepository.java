package io.intino.plugin.project.configuration.model;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Computable;
import io.intino.Configuration;
import io.intino.plugin.lang.psi.TaraMogram;
import io.intino.plugin.project.configuration.ArtifactLegioConfiguration;
import io.intino.plugin.project.configuration.ProjectLegioConfiguration;
import io.intino.plugin.settings.ArtifactoryCredential;
import io.intino.plugin.settings.IntinoSettings;
import io.intino.tara.language.model.Mogram;

import static io.intino.plugin.lang.psi.impl.TaraPsiUtil.parameterValue;

public abstract class LegioRepository implements Configuration.Repository {
	private final Configuration configuration;
	private final Mogram node;
	private final IntinoSettings settings;

	public LegioRepository(Configuration configuration, Mogram node) {
		this.configuration = configuration;
		this.node = node;
		this.settings = IntinoSettings.getInstance(project(configuration));
	}

	public LegioRepository(ProjectLegioConfiguration configuration, Mogram node) {
		this.configuration = null;
		this.node = node;
		this.settings = IntinoSettings.getInstance((configuration).ijProject());
	}

	@Override
	public String identifier() {
		return ApplicationManager.getApplication().runReadAction((Computable<String>) () -> parameterValue(node.container(), "identifier", 0));
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

	public Mogram getNode() {
		return node;
	}

	@Override
	public UpdatePolicy updatePolicy() {
		String value = parameterValue(node, "updatePolicy", 1);
		try {
			return value == null ? defaultUpdatePolicy() : UpdatePolicy.valueOf(value);
		} catch (IllegalArgumentException e) {
			return defaultUpdatePolicy();
		}
	}

	protected abstract UpdatePolicy defaultUpdatePolicy();

	private static Project project(Configuration configuration) {
		return configuration instanceof ArtifactLegioConfiguration c1 ?
				c1.module().getProject() :
				((ProjectLegioConfiguration) configuration).ijProject();
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
		public LegioReleaseRepository(ArtifactLegioConfiguration configuration, TaraMogram mogram) {
			super(configuration, mogram);
		}

		public LegioReleaseRepository(ProjectLegioConfiguration configuration, TaraMogram mogram) {
			super(configuration, mogram);
		}

		@Override
		protected UpdatePolicy defaultUpdatePolicy() {
			return UpdatePolicy.Daily;
		}
	}

	public static class LegioSnapshotRepository extends LegioRepository implements Configuration.Repository.Snapshot {
		public LegioSnapshotRepository(ArtifactLegioConfiguration configuration, TaraMogram mogram) {
			super(configuration, mogram);
		}

		public LegioSnapshotRepository(ProjectLegioConfiguration root, TaraMogram mogram) {
			super(root, mogram);
		}

		@Override
		protected UpdatePolicy defaultUpdatePolicy() {
			return UpdatePolicy.Always;
		}
	}

}
