package io.intino.plugin.dependencyresolution;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.jcabi.aether.Aether;
import io.intino.Configuration;
import io.intino.Configuration.Artifact.Dependency;
import io.intino.Configuration.Artifact.Dependency.Web;
import io.intino.Configuration.Repository;
import io.intino.plugin.dependencyresolution.DependencyCatalog.DependencyScope;
import io.intino.plugin.lang.psi.impl.IntinoUtil;
import io.intino.plugin.project.LegioConfiguration;
import io.intino.plugin.project.configuration.model.LegioDependency;
import io.intino.plugin.settings.ArtifactoryCredential;
import io.intino.plugin.settings.IntinoSettings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.repository.Authentication;
import org.sonatype.aether.repository.RemoteRepository;
import org.sonatype.aether.repository.RepositoryPolicy;
import org.sonatype.aether.resolution.DependencyResolutionException;
import org.sonatype.aether.util.artifact.DefaultArtifact;
import org.sonatype.aether.util.artifact.JavaScopes;
import org.sonatype.aether.util.filter.ExclusionsDependencyFilter;

import java.io.File;
import java.util.*;

import static io.intino.plugin.dependencyresolution.ArtifactoryConnector.MAVEN_URL;
import static java.util.Collections.emptyMap;
import static java.util.stream.Collectors.toList;

public class ImportsResolver {
	private final Module module;
	private final List<Repository> repositories;
	private final Aether aether;
	private final DependencyAuditor auditor;
	private final String updatePolicy;
	private boolean mustReload;

	public ImportsResolver(@Nullable Module module, DependencyAuditor auditor, String updatePolicy, List<Repository> repositories) {
		this.module = module;
		this.repositories = repositories;
		this.auditor = auditor;
		this.updatePolicy = updatePolicy;
		this.aether = new Aether(collectRemotes(), localRepository());
		this.mustReload = false;
	}

	public DependencyCatalog resolve(List<Dependency> dependencies) {
		if (module == null) return DependencyCatalog.EMPTY;
		DependencyCatalog dependencyCatalog = processDependencies(dependencies);
		if (mustReload) {
			auditor.reload();
			return processDependencies(dependencies);
		} else return dependencyCatalog;
	}

	public DependencyCatalog resolveWeb(List<Web> webs) {
		DependencyCatalog catalog = new DependencyCatalog();
		for (Web web : webs) {
			Module moduleDependency = moduleOf(web.identifier());
			if (moduleDependency == null) continue;
			catalog.merge(processModuleDependency(moduleDependency));
			web.resolved(true);
		}
		return catalog;
	}

	public Artifact sourcesOf(String groupId, String artifactId, String version) {
		try {
			return aether.resolve(new DefaultArtifact(groupId, artifactId, "sources", "jar", version), JavaScopes.COMPILE).get(0);
		} catch (DependencyResolutionException e) {
			return null;
		}
	}

	private DependencyCatalog processDependencies(List<Dependency> dependencies) {
		ResolutionCache cache = ResolutionCache.instance(module.getProject());
		DependencyCatalog catalog = new DependencyCatalog();
		for (Dependency d : dependencies) {
			if (auditor.isModified(((LegioDependency) d).node()) || mustReload) {
				Module moduleDependency = moduleOf(d);
				if (moduleDependency != null) {
					DependencyCatalog newDeps = processModuleDependency(d, moduleDependency);
					catalog.merge(newDeps);
					cache.put(d.identifier(), newDeps.dependencies());
				} else if (!(d instanceof Web)) {//TODO
					DependencyCatalog newDeps = processLibraryDependency(d);
					catalog.merge(newDeps);
					cache.put(d.identifier(), newDeps.dependencies());
				}
			} else {
				List<DependencyCatalog.Dependency> deps = cache.get(d.identifier());
				if (deps != null && !deps.isEmpty() && existFiles(deps)) {
					catalog.addAll(deps);
					d.resolved(true);
				} else {
					auditor.invalidate(((LegioDependency) d).node());
					mustReload = true;
				}
			}
		}
		cache.save();
		return catalog;
	}

	private boolean existFiles(List<DependencyCatalog.Dependency> dependencies) {
		return dependencies.stream().allMatch(d -> d.jar != null && d.jar.exists());
	}

	private DependencyCatalog processModuleDependency(Dependency d, Module moduleDependency) {
		DependencyCatalog catalog = processModuleDependency(moduleDependency);
		d.effectiveVersion(d.version());
		d.toModule(true);
		d.resolved(true);
		return catalog;
	}

	@NotNull
	private DependencyCatalog processModuleDependency(Module moduleDependency) {
		DependencyCatalog catalog = new DependencyCatalog();
		DependencyCatalog moduleDependenciesCatalog = new ModuleDependencyResolver().resolveDependencyTo(moduleDependency);
		catalog.merge(moduleDependenciesCatalog);
		return catalog;
	}

