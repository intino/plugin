package io.intino.plugin.actions.box.accessor;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationGroup;
import com.intellij.notification.NotificationGroupManager;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications.Bus;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import io.intino.Configuration;
import io.intino.itrules.Frame;
import io.intino.itrules.FrameBuilder;
import io.intino.plugin.build.maven.MavenRunner;
import io.intino.plugin.dependencyresolution.ArtifactoryConnector;
import io.intino.plugin.project.LegioConfiguration;
import org.apache.commons.io.FileUtils;
import org.apache.maven.shared.invoker.InvocationResult;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static com.intellij.notification.NotificationType.ERROR;
import static com.intellij.notification.NotificationType.INFORMATION;
import static io.intino.plugin.project.Safe.safe;

public class AccessorsPublisher {
	private static final Logger LOG = Logger.getInstance("Publishing Accessor:");
	private static final String ACCESSOR = "-accessor";
	private final Module module;
	private final StringBuilder log = new StringBuilder();
	private final File root;
	private final LegioConfiguration conf;

	public AccessorsPublisher(Module module, LegioConfiguration conf, File root) {
		this.module = module;
		this.conf = conf;
		this.root = root;
	}

	public void install() {
		try {
			for (File serviceDirectory : Objects.requireNonNull(root.listFiles(File::isDirectory))) {
				File[] sources = serviceDirectory.listFiles();
				if (sources == null || sources.length == 0) return;
				mvn(serviceDirectory, conf, "install");
			}
			FileUtils.deleteDirectory(root);
		} catch (IOException e) {
			notifyError(e.getMessage());
			LOG.error(e.getMessage());
		}
	}

	public void publish() {
		try {
			for (File serviceDirectory : Objects.requireNonNull(root.listFiles(File::isDirectory))) {
				File[] sources = serviceDirectory.listFiles();
				if (sources == null || sources.length == 0) return;
				mvn(serviceDirectory, conf, "deploy");
			}
			FileUtils.deleteDirectory(root);
		} catch (IOException e) {
			notifyError(e.getMessage());
		}
	}

	private void mvn(File serviceDirectory, Configuration conf, String goal) throws IOException {
		String[] name = serviceDirectory.getName().split("#");
		final File pom = createPom(serviceDirectory, name[0], accessorGroupId(), accessorArtifactId(name[1]), conf.artifact().version());
		final InvocationResult result = new MavenRunner(module).invokeMavenWithConfiguration(pom, goal);
		if (result != null && result.getExitCode() != 0) {
			if (result.getExecutionException() != null)
				throw new IOException("Failed to publish accessor.", result.getExecutionException());
			else throw new IOException("Failed to publish accessor. Exit code: " + result.getExitCode());
		} else if (result == null) throw new IOException("Failed to publish accessor. Maven HOME not found");
		notifySuccess(this.conf, name[1]);
	}

	private File createPom(File root, String serviceType, String group, String artifact, String version) {
		final FrameBuilder builder = new FrameBuilder("pom").add("group", group).add("artifact", artifact).add("version", version);
		conf.repositories().forEach(r -> buildRepoFrame(builder, r, false, r instanceof Configuration.Repository.Snapshot));
		if (safe(() -> conf.artifact().distribution().release()) != null)
			buildRepoFrame(builder, conf.artifact().distribution().release(), true, false);
		if (safe(() -> conf.artifact().distribution().snapshot()) != null)
			buildRepoFrame(builder, conf.artifact().distribution().snapshot(), true, true);
		builder.add("dependency", new FrameBuilder(serviceType).add("value", "").add("version", versionOf(serviceType)).toFrame());
		final File pomFile = new File(root, "pom.xml");
		write(builder, pomFile);
		return pomFile;
	}

	private String versionOf(String serviceType) {
		String artifact = "";
		if ("rest".equals(serviceType)) artifact = "io.intino.alexandria:rest-accessor";
		else if ("messaging".equals(serviceType)) artifact = "io.intino.alexandria:terminal-jms";
		else if ("analytic".equals(serviceType)) artifact = "io.intino.alexandria:led";
		List<String> versions = new ArtifactoryConnector(conf.repositories()).versions(artifact);
		if (versions.isEmpty()) return "";
		Collections.sort(versions);
		return versions.get(versions.size() - 1);
	}

	private void write(FrameBuilder builder, File pomFile) {
		try {
			Files.writeString(pomFile.toPath(), new AccessorPomTemplate().render(builder.toFrame()));
		} catch (IOException e) {
			LOG.error(e.getMessage());
		}
	}

	private void buildRepoFrame(FrameBuilder builder, Configuration.Repository r, boolean isDistribution, boolean snapshot) {
		builder.add("repository", createRepositoryFrame(r, isDistribution, snapshot));
	}

	private Frame createRepositoryFrame(Configuration.Repository repository, boolean isDistribution, boolean snapshot) {
		FrameBuilder builder = new FrameBuilder("repository").
				add("name", repository.identifier()).
				add("random", UUID.randomUUID().toString()).
				add("url", repository.url());
		if (isDistribution) builder.add("distribution");
		if (snapshot) builder.add("snapshot");
		return builder.toFrame();
	}

	private void notifySuccess(Configuration conf, String app) {
		NotificationGroup balloon = NotificationGroup.findRegisteredGroup("Intino");
		balloon.createNotification("Accessors generated and uploaded", message(), INFORMATION).addAction(new AnAction() {
			@Override
			public void actionPerformed(@NotNull AnActionEvent e) {
				StringSelection selection = new StringSelection(newDependency(conf, app));
				Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
				clipboard.setContents(selection, selection);
			}
		}).setImportant(true).notify(module.getProject());
	}

	@NotNull
	private String message() {
		return "<a href=\"#\">Copy maven dependency</a>";
	}

	@NotNull
	private String newDependency(Configuration conf, String app) {
		return "<dependency>\n" +
				"    <groupId>" + accessorGroupId() + "</groupId>\n" +
				"    <artifactId>" + accessorArtifactId(app) + "</artifactId>\n" +
				"    <version>" + conf.artifact().version() + "</version>\n" +
				"</dependency>";
	}

	@NotNull
	private String accessorGroupId() {
		return conf.artifact().groupId().toLowerCase();
	}

	@NotNull
	private String accessorArtifactId(String service) {
		return conf.artifact().name().toLowerCase() + "-" + service.toLowerCase() + ACCESSOR;
	}

	private void notifyError(String message) {
		final String result = log.toString();
		Bus.notify(new Notification("Intino", "Accessor cannot be published. ", message + "\n" + result, ERROR), module.getProject());
	}

}
