package io.intino.plugin.actions.box.accessor;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationGroup;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications.Bus;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.util.messages.MessageBus;
import com.intellij.util.messages.MessageBusConnection;
import io.intino.Configuration;
import io.intino.itrules.Frame;
import io.intino.itrules.FrameBuilder;
import io.intino.plugin.project.LegioConfiguration;
import io.intino.plugin.toolwindows.output.IntinoTopics;
import io.intino.plugin.toolwindows.output.MavenListener;
import org.apache.maven.shared.invoker.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.idea.maven.project.MavenProjectsManager;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static com.intellij.notification.NotificationType.ERROR;
import static com.intellij.notification.NotificationType.INFORMATION;
import static org.jetbrains.idea.maven.utils.MavenUtil.resolveMavenHomeDirectory;

public class AccessorsPublisher {
	private static final Logger LOG = Logger.getInstance("Publishing Accessor:");
	private static final String ACCESSOR = "-accessor";
	private final Module module;
	private StringBuilder log = new StringBuilder();
	private File root;
	private LegioConfiguration conf;

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
		} catch (IOException | MavenInvocationException e) {
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
		} catch (IOException | MavenInvocationException e) {
			notifyError(e.getMessage());
		}
	}

	private void mvn(File serviceDirectory, Configuration conf, String goal) throws MavenInvocationException, IOException {
		String[] name = serviceDirectory.getName().split("#");
		final File pom = createPom(serviceDirectory, name[0], accessorGroupId(), accessorArtifactId(name[1]), conf.artifact().version());
		final InvocationResult result = invoke(pom, goal);
		if (result != null && result.getExitCode() != 0) {
			if (result.getExecutionException() != null)
				throw new IOException("Failed to publish accessor.", result.getExecutionException());
			else throw new IOException("Failed to publish accessor. Exit code: " + result.getExitCode());
		} else if (result == null) throw new IOException("Failed to publish accessor. Maven HOME not found");
		notifySuccess(this.conf, name[1]);
	}

	private InvocationResult invoke(File pom, String goal) throws MavenInvocationException {
		List<String> goals = new ArrayList<>();
		final String ijMavenHome = MavenProjectsManager.getInstance(module.getProject()).getGeneralSettings().getMavenHome();
		goals.add("clean");
		goals.add("install");
		if (!goal.isEmpty()) goals.add(goal);
		InvocationRequest request = new DefaultInvocationRequest().setPomFile(pom).setGoals(goals);
		final File mavenHome = resolveMavenHomeDirectory(ijMavenHome);
		if (mavenHome == null) return null;
		LOG.info("Maven HOME: " + mavenHome.getAbsolutePath());
		Invoker invoker = new DefaultInvoker().setMavenHome(mavenHome);
		log(invoker);
		config(request, mavenHome);
		return invoker.execute(request);
	}

	private void log(Invoker invoker) {
		invoker.setErrorHandler(LOG::error);
		invoker.setOutputHandler(this::publish);
	}

	private void publish(String line) {
		if (module.getProject().isDisposed()) return;
		final MessageBus messageBus = module.getProject().getMessageBus();
		final MavenListener mavenListener = messageBus.syncPublisher(IntinoTopics.BUILD_CONSOLE);
		mavenListener.println(line);
		final MessageBusConnection connect = messageBus.connect();
		connect.deliverImmediately();
		connect.disconnect();
	}

	private void config(InvocationRequest request, File mavenHome) {
		final File mvn = new File(mavenHome, "bin" + File.separator + "mvn");
		mvn.setExecutable(true);
		final Sdk sdk = ModuleRootManager.getInstance(module).getSdk();
		if (sdk != null && sdk.getHomePath() != null) request.setJavaHome(new File(sdk.getHomePath()));
	}

	private File createPom(File root, String serviceType, String group, String artifact, String version) {
		final FrameBuilder builder = new FrameBuilder("pom").add("group", group).add("artifact", artifact).add("version", version);
		conf.repositories().stream().filter(r -> !(r instanceof Configuration.Repository.Language)).forEach(r -> buildRepoFrame(builder, r, version.contains("SNAPSHOT")));
		builder.add("dependency", new FrameBuilder(serviceType).add("value", "").toFrame());
		final File pomFile = new File(root, "pom.xml");
		write(builder, pomFile);
		return pomFile;
	}

	private void write(FrameBuilder builder, File pomFile) {
		try {
			Files.writeString(pomFile.toPath(), new AccessorPomTemplate().render(builder.toFrame()));
		} catch (IOException e) {
			LOG.error(e.getMessage());
		}
	}

	private void buildRepoFrame(FrameBuilder builder, Configuration.Repository r, boolean snapshot) {
		builder.add("repository", createRepositoryFrame(r, snapshot));
	}

	private Frame createRepositoryFrame(Configuration.Repository repository, boolean snapshot) {
		return new FrameBuilder("repository", isDistribution(repository, snapshot) ? "distribution" : "release").
				add("name", repository.identifier()).
				add("random", UUID.randomUUID().toString()).
				add("url", repository.url()).toFrame();
	}

	private boolean isDistribution(Configuration.Repository repository, boolean snapshot) {
		Configuration.Distribution distribution = conf.artifact().distribution();
		if (distribution == null) return false;
		Configuration.Repository repo = snapshot ? distribution.snapshot() : distribution.release();
		return repo != null && repository.identifier().equals(repo.identifier()) &&
				repository.url().equals(repo.url());
	}

	private void notifySuccess(Configuration conf, String app) {
		final NotificationGroup balloon = NotificationGroup.toolWindowGroup("Tara Language", "Balloon");
		balloon.createNotification("Accessors generated and uploaded", message(), INFORMATION, (n, e) -> {
			StringSelection selection = new StringSelection(newDependency(conf, app));
			Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
			clipboard.setContents(selection, selection);
		}).setImportant(true).notify(module.getProject());
	}

	private void notify(String message, NotificationType type) {
		Bus.notify(
				new Notification("Konos", message, module.getName(), type), module.getProject());
	}


	private void write(Path file, String text) {
		try {
			Files.write(file, text.getBytes(StandardCharsets.UTF_8));
		} catch (IOException e) {
			e.printStackTrace();

		}
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
		Bus.notify(new Notification("Konos", "Accessor cannot be published. ", message + "\n" + result, ERROR), module.getProject());
	}

}
