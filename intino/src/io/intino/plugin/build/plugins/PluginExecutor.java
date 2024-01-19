package io.intino.plugin.build.plugins;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.progress.ProgressIndicator;
import io.intino.Configuration.Artifact.Plugin;
import io.intino.plugin.MessageProvider;
import io.intino.plugin.build.FactoryPhase;
import io.intino.plugin.dependencyresolution.MavenDependencyResolver;
import io.intino.plugin.dependencyresolution.Repositories;
import io.intino.plugin.project.configuration.ArtifactLegioConfiguration;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.repository.RepositoryPolicy;
import org.eclipse.aether.resolution.DependencyResolutionException;
import org.eclipse.aether.resolution.DependencyResult;
import org.eclipse.aether.util.artifact.JavaScopes;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static com.intellij.openapi.diagnostic.Logger.getInstance;
import static io.intino.plugin.dependencyresolution.MavenDependencyResolver.dependenciesFrom;

public class PluginExecutor {
	private static final Logger LOG = getInstance(PluginExecutor.class);

	private final Module module;
	private final FactoryPhase phase;
	private final ArtifactLegioConfiguration configuration;
	private final Plugin plugin;
	private final ProgressIndicator indicator;

	public PluginExecutor(Module module, FactoryPhase phase, ArtifactLegioConfiguration configuration, Plugin plugin, ProgressIndicator indicator) {
		this.module = module;
		this.phase = phase;
		this.configuration = configuration;
		this.plugin = plugin;
		this.indicator = indicator;
	}

	public void execute() {
		try {
			indicator.setText("Running package plugins");
			new PluginRunner(module, configuration, plugin, phase, pluginLibraries(), indicator).run();
		} catch (Throwable e) {
			LOG.error(e);
			Notifications.Bus.notify(new Notification("Intino", MessageProvider.message("error.occurred", "plugin execution"), e.getMessage(), NotificationType.ERROR), module.getProject());
		}
	}


	private List<Dependency> pluginLibraries() throws DependencyResolutionException {
		DependencyResult result = new MavenDependencyResolver(collectRemotes())
				.resolve(new DefaultArtifact(plugin.groupId(), plugin.artifactId(), "jar", plugin.version()), JavaScopes.COMPILE);
		return dependenciesFrom(result, false);
	}

	@NotNull
	private List<RemoteRepository> collectRemotes() {
		List<RemoteRepository> remotes = new ArrayList<>();
		Repositories repositories = new Repositories(this.module);
		remotes.add(repositories.maven(RepositoryPolicy.UPDATE_POLICY_ALWAYS));
		remotes.addAll(repositories.map(this.configuration.repositories()));
		return remotes;
	}
}
