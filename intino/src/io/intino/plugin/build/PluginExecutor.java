package io.intino.plugin.build;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications.Bus;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.CompilerModuleExtension;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.messages.MessageBus;
import com.intellij.util.messages.MessageBusConnection;
import com.jcabi.aether.Aether;
import io.intino.plugin.MessageProvider;
import io.intino.plugin.PluginLauncher;
import io.intino.plugin.dependencyresolution.ArtifactoryConnector;
import io.intino.plugin.project.LegioConfiguration;
import io.intino.plugin.settings.ArtifactoryCredential;
import io.intino.plugin.settings.IntinoSettings;
import io.intino.plugin.toolwindows.output.IntinoTopics;
import io.intino.plugin.toolwindows.output.MavenListener;
import io.intino.plugin.toolwindows.output.OutputsToolWindow;
import io.intino.tara.compiler.shared.Configuration.Repository;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.idea.maven.project.MavenProjectsManager;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.repository.Authentication;
import org.sonatype.aether.repository.RemoteRepository;
import org.sonatype.aether.repository.RepositoryPolicy;
import org.sonatype.aether.resolution.DependencyResolutionException;
import org.sonatype.aether.util.artifact.DefaultArtifact;
import org.sonatype.aether.util.artifact.JavaScopes;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.*;
import java.util.stream.Collectors;

import static com.intellij.openapi.roots.ModuleRootManager.getInstance;
import static java.util.stream.Collectors.toList;
import static org.jetbrains.idea.maven.utils.MavenUtil.resolveMavenHomeDirectory;
import static org.jetbrains.jps.model.java.JavaResourceRootType.RESOURCE;
import static org.jetbrains.jps.model.java.JavaSourceRootType.SOURCE;

public class PluginExecutor {
	public static final String END = "##end##";
	private static final Logger LOG = Logger.getInstance(PluginExecutor.class);
	private final Module module;
	private final FactoryPhase phase;
	private final LegioConfiguration configuration;
	private final String artifact;
	private final String pluginClass;
	private final List<String> errorMessages;
	private final ProgressIndicator indicator;

	public PluginExecutor(Module module, FactoryPhase phase, LegioConfiguration configuration, String artifact, String pluginClass, List<String> errorMessages, ProgressIndicator indicator) {
		this.module = module;
		this.phase = phase;
		this.configuration = configuration;
		this.artifact = artifact;
		this.pluginClass = pluginClass;
		this.errorMessages = errorMessages;
		this.indicator = indicator;
	}

	private static URL toURL(File l) {
		try {
			return l.toURI().toURL();
		} catch (MalformedURLException e) {
			return null;
		}
	}

	public void execute() {
		try {
			indicator.setText("Running package plugins");
			List<Artifact> artifacts = resolve();
			instantiateAndRun(artifacts);
		} catch (DependencyResolutionException e) {
			errorMessages.add("Plugin artifact not found");
		}
	}

	private void instantiateAndRun(List<Artifact> artifacts) {
		PipedOutputStream out = new PipedOutputStream();
		PrintStream logStream = new PrintStream(out);
		ClassLoader classLoader = createClassLoader(artifacts.stream().map(Artifact::getFile).toArray(File[]::new));
		if (classLoader == null) return;
		ModuleRootManager manager = ModuleRootManager.getInstance(module);
		new Thread(() -> connectLogger(out)).start();
		try {
			Thread thread = new Thread(() -> executePlugin(logStream, classLoader, manager));
			thread.setContextClassLoader(classLoader);
			thread.start();
			thread.join();
		} catch (InterruptedException e) {
			LOG.error(e);
		}
	}

	private void executePlugin(PrintStream logStream, ClassLoader classLoader, ModuleRootManager manager) {
		try {
			PluginLauncher launcher = (PluginLauncher) classLoader.loadClass(pluginClass).getConstructors()[0].newInstance();
			launcher.moduleConfiguration(configuration);
			launcher.moduleDirectory(new File(manager.getContentRootUrls()[0]))
					.moduleStructure(new PluginLauncher.ModuleStructure(srcDirectories(module), resourceDirectories(module), outDirectory()))
					.systemProperties(new PluginLauncher.SystemProperties(mavenHome(), sdkHome()))
					.invokedPhase(PluginLauncher.Phase.valueOf(phase.name()))
					.notifier(new PluginLauncher.Notifier() {
						@Override
						public void notify(String text) {
							Bus.notify(new Notification("Intino", "Plugin execution", text, NotificationType.INFORMATION), module.getProject());
						}

						@Override
						public void notifyError(String text) {
							Bus.notify(new Notification("Intino", MessageProvider.message("error.occurred", "plugin execution"), text, NotificationType.ERROR), module.getProject());
						}
					})
					.logger(logStream);
			launcher.run();
		} catch (Throwable e) {
			if (e instanceof NullPointerException) e.printStackTrace();
			errorMessages.add("Error executing plugin.\n" + e.getMessage());
		}
		logStream.println(END);
	}

