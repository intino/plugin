package io.intino.plugin.project.builders;

import com.intellij.openapi.project.Project;
import com.jcabi.aether.Aether;
import org.jetbrains.annotations.NotNull;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.repository.RemoteRepository;
import org.sonatype.aether.resolution.DependencyResolutionException;
import org.sonatype.aether.util.artifact.DefaultArtifact;
import org.sonatype.aether.util.artifact.JavaScopes;

import java.io.File;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class InterfaceBuilderManager {

	private static final File LOCAL_REPOSITORY = new File(System.getProperty("user.home") + File.separator + ".m2" + File.separator + "repository");

	public void reload(Project project, String version) {
		if (InterfaceBuilderLoader.isLoaded(project, version)) return;
		List<Artifact> library = konosLibrary(version);
		if (library != null && !library.isEmpty())
			InterfaceBuilderLoader.load(project, library.stream().map(this::pathOf).toArray(File[]::new), version);
	}

	private File pathOf(Artifact artifact) {
		return artifact.getFile();
	}

	private List<Artifact> konosLibrary(String version) {
		final Aether aether = new Aether(collectRemotes(), LOCAL_REPOSITORY);
		try {
			return aether.resolve(new DefaultArtifact("io.intino.konos:builder:jar:" + version), JavaScopes.COMPILE);
		} catch (DependencyResolutionException e) {
			return null;
		}
	}

	@NotNull
	private Collection<RemoteRepository> collectRemotes() {
		Collection<RemoteRepository> remotes = new ArrayList<>();
		try {
			remotes.add(new RemoteRepository("local", "default", LOCAL_REPOSITORY.toURI().toURL().toString()));
		} catch (MalformedURLException ignored) {
		}
		remotes.add(new RemoteRepository("intino-maven", "default", "http://artifactory.intino.io/artifactory/releases"));
		remotes.add(new RemoteRepository("maven-central", "default", "http://repo1.maven.org/maven2/"));
		return remotes;
	}

}
