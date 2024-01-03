package io.intino.plugin.dependencyresolution;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.progress.ProgressIndicator;
import io.intino.Configuration;
import io.intino.Configuration.Artifact.Dependency;
import io.intino.Configuration.Artifact.Dependency.Web;
import io.intino.Configuration.Repository;
import io.intino.plugin.dependencyresolution.DependencyCatalog.DependencyScope;
import io.intino.plugin.lang.psi.impl.IntinoUtil;
import io.intino.plugin.project.configuration.ArtifactLegioConfiguration;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.ArtifactResult;
import org.eclipse.aether.resolution.DependencyResolutionException;
import org.eclipse.aether.resolution.DependencyResult;
import org.eclipse.aether.util.artifact.JavaScopes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static io.intino.plugin.dependencyresolution.MavenDependencyResolver.dependenciesFrom;

public class ImportsResolver {
	private final Module module;
	private final List<Repository> repositories;
	private final String updatePolicy;
	private final ProgressIndicator indicator;
	private final MavenDependencyResolver resolver;

	public ImportsResolver(@Nullable Module module, String updatePolicy, List<Repository> repositories, ProgressIndicator indicator) {
		this.module = module;
		this.repositories = repositories;
		this.updatePolicy = updatePolicy;
		this.indicator = indicator;
		this.resolver = new MavenDependencyResolver(collectRemotes());
	}

	public DependencyCatalog resolve(List<Dependency> dependencies) {
		if (module == null) return DependencyCatalog.EMPTY;
		DependencyCatalog catalog = new DependencyCatalog();
		if (indicator != null) indicator.setText("Resolving imports...");
		List<Dependency> javaImports = dependencies.stream().filter(d -> !(d instanceof Web)).toList();
		try {
			DependencyResult result = resolver.resolve(javaImports);
			catalog.addAll(dependenciesFrom(result, false));
		} catch (DependencyResolutionException e) {
			Notifications.Bus.notify(new Notification("Intino", "Dependency not found", e.getMessage(), NotificationType.ERROR), null);
			DependencyResult result = e.getResult();
			catalog.addAll(dependenciesFrom(result, false));
		}
		dependencies.stream()
				.filter(d -> (d instanceof Web))
				.map(this::moduleOf)
				.filter(Objects::nonNull)
				.forEach(m -> catalog.add(m, JavaScopes.COMPILE));
		return catalog;
	}

	public DependencyCatalog resolveWeb(List<Web> webs) {
		DependencyCatalog catalog = new DependencyCatalog();
		for (Web web : webs) {
			Module moduleDependency = moduleOf(web.identifier());
			if (moduleDependency == null) continue;
			catalog.merge(processModuleDependency(moduleDependency, Collections.emptyList(), DependencyScope.COMPILE.name()));
		}
		return catalog;
	}

	public ArtifactResult sourcesOf(String groupId, String artifactId, String version) {
		return resolver.resolveSources(new DefaultArtifact(groupId, artifactId, "sources", "jar", version));
	}

	@NotNull
	private DependencyCatalog processModuleDependency(Module moduleDependency, List<Dependency.Exclude> excludes, String scope) {
		DependencyCatalog catalog = new DependencyCatalog();
		DependencyCatalog moduleDependenciesCatalog = new ModuleDependencyResolver().resolveDependencyWith(moduleDependency, scope, excludes);
		return catalog.merge(moduleDependenciesCatalog);
	}

	private Module moduleOf(Dependency d) {
		return moduleOf(d.identifier());
	}

	private Module moduleOf(String identifier) {
		String[] names = identifier.split(":");
		for (Module m : Arrays.stream(ModuleManager.getInstance(module.getProject()).getModules()).filter(module -> !module.equals(this.module)).toList()) {
			final Configuration configuration = IntinoUtil.configurationOf(m);
			if (!(configuration instanceof ArtifactLegioConfiguration)) continue;
			Configuration.Artifact artifact = configuration.artifact();
			if (names[0].equals(artifact.groupId().toLowerCase()) && names[1].equals(artifact.name().toLowerCase()) && names[2].equalsIgnoreCase(artifact.version()))
				return m;
		}
		return null;
	}

	@NotNull
	private List<RemoteRepository> collectRemotes() {
		Repositories repositories = new Repositories(this.module);
		List<RemoteRepository> remotes = new ArrayList<>(repositories.map(this.repositories));
		remotes.add(repositories.maven(updatePolicy));
		return remotes;
	}

}