	private DependencyCatalog processLibraryDependency(Dependency d) {
		final Map<Artifact, DependencyScope> artifacts = collectArtifacts(d);
		if (artifacts.isEmpty()) {
			d.effectiveVersion("");
			return new DependencyCatalog();
		}
		DependencyCatalog catalog = new DependencyCatalog();
		artifacts.forEach((a, s) -> catalog.add(new DependencyCatalog.Dependency(a.getGroupId() + ":" + a.getArtifactId() + ":" + a.getVersion() + ":" + s.name(), a.getFile(), false)));
		d.effectiveVersion(artifacts.keySet().iterator().next().getVersion());
		d.resolved(true);
		return catalog;
	}

	private Module moduleOf(Dependency d) {
		return moduleOf(d.identifier());
	}

	private Module moduleOf(String identifier) {
		String[] names = identifier.split(":");
		for (Module m : Arrays.stream(ModuleManager.getInstance(module.getProject()).getModules()).filter(module -> !module.equals(this.module)).collect(toList())) {
			final Configuration configuration = IntinoUtil.configurationOf(m);
			if (!(configuration instanceof LegioConfiguration)) continue;
			Configuration.Artifact artifact = configuration.artifact();
			if (names[0].equals(artifact.groupId().toLowerCase()) && names[1].equals(artifact.name().toLowerCase()) && names[2].equalsIgnoreCase(artifact.version()))
				return m;
		}
		return null;
	}

	private Map<Artifact, DependencyScope> collectArtifacts(Dependency dependency) {
		final DependencyScope scope = scopeOf(dependency);
		try {
			final Map<Artifact, DependencyScope> artifacts = toMap(resolve(dependency, scope), scope);
			if (scope == DependencyScope.COMPILE) {
				final Map<Artifact, DependencyScope> m = toMap(resolve(dependency, DependencyScope.RUNTIME), DependencyScope.RUNTIME);
				for (Artifact artifact : m.keySet())
					if (!artifacts.containsKey(artifact)) artifacts.put(artifact, m.get(artifact));
			}
			return artifacts;
		} catch (DependencyResolutionException e) {
			e.printStackTrace();
			return tryAsPom(aether, dependency.identifier().split(":"), scope);
		}
	}

	private DependencyScope scopeOf(Dependency dependency) {
		return DependencyScope.valueOf(dependency.scope().toUpperCase());
	}

	private List<Artifact> resolve(Dependency dependency, DependencyScope scope) throws DependencyResolutionException {
		final DefaultArtifact artifact = new DefaultArtifact(dependency.groupId(), dependency.artifactId(), "jar", dependency.version());
		if (dependency.excludes().isEmpty()) return aether.resolve(artifact, scope.name().toLowerCase());
		return aether.resolve(artifact, scope.name().toLowerCase(), exclusionsOf(dependency));
	}

	@NotNull
	private ExclusionsDependencyFilter exclusionsOf(Dependency dependency) {
		return new ExclusionsDependencyFilter(dependency.excludes().stream().map(e -> e.groupId() + ":" + e.artifactId()).collect(toList()));
	}

	private Map<Artifact, DependencyScope> tryAsPom(Aether aether, String[] dependency, DependencyScope scope) {
		if (dependency.length != 3) return emptyMap();
		try {
			return toMap(aether.resolve(new DefaultArtifact(dependency[0], dependency[1], "pom", dependency[2]), scope.name().toLowerCase()), scope);
		} catch (DependencyResolutionException e) {
			return emptyMap();
		}
	}

	private Map<Artifact, DependencyScope> toMap(List<Artifact> artifacts, DependencyScope scope) {
		Map<Artifact, DependencyScope> map = new LinkedHashMap<>();
		artifacts.forEach(a -> map.put(a, scope));
		return map;
	}

	@NotNull
	private Collection<RemoteRepository> collectRemotes() {
		Collection<RemoteRepository> remotes = new ArrayList<>();
		remotes.add(new RemoteRepository("maven-central", "default", MAVEN_URL).setPolicy(false, new RepositoryPolicy().setEnabled(true).setUpdatePolicy(updatePolicy)));
		remotes.addAll(repositories.stream().filter(r -> r != null && !(r instanceof Repository.Language)).map(this::repository).collect(toList()));
		return remotes;
	}

	private RemoteRepository repository(Repository remote) {
		final RemoteRepository repository = new RemoteRepository(remote.identifier(), "default", remote.url()).setAuthentication(provideAuthentication(remote.identifier()));
		repository.setPolicy(false, new RepositoryPolicy().setEnabled(true).setUpdatePolicy(updatePolicy));
		return repository;
	}

	private Authentication provideAuthentication(String mavenId) {
		final IntinoSettings settings = IntinoSettings.getSafeInstance(module.getProject());
		for (ArtifactoryCredential credential : settings.artifactories())
			if (credential.serverId.equals(mavenId))
				return new Authentication(credential.username, credential.password);
		return null;
	}

	@NotNull
	private File localRepository() {
		return new File(System.getProperty("user.home") + File.separator + ".m2" + File.separator + "repository");
	}

}
