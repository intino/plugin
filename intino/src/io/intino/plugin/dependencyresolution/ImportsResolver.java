package io.intino.plugin.dependencyresolution;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.jcabi.aether.Aether;
import io.intino.Configuration;
import io.intino.Configuration.Artifact.Dependency;
import io.intino.Configuration.Artifact.Dependency.Web;
import io.intino.Configuration.Repository;
import io.intino.plugin.IntinoException;
import io.intino.plugin.dependencyresolution.DependencyCatalog.DependencyScope;
import io.intino.plugin.lang.psi.impl.IntinoUtil;
import io.intino.plugin.project.configuration.LegioConfiguration;
import io.intino.plugin.project.configuration.Version;
import io.intino.plugin.project.configuration.model.LegioDependency;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.repository.RemoteRepository;
import org.sonatype.aether.resolution.DependencyResolutionException;
import org.sonatype.aether.util.artifact.DefaultArtifact;
import org.sonatype.aether.util.artifact.JavaScopes;
import org.sonatype.aether.util.filter.ExclusionsDependencyFilter;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Collections.emptyMap;
import static java.util.stream.Collectors.toList;

public class ImportsResolver {
	private final Module module;
	private final List<Repository> repositories;
	private final Aether aether;
	private final DependencyAuditor auditor;
	private final String updatePolicy;
	private final ProgressIndicator indicator;
	private final ResolutionCache cache;
	private boolean mustReload;

	public ImportsResolver(@Nullable Module module, DependencyAuditor auditor, String updatePolicy, List<Repository> repositories, ProgressIndicator indicator) {
		this.module = module;
		this.repositories = repositories;
		this.auditor = auditor;
		this.updatePolicy = updatePolicy;
		this.indicator = indicator;
		this.aether = new Aether(collectRemotes(), localRepository());
		this.mustReload = false;
		this.cache = ResolutionCache.instance(module.getProject());

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
			catalog.merge(processModuleDependency(moduleDependency, Collections.emptyList(), DependencyScope.COMPILE.name()));
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
		DependencyCatalog catalog = new DependencyCatalog();
		for (Dependency d : dependencies) {
			if (indicator != null) indicator.setText("Resolving import " + d.identifier() + "...");
			if (auditor.isModified(((LegioDependency) d).node()) || mustReload) processDependency(catalog, d);
			else {
				List<DependencyCatalog.Dependency> deps = cache.get(cacheId(d));
				if (deps != null && !deps.isEmpty() && (moduleOf(d) != null || existFiles(deps))) {
					catalog.addAll(deps);
					d.resolved(true);
				} else {
					d.resolved(false);
					auditor.invalidate(((LegioDependency) d).node());
					mustReload = true;
				}
			}
		}
		cache.save();
		return catalog;
	}

	private void processDependency(DependencyCatalog catalog, Dependency d) {
		if (d instanceof Web) webImport(catalog, d);
		else javaImport(catalog, d);
	}

	private void javaImport(DependencyCatalog catalog, Dependency d) {
		DependencyCatalog newDeps = processLibraryDependency(d);
		if (newDeps.dependencies().isEmpty()) {
			Module moduleDependency = moduleOf(d);
			if (moduleDependency != null) newDeps = processModuleDependency(d, moduleDependency);
		}
		catalog.merge(newDeps);
		addToCache(d, newDeps);
	}

	private void webImport(DependencyCatalog catalog, Dependency d) {
		Module dependantModule = moduleOf(d);
		if (dependantModule != null) {
			DependencyCatalog newDeps = processModuleDependency(d, dependantModule);
			catalog.merge(newDeps);
			addToCache(d, newDeps);
		} else d.resolved(false);
	}

	private boolean existFiles(List<DependencyCatalog.Dependency> dependencies) {
		return dependencies.stream().allMatch(d -> d.jar != null && d.jar.exists());
	}

	private DependencyCatalog processModuleDependency(Dependency d, Module moduleDependency) {
		DependencyCatalog catalog = processModuleDependency(moduleDependency, d.excludes(), d.scope());
		d.effectiveVersion(d.version());
		d.toModule(true);
		d.resolved(true);
		return catalog;
	}

	@NotNull
	private DependencyCatalog processModuleDependency(Module moduleDependency, List<Dependency.Exclude> excludes, String scope) {
		DependencyCatalog catalog = new DependencyCatalog();
		DependencyCatalog moduleDependenciesCatalog = new ModuleDependencyResolver().resolveDependencyWith(moduleDependency, excludes, scope);
		catalog.merge(moduleDependenciesCatalog);
		return catalog;
	}

	private void addToCache(Dependency d, DependencyCatalog newDeps) {
		try {
			if (!new Version(d.version()).isSnapshot()) cache.put(cacheId(d), newDeps.dependencies());
		} catch (IntinoException e) {
		}
	}

	@NotNull
	private String cacheId(Dependency d) {
		String excludes = d.excludes().stream().map(e -> e.groupId() + ":" + e.artifactId()).collect(Collectors.joining("|"));
		return d.identifier() + (excludes.isEmpty() ? "" : "#" + excludes);
	}

	private DependencyCatalog processLibraryDependency(Dependency d) {
		final Map<Artifact, DependencyScope> artifacts = collectArtifacts(d);
		if (artifacts.isEmpty()) {
			d.effectiveVersion("");
			d.resolved(false);
			return new DependencyCatalog();
		}
		DependencyCatalog catalog = new DependencyCatalog();
		artifacts.forEach((a, s) -> catalog.add(new DependencyCatalog.Dependency(a.getGroupId() + ":" + a.getArtifactId() + classifier(a) + ":" + a.getBaseVersion() + ":" + s.name(), a.getFile(), false)));
		d.effectiveVersion(findArtifact(artifacts, d.groupId(), d.artifactId()).getBaseVersion());
		d.resolved(true);
		return catalog;
	}

	private String classifier(Artifact a) {
		return a.getClassifier().trim().isEmpty() ? "" : ":" + a.getClassifier();
	}

	private Artifact findArtifact(Map<Artifact, DependencyScope> artifacts, String groupId, String artifactId) {
		return artifacts.keySet().stream().filter(a -> a.getGroupId().equals(groupId) && a.getArtifactId().equals(artifactId)).findFirst().get();
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
		Repositories repositories = new Repositories(this.module);
		remotes.add(repositories.maven(updatePolicy));
		remotes.addAll(repositories.map(this.repositories));
		return remotes;
	}

	@NotNull
	private File localRepository() {
		return new File(System.getProperty("user.home") + File.separator + ".m2" + File.separator + "repository");
	}
}
