package io.intino.plugin.dependencyresolution;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.jcabi.aether.Aether;
import io.intino.legio.level.LevelArtifact.Model;
import io.intino.tara.plugin.lang.LanguageManager;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.repository.RemoteRepository;
import org.sonatype.aether.resolution.DependencyResolutionException;
import org.sonatype.aether.util.artifact.DefaultArtifact;
import org.sonatype.aether.util.artifact.JavaScopes;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class TaraBuilderResolver {
	private static final Logger LOG = Logger.getInstance(LanguageResolver.class);
	public static final String TARA_BUILDER_REPOSITORY = "https://artifactory.intino.io/artifactory/releases";

	private File localRepository = new File(System.getProperty("user.home") + File.separator + ".m2" + File.separator + "repository");
	private final Project project;
	private final Model modeling;

	public TaraBuilderResolver(Project project, Model modeling) {
		this.project = project;
		this.modeling = modeling;
	}


	public List<String> resolveBuilder() {
		try {
			final List<RemoteRepository> repos = new ArrayList<>();
			repos.add(new RemoteRepository("intino-maven", "default", TARA_BUILDER_REPOSITORY));
			repos.add(new RemoteRepository("maven-central", "default", "http://repo1.maven.org/maven2/"));
			String sdk = modeling.sdk();
			final List<String> paths = librariesOf(new Aether(repos, localRepository).resolve(new DefaultArtifact("io.intino.tara:builder:" + sdk), JavaScopes.COMPILE));
			saveClassPath(paths);
			return paths;
		} catch (DependencyResolutionException e) {
			Notifications.Bus.notify(new Notification("Tara Language", "Dependecies not found",
					e.getMessage(), NotificationType.ERROR), null);

			return Collections.emptyList();
		}
	}

	private void saveClassPath(List<String> paths) {
		if (paths.isEmpty()) return;
		final String home = System.getProperty("user.home");
		List<String> libraries = paths.stream().map(l -> l.replace(home, "$HOME")).collect(Collectors.toList());
		final File miscDirectory = LanguageManager.getMiscDirectory(this.project);
		final File file = new File(miscDirectory, "compiler.classpath");
		try {
			Files.write(file.toPath(), String.join(":", libraries).getBytes());
		} catch (IOException e) {
			LOG.error(e.getMessage());
		}
	}

	private List<String> librariesOf(List<Artifact> classpath) {
		return classpath.stream().map(c -> c.getFile().getAbsolutePath()).collect(Collectors.toList());
	}
}