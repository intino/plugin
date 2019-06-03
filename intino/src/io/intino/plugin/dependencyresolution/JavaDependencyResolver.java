package io.intino.plugin.dependencyresolution;

import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.roots.DependencyScope;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.util.Computable;
import com.jcabi.aether.Aether;
import io.intino.legio.graph.Artifact.Imports.Dependency;
import io.intino.legio.graph.Repository;
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

public class JavaDependencyResolver {
	private final Module module;
	private final List<Repository.Type> repositories;
	private final LibraryManager moduleLibrariesManager;
	private final Aether aether;
	private final String updatePolicy;
	private List<Dependency> dependencies;
	private Map<Dependency, Map<Artifact, DependencyScope>> collectedArtifacts = new HashMap<>();


	public JavaDependencyResolver(@Nullable Module module, List<Repository.Type> repositories, String updatePolicy, List<Dependency> dependencies) {
		this.moduleLibrariesManager = new LibraryManager(module);
		this.module = module;
		this.repositories = repositories;
		this.updatePolicy = updatePolicy;
		this.dependencies = dependencies;
		this.aether = new Aether(collectRemotes(), localRepository());
	}

	@NotNull
	private File localRepository() {
		return new File(System.getProperty("user.home") + File.separator + ".m2" + File.separator + "repository");
	}

	public List<Library> resolve() {
		if (module == null) return Collections.emptyList();
		collectArtifacts();
		final Application application = ApplicationManager.getApplication();
		final Set<Library> libraries = new HashSet<>();
		application.invokeAndWait(() -> libraries.addAll(application.runWriteAction((Computable<List<Library>>) this::processDependencies)), ModalityState.defaultModalityState());
		return new ArrayList<>(libraries);
	}

	private void collectArtifacts() {
		final DependencyLogger logger = DependencyLogger.instance();
		for (Dependency dependency : dependencies)
			if (moduleOf(dependency) == null) {
				final Map<Artifact, DependencyScope> artifacts = collectArtifacts(dependency);
				logger.add(dependency.identifier(), identifiersOf(artifacts));
				collectedArtifacts.put(dependency, artifacts);
			}
	}

	private List<String> identifiersOf(Map<Artifact, DependencyScope> artifacts) {
		return artifacts.keySet().stream().map(a -> a.getGroupId() + ":" + a.getArtifactId() + ":" + a.getVersion() + ":" + artifacts.get(a).getDisplayName()).collect(toList());
	}

	private List<Library> processDependencies() {
		List<Library> newLibraries = new ArrayList<>();
		for (Dependency d : dependencies)
			if (isLibrary(d)) newLibraries.addAll(asLibrary(d));
			else {
				newLibraries.addAll(moduleLibrariesManager.resolveAsModuleDependency(moduleOf(d)));
				d.effectiveVersion(d.version());
				d.toModule(true);
			}
		return newLibraries;
	}

	private List<Library> asLibrary(Dependency d) {
		final Map<Artifact, DependencyScope> artifacts = collectedArtifacts.get(d);
		final Map<DependencyScope, List<Library>> resolved = moduleLibrariesManager.registerOrGetLibrary(artifacts, emptyMap());
		if (!artifacts.isEmpty()) d.effectiveVersion(artifacts.keySet().iterator().next().getVersion());
		else d.effectiveVersion("");
		for (DependencyScope scope : resolved.keySet())
			moduleLibrariesManager.addToModule(resolved.get(scope), scope);
		d.artifacts().clear();
		d.artifacts().addAll(artifacts.keySet().stream().map(a -> a.getGroupId() + ":" + a.getArtifactId() + ":" + a.getVersion()).collect(toList()));
		d.resolve(true);
		return resolved.values().stream().flatMap(Collection::stream).collect(toList());
	}

	public Artifact sourcesOf(Artifact artifact) {
		try {
			return aether.resolve(new DefaultArtifact(artifact.getGroupId(), artifact.getArtifactId(), "sources", "jar", artifact.getVersion()), JavaScopes.COMPILE).get(0);
		} catch (DependencyResolutionException e) {
			return null;
		}
	}

	public Artifact sourcesOf(String groupId, String artifactId, String version) {
		try {
			return aether.resolve(new DefaultArtifact(groupId, artifactId, "sources", "jar", version), JavaScopes.COMPILE).get(0);
		} catch (DependencyResolutionException e) {
			return null;
		}
	}


	private boolean isLibrary(Dependency d) {
		return collectedArtifacts.containsKey(d);
	}

	private Module moduleOf(Dependency d) {
		for (Module m : Arrays.stream(ModuleManager.getInstance(module.getProject()).getModules()).filter(module -> !module.equals(this.module)).collect(toList())) {
			final Configuration configuration = TaraUtil.configurationOf(m);
			if (configuration == null) continue;
			if (d.groupId().equals(configuration.groupId().toLowerCase()) && d.artifactId().toLowerCase().equals(configuration.artifactId().toLowerCase()) && d.version().equalsIgnoreCase(configuration.version()))
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

	private List<Artifact> resolve(Dependency dependency, String scope) throws DependencyResolutionException {
		final DefaultArtifact artifact = new DefaultArtifact(dependency.groupId(), dependency.artifactId(), "jar", dependency.version());
		if (dependency.excludeList().isEmpty()) return aether.resolve(artifact, scope);
		return aether.resolve(artifact, scope, exclusionsOf(dependency));
	}

	@NotNull
	private ExclusionsDependencyFilter exclusionsOf(Dependency dependency) {
		return new ExclusionsDependencyFilter(dependency.excludeList().stream().map(e -> e.groupId() + ":" + e.artifactId()).collect(toList()));
	}

	@NotNull
	private DependencyScope scope(String scope) {
		return DependencyScope.valueOf(scope.toUpperCase());
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
}
