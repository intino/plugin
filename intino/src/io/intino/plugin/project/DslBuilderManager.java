package io.intino.plugin.project;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import io.intino.Configuration;
import io.intino.Configuration.Artifact.Dsl;
import io.intino.plugin.dependencyresolution.LanguageResolver;
import io.intino.plugin.dependencyresolution.MavenDependencyResolver;
import io.intino.plugin.dependencyresolution.Repositories;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.DependencyResolutionException;
import org.eclipse.aether.resolution.DependencyResult;
import org.eclipse.aether.util.artifact.JavaScopes;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static io.intino.plugin.dependencyresolution.MavenDependencyResolver.dependenciesFrom;
import static io.intino.plugin.dependencyresolution.Repositories.INTINO_RELEASES;
import static org.apache.maven.artifact.repository.ArtifactRepositoryPolicy.UPDATE_POLICY_DAILY;


public class DslBuilderManager {
	private static final Logger LOG = Logger.getInstance(LanguageResolver.class);
	private static final Map<String, Set<String>> dslVersions = new HashMap<>();
	private final Project project;
	private final Module module;
	private final List<Configuration.Repository> repositories;
	private final Dsl dsl;
	private final MavenDependencyResolver resolver;

	public DslBuilderManager(Module module, List<Configuration.Repository> repositories, Dsl dsl) {
		this.project = module.getProject();
		this.module = module;
		this.repositories = repositories;
		this.dsl = dsl;
		this.resolver = new MavenDependencyResolver(repos());
	}

	public void resolveBuilder() {
		try {
			Dsl.Builder builder = dsl.builder();
			final DependencyResult resolution = resolver.resolve(new DefaultArtifact(builder.groupId(), builder.artifactId(), "jar", builder.version()), JavaScopes.COMPILE);
			saveClassPath(librariesOf(dependenciesFrom(resolution, false)));
			updateCache(builder);
		} catch (DependencyResolutionException e) {
			Notifications.Bus.notify(new Notification("Intino", "Builder not found", e.getMessage(), NotificationType.ERROR), null);
		}
	}

	private void updateCache(Dsl.Builder builder) {
		String name = dsl.name();
		if (dslVersions.get(name) == null) dslVersions.put(dsl.name(), new HashSet<>());
		dslVersions.get(dsl.name()).add(builder.version());
	}

	public boolean exists(String version) {
		return dslVersions.getOrDefault(dsl.name(), Set.of()).contains(version);
	}

	public Path classpathFile() {
		return new File(IntinoDirectory.dslDirectory(project, dsl.name()), module.getName() + File.separator + "compiler.classpath").toPath();
	}

	private void saveClassPath(List<String> paths) {
		if (paths.isEmpty()) return;
		final String home = System.getProperty("user.home");
		List<String> libraries = paths.stream().map(l -> l.replace(home, "$HOME")).toList();
		try {
			File file = classpathFile().toFile();
			file.getParentFile().mkdirs();
			Files.write(file.toPath(), String.join(":", libraries).getBytes());
		} catch (IOException e) {
			LOG.error(e.getMessage());
		}
	}

	@NotNull
	private List<RemoteRepository> repos() {
		Repositories repositoryManager = new Repositories(module);
		List<RemoteRepository> repos = repositoryManager.map(repositories);
		repos.add(repositoryManager.maven(UPDATE_POLICY_DAILY));
		if (repos.stream().noneMatch(r -> r.getUrl().equals(INTINO_RELEASES)))
			repos.add(repositoryManager.intino(UPDATE_POLICY_DAILY));
		return repos;
	}

	private List<String> librariesOf(List<Dependency> classpath) {
		return classpath.stream().map(c -> c.getArtifact().getFile().getAbsolutePath()).toList();
	}
}
