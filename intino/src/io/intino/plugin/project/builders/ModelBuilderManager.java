package io.intino.plugin.project.builders;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.jcabi.aether.Aether;
import io.intino.plugin.dependencyresolution.ArtifactoryConnector;
import io.intino.plugin.dependencyresolution.LanguageResolver;
import io.intino.plugin.project.IntinoDirectory;
import io.intino.tara.compiler.shared.Configuration.Artifact.Model;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.repository.RemoteRepository;
import org.sonatype.aether.repository.RepositoryPolicy;
import org.sonatype.aether.resolution.DependencyResolutionException;
import org.sonatype.aether.util.artifact.DefaultArtifact;
import org.sonatype.aether.util.artifact.JavaScopes;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.eclipse.aether.repository.RepositoryPolicy.UPDATE_POLICY_DAILY;

public class ModelBuilderManager {
	private static final Logger LOG = Logger.getInstance(LanguageResolver.class);
	public static final String TARA_BUILDER_REPOSITORY = "https://artifactory.intino.io/artifactory/releases";

	private File localRepository = new File(System.getProperty("user.home") + File.separator + ".m2" + File.separator + "repository");
	private final Project project;
	private final Model model;

	public ModelBuilderManager(Project project, Model model) {
		this.project = project;
		this.model = model;
	}

	public List<String> resolveBuilder() {
		try {
			final List<Artifact> artifacts = artifacts();
			final List<String> paths = librariesOf(artifacts);
			saveClassPath(paths);
			return paths;
		} catch (DependencyResolutionException e) {
			Notifications.Bus.notify(new Notification("Tara Language", "Dependecies not found",
					e.getMessage(), NotificationType.ERROR), null);
			return Collections.emptyList();
		}
	}

	private List<Artifact> artifacts() throws DependencyResolutionException {
		final List<RemoteRepository> repos = Arrays.asList(
				new RemoteRepository("intino-maven", "default", TARA_BUILDER_REPOSITORY).setPolicy(false, new RepositoryPolicy().setEnabled(true).setUpdatePolicy(UPDATE_POLICY_DAILY)),
				new RemoteRepository("maven-central", "default", ArtifactoryConnector.MAVEN_URL).setPolicy(false, new RepositoryPolicy().setEnabled(true).setUpdatePolicy(UPDATE_POLICY_DAILY)));
		return new Aether(repos, localRepository).resolve(new DefaultArtifact("io.intino.tara:builder:" + model.sdk()), JavaScopes.COMPILE);
	}

	private void saveClassPath(List<String> paths) {
		if (paths.isEmpty()) return;
		final String home = System.getProperty("user.home");
		List<String> libraries = paths.stream().map(l -> l.replace(home, "$HOME")).collect(Collectors.toList());
		try {
			Files.write(new File(IntinoDirectory.of(project), "compiler.classpath").toPath(), String.join(":", libraries).getBytes());
		} catch (IOException e) {
			LOG.error(e.getMessage());
		}
	}

	private List<String> librariesOf(List<Artifact> classpath) {
		return classpath.stream().map(c -> c.getFile().getAbsolutePath()).collect(Collectors.toList());
	}
}