	private File mavenHome() {
		final String ijMavenHome = MavenProjectsManager.getInstance(module.getProject()).getGeneralSettings().getMavenHome();
		return resolveMavenHomeDirectory(ijMavenHome);
	}

	private File sdkHome() {
		final Sdk sdk = ModuleRootManager.getInstance(module).getSdk();
		if (sdk != null && sdk.getHomePath() != null) return new File(sdk.getHomePath());
		return null;
	}

	private void connectLogger(PipedOutputStream out) {
		try {
			publish(OutputsToolWindow.CLEAR);
			PipedInputStream stream = new PipedInputStream(out);
			BufferedReader br = new BufferedReader(new InputStreamReader(stream));
			String line;
			while ((line = br.readLine()) != null) {
				if (line.equalsIgnoreCase(END)) {
					break;
				}
				publish(line);
			}
		} catch (IOException ignored) {
		}
	}

	private File outDirectory() {
		final CompilerModuleExtension extension = CompilerModuleExtension.getInstance(module);
		return new File(pathOf(Objects.requireNonNull(extension.getCompilerOutputUrl())));
	}

	private List<Artifact> resolve() throws DependencyResolutionException {
		Aether aether = new Aether(collectRemotes(), localRepository());
		String[] coords = this.artifact.split(":");
		return aether.resolve(new DefaultArtifact(coords[0], coords[1], "jar", coords[2]), JavaScopes.COMPILE);
	}

	@NotNull
	private Collection<RemoteRepository> collectRemotes() {
		Collection<RemoteRepository> remotes = new ArrayList<>();
		remotes.add(new RemoteRepository("maven-central", "default", ArtifactoryConnector.MAVEN_URL).setPolicy(false, new RepositoryPolicy().setEnabled(true).setUpdatePolicy(RepositoryPolicy.UPDATE_POLICY_ALWAYS)));
		remotes.addAll(this.configuration.repositories().stream().filter(r -> r != null && !(r instanceof Repository.Language)).map(this::repository).collect(toList()));
		return remotes;
	}

	private RemoteRepository repository(Repository remote) {
		final RemoteRepository repository = new RemoteRepository(remote.identifier(), "default", remote.url()).setAuthentication(provideAuthentication(remote.identifier()));
		repository.setPolicy(false, new RepositoryPolicy().setEnabled(true).setUpdatePolicy(RepositoryPolicy.UPDATE_POLICY_ALWAYS));
		return repository;
	}

	@NotNull
	private File localRepository() {
		return new File(System.getProperty("user.home") + File.separator + ".m2" + File.separator + "repository");
	}

	private void publish(String line) {
		if (module.getProject().isDisposed()) return;
		final MessageBus messageBus = module.getProject().getMessageBus();
		final MessageBusConnection connect = messageBus.connect();
		final MavenListener mavenListener = messageBus.syncPublisher(IntinoTopics.BUILD_CONSOLE);
		mavenListener.println(line);
		connect.deliverImmediately();
		connect.disconnect();
	}

	private Authentication provideAuthentication(String mavenId) {
		final IntinoSettings settings = IntinoSettings.getSafeInstance(module.getProject());
		for (ArtifactoryCredential credential : settings.artifactories())
			if (credential.serverId.equals(mavenId))
				return new Authentication(credential.username, credential.password);
		return null;
	}

	private List<File> srcDirectories(Module module) {
		return ApplicationManager.getApplication().runReadAction((Computable<List<File>>) () -> {
			final ModuleRootManager manager = getInstance(module);
			final List<VirtualFile> sourceRoots = manager.getModifiableModel().getSourceRoots(SOURCE);
			return sourceRoots.stream().map(virtualFile -> new File(virtualFile.getPath())).collect(Collectors.toList());
		});
	}

	private List<File> resourceDirectories(Module module) {
		return getInstance(module).getSourceRoots(RESOURCE).stream().map(virtualFile -> new File(virtualFile.getPath())).collect(Collectors.toList());
	}

	private String pathOf(String path) {
		if (path.startsWith("file://")) return path.substring("file://".length());
		try {
			return new URL(path).getFile();
		} catch (MalformedURLException e) {
			return path;
		}
	}

	private ClassLoader createClassLoader(File[] libraries) {
		return AccessController.doPrivileged((PrivilegedAction<ClassLoader>) () ->
				new URLClassLoader(Arrays.stream(libraries).map(PluginExecutor::toURL).toArray(URL[]::new), PluginExecutor.class.getClassLoader()));
	}
}
