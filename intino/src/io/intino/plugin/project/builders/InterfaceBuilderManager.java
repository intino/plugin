package io.intino.plugin.project.builders;

import com.intellij.openapi.project.Project;
import com.jcabi.aether.Aether;
import org.jetbrains.annotations.NotNull;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.repository.RemoteRepository;
import org.sonatype.aether.repository.RepositoryPolicy;
import org.sonatype.aether.resolution.DependencyResolutionException;
import org.sonatype.aether.util.artifact.DefaultArtifact;
import org.sonatype.aether.util.artifact.JavaScopes;

import java.io.File;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class InterfaceBuilderManager {

	private static final File LOCAL_REPOSITORY = new File(System.getProperty("user.home") + File.separator + ".m2" + File.separator + "repository");
	public static final String INTINO_RELEASES = "https://artifactory.intino.io/artifactory/releases";

	public void reload(Project project, String version) {
		if (InterfaceBuilderLoader.isLoaded(project, version)) return;
		List<Artifact> artifacts = konosLibrary(version);
		if (!artifacts.isEmpty())
			InterfaceBuilderLoader.load(project, artifacts.stream().map(this::pathOf).toArray(File[]::new), version);
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
			remotes.add(new RemoteRepository("local", "default", LOCAL_REPOSITORY.toURI().toURL().toString()).setPolicy(false, new RepositoryPolicy().setEnabled(true).setUpdatePolicy("always")));
		} catch (MalformedURLException ignored) {
		}
		remotes.add(new RemoteRepository("intino-maven", "default", INTINO_RELEASES).setPolicy(false, new RepositoryPolicy().setEnabled(true).setUpdatePolicy("always")));
		remotes.add(new RemoteRepository("maven-central", "default", "http://repo1.maven.org/maven2/").setPolicy(false, new RepositoryPolicy().setEnabled(true).setUpdatePolicy("always")));
		return remotes;
	}

}
