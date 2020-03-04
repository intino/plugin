package io.intino.plugin.project.builders;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.jcabi.aether.Aether;
import io.intino.plugin.project.IntinoDirectory;
import org.jetbrains.annotations.NotNull;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.repository.RemoteRepository;
import org.sonatype.aether.repository.RepositoryPolicy;
import org.sonatype.aether.resolution.DependencyResolutionException;
import org.sonatype.aether.util.artifact.DefaultArtifact;
import org.sonatype.aether.util.artifact.JavaScopes;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static io.intino.plugin.dependencyresolution.ArtifactoryConnector.MAVEN_URL;
import static org.eclipse.aether.repository.RepositoryPolicy.UPDATE_POLICY_DAILY;

public class InterfaceBuilderManager {
	private static final Logger LOG = Logger.getInstance(InterfaceBuilderManager.class);
	public static final String INTINO_RELEASES = "https://artifactory.intino.io/artifactory/releases";
	private static final File LOCAL_REPOSITORY = new File(System.getProperty("user.home") + File.separator + ".m2" + File.separator + "repository");
	public static String minimunVersion = "8.0.0";

	public String download(Module module, String version) {
		if (isDownloaded(version)) {
			LOG.info("Konos " + version + " is already downloaded");
			return version;
		}
		List<Artifact> artifacts = konosLibrary(version);
		final List<String> paths = librariesOf(artifacts);
		saveClassPath(module, paths);
		if (!artifacts.isEmpty()) return artifacts.get(0).getVersion();
		return version;
	}

	public static boolean exists(String version) {
		//TODO
		return true;
	}

	private boolean isDownloaded(String version) {
		//TODO
		return false;
	}

	public void purge(String version) {
		final List<Artifact> artifacts = konosLibrary(version);
		for (Artifact artifact : artifacts) {
			final File file = artifact.getFile();
			if (file != null && file.exists()) file.delete();
		}
	}

	private File pathOf(Artifact artifact) {
		return artifact.getFile();
	}

	private List<Artifact> konosLibrary(String version) {
		final Aether aether = new Aether(collectRemotes(), LOCAL_REPOSITORY);
		try {
			return aether.resolve(new DefaultArtifact("io.intino.konos", "builder", "jar", version), JavaScopes.COMPILE);
		} catch (DependencyResolutionException e) {
			return Collections.emptyList();
		}
	}

	@NotNull
	private Collection<RemoteRepository> collectRemotes() {
		Collection<RemoteRepository> remotes = new ArrayList<>();
		try {
			remotes.add(new RemoteRepository("local", "default", LOCAL_REPOSITORY.toURI().toURL().toString()).setPolicy(false, new RepositoryPolicy().setEnabled(true).setUpdatePolicy(UPDATE_POLICY_DAILY)));
		} catch (MalformedURLException ignored) {
		}
		remotes.add(new RemoteRepository("intino-maven", "default", INTINO_RELEASES).setPolicy(false, new RepositoryPolicy().setEnabled(true).setUpdatePolicy(UPDATE_POLICY_DAILY)));
		remotes.add(new RemoteRepository("maven-central", "default", MAVEN_URL).setPolicy(false, new RepositoryPolicy().setEnabled(true).setUpdatePolicy(UPDATE_POLICY_DAILY)));
		return remotes;
	}

	private List<String> librariesOf(List<Artifact> classpath) {
		return classpath.stream().map(c -> c.getFile().getAbsolutePath()).collect(Collectors.toList());
	}

	private void saveClassPath(Module module, List<String> paths) {
		if (paths.isEmpty()) return;
		final String home = System.getProperty("user.home");
		List<String> libraries = paths.stream().map(l -> l.replace(home, "$HOME")).collect(Collectors.toList());
		try {
			File moduleBoxDirectory = new File(IntinoDirectory.boxDirectory(module.getProject()), module.getName());
			moduleBoxDirectory.mkdirs();
			Files.write(classpathFile(moduleBoxDirectory), String.join(":", libraries).getBytes());
		} catch (IOException e) {
			LOG.error(e.getMessage());
		}
	}

	@NotNull
	public static Path classpathFile(File moduleBoxDirectory) {
		return new File(moduleBoxDirectory, "compiler.classpath").toPath();
	}
}
