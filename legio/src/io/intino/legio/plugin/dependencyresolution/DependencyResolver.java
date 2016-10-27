package io.intino.legio.plugin.dependencyresolution;

import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.roots.libraries.Library;
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

	public DependencyResolver(Module module, Repositories repositories, Dependencies dependencies) {
		this.manager = new LibraryManager(module);
		this.module = module;
		this.repositories = repositories;
		this.dependencies = dependencies;
	}

	public List<Library> resolve() {
		List<Library> newLibraries = new ArrayList<>();
		Application application = ApplicationManager.getApplication();
		if (application.isWriteAccessAllowed())
			resolveInWriteAction(newLibraries, application);
		else application.invokeLater(() -> resolveInWriteAction(newLibraries, application), ModalityState.NON_MODAL);
		return newLibraries;
	}

	private void resolveInWriteAction(List<Library> newLibraries, Application application) {
		application.runWriteAction(() -> processDependencies(newLibraries));
	}

	private void processDependencies(List<Library> newLibraries) {
		if (dependencies == null) return;
		dependencies.dependencyList().forEach(d -> {
			Module moduleDependency = moduleOf(d);
			if (moduleDependency != null)
				newLibraries.addAll(manager.resolveAsModuleDependency(moduleDependency));
			else {
				final List<Library> resolved = manager.registerOrGetLibrary(collectArtifacts(d));
				manager.addToModule(resolved, d.is(Dependencies.Test.class));
				newLibraries.addAll(resolved);
			}
		});
	}

	private Module moduleOf(Dependency d) {
		final List<Module> modules = Arrays.stream(ModuleManager.getInstance(module.getProject()).getModules()).filter(m -> !m.equals(this.module)).collect(Collectors.toList());
		for (Module m : modules) {
			final Configuration configuration = TaraUtil.configurationOf(m);
			if (configuration == null) continue;
			if (d.groupId().equals(configuration.groupId().toLowerCase()) && d.artifactId().equals(configuration.artifactId().toLowerCase()))
				return m;
		}
		return null;
	}

	private List<Artifact> collectArtifacts(Dependency dependency) {
		File localRepository = new File(System.getProperty("user.home") + File.separator + ".m2" + File.separator + "repository");
		final Aether aether = new Aether(collectRemotes(), localRepository);
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
