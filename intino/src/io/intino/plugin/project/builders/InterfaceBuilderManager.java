package io.intino.plugin.project.builders;

import com.intellij.notification.NotificationGroup;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.ui.MessageType;
import com.jcabi.aether.Aether;
import io.intino.plugin.project.LegioConfiguration;
import org.jetbrains.annotations.NotNull;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.repository.RemoteRepository;
import org.sonatype.aether.resolution.DependencyResolutionException;
import org.sonatype.aether.util.artifact.DefaultArtifact;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class InterfaceBuilderManager {

	private static final Logger LOG = Logger.getInstance(InterfaceBuilderManager.class.getName());

	private static final String PANDORA = "pandora";
	private LegioConfiguration configuration;

	public InterfaceBuilderManager(LegioConfiguration configuration) {
		this.configuration = configuration;
	}

	public void reload(String version) {
		List<Artifact> library = pandoraLibrary(version);
		if (library == null || library.isEmpty()) {
			notifyError("Interface Builder cannot be loaded. None libraries are found");
			return;
		}
		BuilderLoader.load(PANDORA, library.stream().map(this::pathOf).toArray(File[]::new));
	}


	private void notifyError(String message) {
		final NotificationGroup balloon = NotificationGroup.toolWindowGroup("Tara Language", "Balloon");
		balloon.createNotification(message, MessageType.ERROR).setImportant(false).notify(null);
	}

	private File pathOf(Artifact artifact) {
		return artifact.getFile();
	}

	private List<Artifact> pandoraLibrary(String version) {
		final Aether aether = new Aether(collectRemotes(), new File(System.getProperty("user.home") + File.separator + ".m2" + File.separator + "repository"));
		try {
			return aether.resolve(new DefaultArtifact("io.intino.pandora:pandora-plugin:" + version), "compile");
		} catch (DependencyResolutionException e) {
			notifyError(e.getMessage());
			e.printStackTrace();
		}
		return null;
	}


	@NotNull
	private Collection<RemoteRepository> collectRemotes() {
		Collection<RemoteRepository> remotes = new ArrayList<>();
		remotes.add(new RemoteRepository("maven-central", "default", "http://repo1.maven.org/maven2/"));
		remotes.add(new RemoteRepository("intino-maven", "default", "http://artifactory.intino.io/artifactory/release-builders"));
		remotes.addAll(configuration.legioRepositories().stream().map(remote -> new RemoteRepository(remote.name(), "default", remote.url())).collect(Collectors.toList()));
		return remotes;
	}

}
