package io.intino.plugin.project.builders;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import io.intino.Configuration;
import io.intino.Configuration.Artifact.Model;
import io.intino.plugin.dependencyresolution.LanguageResolver;
import io.intino.plugin.dependencyresolution.MavenDependencyResolver;
import io.intino.plugin.dependencyresolution.Repositories;
import io.intino.plugin.project.IntinoDirectory;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static io.intino.plugin.dependencyresolution.MavenDependencyResolver.dependenciesFrom;
import static io.intino.plugin.dependencyresolution.Repositories.INTINO_RELEASES;
import static org.apache.maven.artifact.repository.ArtifactRepositoryPolicy.UPDATE_POLICY_DAILY;


public class ModelBuilderManager {
	private static final Logger LOG = Logger.getInstance(LanguageResolver.class);
	private static final Set<String> loadedVersions = new HashSet<>();

	private final Project project;
	private final Module module;
	private final List<Configuration.Repository> repositories;
	private final Model model;
	private final MavenDependencyResolver resolver;

	public ModelBuilderManager(Module module, List<Configuration.Repository> repositories, Model model) {
		this.project = module.getProject();
		this.module = module;
		this.repositories = repositories;
		this.model = model;
		this.resolver = new MavenDependencyResolver(repos());
	}

	public void resolveBuilder() {
		try {
			final DependencyResult resolution = resolver.resolve(new DefaultArtifact(model.sdk() + ":" + model.sdkVersion()), JavaScopes.COMPILE);
			loadedVersions.add(model.sdkVersion());
			saveClassPath(librariesOf(dependenciesFrom(resolution, false)));
		} catch (DependencyResolutionException e) {
			Notifications.Bus.notify(new Notification("Intino", "Dependency not found", e.getMessage(), NotificationType.ERROR), null);
		}
	}

	public static boolean exists(String version) {
		return loadedVersions.contains(version);
	}

	private void saveClassPath(List<String> paths) {
		if (paths.isEmpty()) return;
		final String home = System.getProperty("user.home");
		List<String> libraries = paths.stream().map(l -> l.replace(home, "$HOME")).toList();
		try {
			File file = new File(IntinoDirectory.modelDirectory(project), module.getName() + File.separator + "compiler.classpath");
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
