package io.intino.plugin.dependencyresolution;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.util.Computable;
import com.jcabi.aether.Aether;
import io.intino.legio.Project.Dependencies;
import io.intino.legio.Project.Dependencies.Dependency;
import io.intino.legio.Project.Repositories;
import org.jetbrains.annotations.NotNull;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.repository.RemoteRepository;
import org.sonatype.aether.resolution.DependencyResolutionException;
import org.sonatype.aether.util.artifact.DefaultArtifact;
import org.sonatype.aether.util.artifact.JavaScopes;
import tara.compiler.shared.Configuration;
import tara.intellij.lang.psi.impl.TaraUtil;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

public class DependencyResolver {

	private final Module module;
	private final Repositories repositories;
	private final Dependencies dependencies;
	private final LibraryManager manager;
	private final Aether aether;

	public DependencyResolver(Module module, Repositories repositories, Dependencies dependencies) {
		this.manager = new LibraryManager(module);
		this.module = module;
		this.repositories = repositories;
		this.dependencies = dependencies;
		aether = new Aether(collectRemotes(), new File(System.getProperty("user.home") + File.separator + ".m2" + File.separator + "repository"));
	}

	public List<Library> resolve() {
		return ApplicationManager.getApplication().runWriteAction((Computable<List<Library>>) this::processDependencies);
	}

	private List<Library> processDependencies() {
		List<Library> newLibraries = new ArrayList<>();
		if (dependencies == null) return newLibraries;
		dependencies.dependencyList().forEach(d -> {
			Module moduleDependency = moduleOf(d);
			if (moduleDependency != null) {
				newLibraries.addAll(manager.resolveAsModuleDependency(moduleDependency));
				d.effectiveVersion(d.version());
				d.toModule(true);
			} else newLibraries.addAll(asLibrary(d));
		});
		return newLibraries;
	}

	private List<Library> asLibrary(Dependency d) {
		final List<Artifact> artifacts = collectArtifacts(d);
		final List<Library> resolved = manager.registerOrGetLibrary(artifacts);
		if (!artifacts.isEmpty()) d.effectiveVersion(artifacts.get(0).getVersion());
		else d.effectiveVersion("");
		manager.addToModule(resolved, d.is(Dependencies.Test.class));
		d.artifacts().clear();
		d.artifacts().addAll(artifacts.stream().map(a -> a.getGroupId() + ":" + a.getArtifactId() + ":" + a.getVersion()).collect(Collectors.toList()));
		d.resolved(true);
		return resolved;
	}

	private Module moduleOf(Dependency d) {
		final List<Module> modules = Arrays.stream(ModuleManager.getInstance(module.getProject()).getModules()).filter(m -> !m.equals(this.module)).collect(Collectors.toList());
		for (Module m : modules) {
			final Configuration configuration = TaraUtil.configurationOf(m);
			if (configuration == null) continue;
			if (d.groupId().equals(configuration.groupId().toLowerCase()) && d.artifactId().toLowerCase().equals(configuration.artifactId().toLowerCase()))
				return m;
		}
		return null;
	}

	private List<Artifact> collectArtifacts(Dependency dependency) {
		final String scope = dependency.is(Dependencies.Test.class) ? JavaScopes.TEST : JavaScopes.COMPILE;
		try {
			return aether.resolve(new DefaultArtifact(dependency.identifier()), scope);
		} catch (DependencyResolutionException e) {
			return tryAsPom(aether, dependency.identifier().split(":"), scope);
		}
	}

	private List<Artifact> tryAsPom(Aether aether, String[] dependency, String scope) {
		if (dependency.length != 3) return Collections.emptyList();
		try {
			return aether.resolve(new DefaultArtifact(dependency[0], dependency[1], "pom", dependency[2]), scope);
		} catch (DependencyResolutionException e) {
			return Collections.emptyList();
		}
	}

	@NotNull
	private Collection<RemoteRepository> collectRemotes() {
		Collection<RemoteRepository> remotes = new ArrayList<>();
		remotes.add(new RemoteRepository("maven-central", "default", "http://repo1.maven.org/maven2/"));
		remotes.addAll(repositories.repositoryList().stream().map(remote -> new RemoteRepository(remote.name(), "default", remote.url())).collect(Collectors.toList()));
		return remotes;
	}
}
