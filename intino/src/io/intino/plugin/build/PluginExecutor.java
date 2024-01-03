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
import io.intino.plugin.MessageProvider;
import io.intino.plugin.PluginLauncher;
import io.intino.plugin.PluginLauncher.Phase;
import io.intino.plugin.dependencyresolution.MavenDependencyResolver;
import io.intino.plugin.dependencyresolution.Repositories;
import io.intino.plugin.project.configuration.ArtifactLegioConfiguration;
import io.intino.plugin.toolwindows.remote.RemoteWindow;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.repository.RepositoryPolicy;
import org.eclipse.aether.resolution.DependencyResolutionException;
import org.eclipse.aether.resolution.DependencyResult;
import org.eclipse.aether.util.artifact.JavaScopes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.idea.maven.project.MavenProjectsManager;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static com.intellij.openapi.roots.ModuleRootManager.getInstance;
import static org.jetbrains.idea.maven.utils.MavenUtil.resolveMavenHomeDirectory;
import static org.jetbrains.jps.model.java.JavaResourceRootType.RESOURCE;
import static org.jetbrains.jps.model.java.JavaSourceRootType.SOURCE;

public class PluginExecutor {
	public static final String END = "##end##";
	private static final Logger LOG = Logger.getInstance(PluginExecutor.class);
	private final Module module;
	private final FactoryPhase phase;
	private final ArtifactLegioConfiguration configuration;
	private final String artifact;
	private final String pluginClass;
	private final List<String> errorMessages;
	private final ProgressIndicator indicator;

	public PluginExecutor(Module module, FactoryPhase phase, ArtifactLegioConfiguration configuration, String artifact, String pluginClass, List<String> errorMessages, ProgressIndicator indicator) {
		this.module = module;
		this.phase = phase;
		this.configuration = configuration;
		this.artifact = artifact;
		this.pluginClass = pluginClass;
		this.errorMessages = errorMessages;
		this.indicator = indicator;
	}

	public void execute() {
		try {
			indicator.setText("Running package plugins");
			var result = resolve();
			instantiateAndRun(MavenDependencyResolver.dependenciesFrom(result, false));
		} catch (DependencyResolutionException e) {
			errorMessages.add("Intino Plugin " + artifact + " not found");
		}
	}

	private void instantiateAndRun(List<Dependency> dependencies) {
		PipedOutputStream out = new PipedOutputStream();
		PrintStream logStream = new PrintStream(out);
		ClassLoader classLoader = createClassLoader(dependencies.stream().map(d -> d.getArtifact().getFile()).toArray(File[]::new));
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
			launcher.moduleConfiguration(configuration)
					.moduleDirectory(new File(manager.getContentRootUrls()[0]))
					.moduleStructure(new PluginLauncher.ModuleStructure(srcDirectories(module), resourceDirectories(module), outDirectory()))
					.systemProperties(new PluginLauncher.SystemProperties(mavenHome(), sdkHome()))
					.invokedPhase(Phase.valueOf(phase.name()))
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
			publish(RemoteWindow.CLEAR);
			PipedInputStream stream = new PipedInputStream(out);
			BufferedReader br = new BufferedReader(new InputStreamReader(stream));
			String line;
			while ((line = br.readLine()) != null) {
				if (line.equalsIgnoreCase(END)) break;
				publish(line);
			}
		} catch (IOException ignored) {
		}
	}

	private File outDirectory() {
		final CompilerModuleExtension extension = CompilerModuleExtension.getInstance(module);
		return new File(pathOf(Objects.requireNonNull(extension.getCompilerOutputUrl())));
	}

	private DependencyResult resolve() throws DependencyResolutionException {
		var resolver = new MavenDependencyResolver(collectRemotes());
		String[] coords = this.artifact.split(":");
		return resolver.resolve(new DefaultArtifact(coords[0], coords[1], "jar", coords[2]), JavaScopes.COMPILE);
	}

	@NotNull
	private List<RemoteRepository> collectRemotes() {
		List<RemoteRepository> remotes = new ArrayList<>();
		Repositories repositories = new Repositories(this.module);
		remotes.add(repositories.maven(RepositoryPolicy.UPDATE_POLICY_ALWAYS));
		remotes.addAll(repositories.map(this.configuration.repositories()));
		return remotes;
	}

	private void publish(String line) {
		Logger.getInstance(this.getClass()).info(line);
	}

	private List<File> srcDirectories(Module module) {
		return ApplicationManager.getApplication().runReadAction((Computable<List<File>>) () -> {
			final ModuleRootManager manager = getInstance(module);
			final List<VirtualFile> sourceRoots = manager.getModifiableModel().getSourceRoots(SOURCE);
			return sourceRoots.stream().map(virtualFile -> new File(virtualFile.getPath())).toList();
		});
	}

	private List<File> resourceDirectories(Module module) {
		return getInstance(module).getSourceRoots(RESOURCE).stream().map(virtualFile -> new File(virtualFile.getPath())).toList();
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
		return new URLClassLoader(Arrays.stream(libraries).map(PluginExecutor::toURL).toArray(URL[]::new), PluginExecutor.class.getClassLoader());
	}

	private static URL toURL(File l) {
		try {
			return l.toURI().toURL();
		} catch (MalformedURLException e) {
			return null;
		}
	}
}
