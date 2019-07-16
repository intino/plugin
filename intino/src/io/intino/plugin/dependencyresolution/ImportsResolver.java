package io.intino.plugin.dependencyresolution;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.jcabi.aether.Aether;
import io.intino.legio.graph.Artifact.Imports.Dependency;
import io.intino.legio.graph.Repository;
import io.intino.plugin.dependencyresolution.DependencyCatalog.DependencyScope;
import io.intino.plugin.settings.ArtifactoryCredential;
import io.intino.plugin.settings.IntinoSettings;
import io.intino.tara.compiler.shared.Configuration;
import io.intino.tara.plugin.lang.psi.impl.TaraUtil;
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

import static java.util.Collections.emptyMap;
import static java.util.stream.Collectors.toList;

public class ImportsResolver {
	private final Module module;
	private final List<Repository.Type> repositories;
	private final Aether aether;
	private final DependencyAuditor auditor;
	private final String updatePolicy;
	private boolean mustReload;

	public ImportsResolver(@Nullable Module module, DependencyAuditor auditor, String updatePolicy, List<Repository.Type> repositories) {
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

	public DependencyCatalog resolveWeb(List<io.intino.legio.graph.Artifact.Imports.Web> webs) {
		DependencyCatalog catalog = new DependencyCatalog();
		for (io.intino.legio.graph.Artifact.Imports.Web web : webs) {
			Module moduleDependency = moduleOf(web.identifier());
			if (moduleDependency == null) continue;
			catalog.merge(processModuleDependency(web.identifier(), moduleDependency));
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
			if (auditor.isModified(d.core$())) {
				Module moduleDependency = moduleOf(d);
				if (moduleDependency != null) {
					DependencyCatalog newDeps = processModuleDependency(d, moduleDependency);
					catalog.merge(newDeps);
					cache.put(d.identifier(), newDeps.dependencies());
				} else {
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
					auditor.invalidate(d.core$().id());
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
		DependencyCatalog catalog = processModuleDependency(d.identifier(), moduleDependency);
		d.effectiveVersion(d.version());
		d.toModule(true);
		d.resolved(true);
		return catalog;
	}

	@NotNull
	private DependencyCatalog processModuleDependency(String identifier, Module moduleDependency) {
		DependencyCatalog catalog = new DependencyCatalog();
		DependencyCatalog moduleDependenciesCatalog = new ModuleDependencyResolver().resolveDependencyTo(moduleDependency);
		catalog.add(new DependencyCatalog.Dependency(identifier + ":" + "COMPILE", moduleDependency.getName()));
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
			final Configuration configuration = TaraUtil.configurationOf(m);
			if (configuration == null) continue;
			if (names[0].equals(configuration.groupId().toLowerCase()) && names[1].equals(configuration.artifactId().toLowerCase()) && names[2].equalsIgnoreCase(configuration.version()))
				return m;
		}
		return null;
	}

	private Map<Artifact, DependencyScope> collectArtifacts(Dependency dependency) {
		final String scope = dependency.getClass().getSimpleName().toLowerCase();
		try {
			final Map<Artifact, DependencyScope> artifacts = toMap(resolve(dependency, scope), scope(scope));
			if (scope.equalsIgnoreCase(JavaScopes.COMPILE)) {
				final Map<Artifact, DependencyScope> m = toMap(resolve(dependency, JavaScopes.RUNTIME), DependencyScope.RUNTIME);
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
		return scope(dependency.getClass().getSimpleName());
	}

	@NotNull
	private DependencyScope scope(String scope) {
		return DependencyScope.valueOf(scope.toUpperCase());
	}

	private List<Artifact> resolve(Dependency dependency, String scope) throws DependencyResolutionException {
		final DefaultArtifact artifact = new DefaultArtifact(dependency.groupId(), dependency.artifactId(), "jar", dependency.version());
		if (dependency.excludeList().isEmpty()) return aether.resolve(artifact, scope);
		return aether.resolve(artifact, scope, exclusionsOf(dependency));
	}

	@NotNull
	private ExclusionsDependencyFilter exclusionsOf(Dependency dependency) {
		return new ExclusionsDependencyFilter(dependency.excludeList().stream().map(e -> e.groupId() + ":" + e.artifactId()).collect(toList()));
	}

	private Map<Artifact, DependencyScope> tryAsPom(Aether aether, String[] dependency, String scope) {
		if (dependency.length != 3) return emptyMap();
		try {
			return toMap(aether.resolve(new DefaultArtifact(dependency[0], dependency[1], "pom", dependency[2]), scope), scope(scope));
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
		remotes.add(new RemoteRepository("maven-central", "default", "http://repo1.maven.org/maven2/").setPolicy(false, new RepositoryPolicy().setEnabled(true).setUpdatePolicy(updatePolicy)));
		remotes.addAll(repositories.stream().filter(r -> r != null && !r.i$(Repository.Language.class)).map(this::repository).collect(toList()));
		return remotes;
	}

	private RemoteRepository repository(Repository.Type remote) {
		final RemoteRepository repository = new RemoteRepository(remote.mavenID(), "default", remote.url()).setAuthentication(provideAuthentication(remote.mavenID()));
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
