package org.siani.legio.plugin.dependencyresolution;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.roots.libraries.Library;
import com.jcabi.aether.Aether;
import org.jetbrains.annotations.NotNull;
import org.siani.legio.Project.Dependencies;
import org.siani.legio.Project.Dependencies.Dependency;
import org.siani.legio.Project.Repositories;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.repository.RemoteRepository;
import org.sonatype.aether.resolution.DependencyResolutionException;
import org.sonatype.aether.util.artifact.DefaultArtifact;
import org.sonatype.aether.util.artifact.JavaScopes;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class DependencyResolver {

	private final Repositories repositories;
	private final Dependencies dependencies;
	private final LibraryManager manager;

	public DependencyResolver(Module module, Repositories repositories, Dependencies dependencies) {
		this.manager = new LibraryManager(module);
		this.repositories = repositories;
		this.dependencies = dependencies;
	}

	public List<Library> resolve() {
		List<Library> newLibraries = new ArrayList<>();
		ApplicationManager.getApplication().runWriteAction(() -> dependencies.dependencyList().forEach(d -> {
			final List<Library> resolved = manager.registerOrGetLibrary(collectArtifacts(d));
			manager.addToModule(resolved, d.is(Dependencies.Test.class));
			newLibraries.addAll(resolved);
		}));
		return newLibraries;
	}

	private List<Artifact> collectArtifacts(Dependency dependency) {
		try {
			File local = new File(System.getProperty("user.home") + File.separator + ".m2" + File.separator + "repository");
			return new Aether(collectRemotes(), local).resolve(new DefaultArtifact(dependency.identifier()), dependency.is(Dependencies.Test.class) ? JavaScopes.TEST : JavaScopes.COMPILE);
		} catch (DependencyResolutionException ignored) {
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
