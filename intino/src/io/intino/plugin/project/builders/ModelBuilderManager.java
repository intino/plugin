package io.intino.plugin.project.builders;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.jcabi.aether.Aether;
import io.intino.Configuration;
import io.intino.Configuration.Artifact.Model;
import io.intino.plugin.dependencyresolution.LanguageResolver;
import io.intino.plugin.dependencyresolution.Repositories;
import io.intino.plugin.project.IntinoDirectory;
import org.jetbrains.annotations.NotNull;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.repository.RemoteRepository;
import org.sonatype.aether.resolution.DependencyResolutionException;
import org.sonatype.aether.util.artifact.DefaultArtifact;
import org.sonatype.aether.util.artifact.JavaScopes;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static io.intino.plugin.dependencyresolution.Repositories.INTINO_RELEASES;
import static io.intino.plugin.dependencyresolution.Repositories.LOCAL;
import static org.sonatype.aether.repository.RepositoryPolicy.UPDATE_POLICY_DAILY;


public class ModelBuilderManager {
	private static final Logger LOG = Logger.getInstance(LanguageResolver.class);
	private static Set<String> loadedVersions;

	private final Project project;
	@NotNull
	private final Module module;
	private final List<Configuration.Repository> repositories;
	private final Model model;

	public ModelBuilderManager(Module module, List<Configuration.Repository> repositories, Model model) {
		this.project = module.getProject();
		this.module = module;
		this.repositories = repositories;
		this.model = model;
		if (loadedVersions == null) loadedVersions = new HashSet<>();
	}

	public void resolveBuilder() {
		try {
			final List<Artifact> artifacts = artifacts();
			loadedVersions.add(model.sdkVersion());
			final List<String> paths = librariesOf(artifacts);
			saveClassPath(paths);
		} catch (DependencyResolutionException e) {
			Notifications.Bus.notify(new Notification("Intino", "Dependency not found", e.getMessage(), NotificationType.ERROR), null);
		}
	}

	private List<Artifact> artifacts() throws DependencyResolutionException {
		return new Aether(repos(), LOCAL).resolve(new DefaultArtifact(model.sdk() + ":" + model.sdkVersion()), JavaScopes.COMPILE);
	}

	public static boolean exists(String version) {
		return loadedVersions == null || loadedVersions.contains(version);
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

	private void saveClassPath(List<String> paths) {
		if (paths.isEmpty()) return;
		final String home = System.getProperty("user.home");
		List<String> libraries = paths.stream().map(l -> l.replace(home, "$HOME")).collect(Collectors.toList());
		try {
			File file = new File(IntinoDirectory.modelDirectory(project), module.getName() + File.separator + "compiler.classpath");
			file.getParentFile().mkdirs();
			Files.write(file.toPath(), String.join(":", libraries).getBytes());
		} catch (IOException e) {
			LOG.error(e.getMessage());
		}
	}

	private List<String> librariesOf(List<Artifact> classpath) {
		return classpath.stream().map(c -> c.getFile().getAbsolutePath()).collect(Collectors.toList());
	}
}
