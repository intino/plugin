package io.intino.plugin.project.builders;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import io.intino.Configuration;
import io.intino.plugin.IntinoException;
import io.intino.plugin.dependencyresolution.ArtifactoryConnector;
import io.intino.plugin.dependencyresolution.MavenDependencyResolver;
import io.intino.plugin.dependencyresolution.Repositories;
import io.intino.plugin.lang.LanguageManager;
import io.intino.plugin.project.IntinoDirectory;
import io.intino.plugin.project.configuration.Version;
import io.intino.tara.Language;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.DependencyResolutionException;
import org.eclipse.aether.resolution.DependencyResult;
import org.eclipse.aether.util.artifact.JavaScopes;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;

import static io.intino.plugin.dependencyresolution.MavenDependencyResolver.dependenciesFrom;
import static io.intino.plugin.dependencyresolution.Repositories.LOCAL;
import static org.apache.maven.artifact.repository.ArtifactRepositoryPolicy.UPDATE_POLICY_DAILY;

public class BoxBuilderManager {
	public static final String MinimumVersion = "11.0.0";
	public static final String BOX_LANGUAGE = "Konos";
	public static final String GROUP_ID = "io.intino.konos";
	public static final String ARTIFACT_ID = "builder";
	private static final Logger logger = Logger.getInstance(BoxBuilderManager.class.getName());
	private static final Map<String, ClassLoader> loadedVersions = new HashMap<>();
	private final Module module;
	private final List<Configuration.Repository> repositories;
	private final MavenDependencyResolver resolver;

	public BoxBuilderManager(Module module, List<Configuration.Repository> repositories) {
		this.module = module;
		this.repositories = repositories;
		this.resolver = new MavenDependencyResolver(collectRemotes());
	}

	public String load(String version) {
		String effectiveVersion = version;
		if (version.equals("LATEST")) {
			List<String> versions = new ArtifactoryConnector(Collections.emptyList()).boxBuilderVersions();
			effectiveVersion = versions.get(versions.size() - 1);
		}
		if (isLoaded(effectiveVersion)) {
			downloadAndSaveClassPath(effectiveVersion);
			Logger.getInstance(BoxBuilderManager.class).info("Konos " + version + " is already downloaded");
			return effectiveVersion;
		}
		List<String> libraries = downloadAndSaveClassPath(version);
		loadLanguage(version, effectiveVersion, libraries);
		return effectiveVersion;
	}

	private List<String> downloadAndSaveClassPath(String version) {
		if (isDownloaded(version) && classpathContains(version)) return classpath();
		try {
			List<String> paths = librariesOf(konosLibrary(version));
			saveClassPath(module, paths);
			return paths;
		} catch (DependencyResolutionException e) {
			Notifications.Bus.notify(new Notification("Intino", "Dependency not found", e.getMessage(), NotificationType.ERROR), null);
			return Collections.emptyList();
		}
	}

	private boolean classpathContains(String version) {
		List<String> classpath = classpath();
		if (classpath == null) return false;
		return !classpath.isEmpty() && classpath.get(0).equals(mainArtifact(version).getAbsolutePath());
	}

	private List<String> classpath() {
		Path path = BoxBuilderManager.classpathFile(IntinoDirectory.boxDirectory(module));
		if (!path.toFile().exists()) return Collections.emptyList();
		try {
			return Arrays.asList(Files.readString(path).replace("$HOME", System.getProperty("user.home")).split(":"));
		} catch (IOException e) {
			return Collections.emptyList();
		}
	}

	private void loadLanguage(String version, String effectiveVersion, List<String> libraries) {
		if (!isSuitable(effectiveVersion)) return;
		if (isLoaded(effectiveVersion)) return;
		final ClassLoader classLoader = createClassLoader(libraries.stream().map(File::new));
		Language language = loadLanguage(classLoader);
		if (language != null) {
			LanguageManager.registerBoxLanguage(module.getProject(), language, effectiveVersion);
			loadedVersions.put(effectiveVersion, classLoader);
			if (!effectiveVersion.equals(version)) {
				LanguageManager.registerBoxLanguage(module.getProject(), language, version);
				loadedVersions.put(version, classLoader);
			}
		}
	}

	private boolean isSuitable(String effectiveVersion) {
		try {
			return new Version(effectiveVersion).compareTo(new Version(MinimumVersion)) >= 0;
		} catch (IntinoException e) {
			return false;
		}
	}

	private Language loadLanguage(ClassLoader classLoader) {
		try {
			return (Language) classLoader.loadClass(LanguageManager.DSL_GROUP_ID + ".Konos").getConstructors()[0].newInstance();
		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException |
				 InvocationTargetException e) {
			return null;
		}
	}

	private boolean isDownloaded(String version) {
		return mainArtifact(version).exists();
	}

	@NotNull
	private File mainArtifact(String version) {
		return new File(LOCAL, "io/intino/konos/builder/" + version + "/" + "builder-" + version + ".jar");
	}

	private DependencyResult konosLibrary(String version) throws DependencyResolutionException {
		return resolver.resolve(new DefaultArtifact(GROUP_ID, ARTIFACT_ID, "jar", version), JavaScopes.COMPILE);
	}

	private List<String> librariesOf(DependencyResult result) {
		return dependenciesFrom(result, false).stream()
				.map(d -> d.getArtifact().getFile().getAbsolutePath())
				.toList();
	}

	private void saveClassPath(Module module, List<String> paths) {
		if (paths.isEmpty()) return;
		final String home = System.getProperty("user.home");
		List<String> libraries = paths.stream().map(l -> l.replace(home, "$HOME")).toList();
		try {
			File moduleBoxDirectory = new File(IntinoDirectory.boxDirectory(module.getProject()), module.getName());
			Path path = classpathFile(moduleBoxDirectory);
			path.toFile().getParentFile().mkdirs();
			Files.write(path, String.join(":", libraries).getBytes());
		} catch (IOException e) {
			logger.error(e);
		}
	}

	private ClassLoader createClassLoader(Stream<File> libraries) {
		return new URLClassLoader(libraries.map(this::toURL).toArray(URL[]::new));
	}

	private URL toURL(File l) {
		try {
			return l.toURI().toURL();
		} catch (MalformedURLException e) {
			return null;
		}
	}

	public static boolean exists(String version) {
		return isLoaded(version);
	}

	@NotNull
	public static Path classpathFile(File moduleBoxDirectory) {
		return new File(moduleBoxDirectory, "compiler.classpath").toPath();
	}

	private static boolean isLoaded(String version) {
		return version != null && loadedVersions.containsKey(version);
	}

	@NotNull
	private List<RemoteRepository> collectRemotes() {
		Repositories repositoryManager = new Repositories(module);
		List<RemoteRepository> remotes = repositoryManager.map(repositories);
		remotes.add(repositoryManager.local());
		if (remotes.stream().noneMatch(r -> r.getUrl().equals(Repositories.INTINO_RELEASES)))
			remotes.add(repositoryManager.intino(UPDATE_POLICY_DAILY));
		remotes.add(repositoryManager.maven(UPDATE_POLICY_DAILY));
		return remotes;
	}
}
