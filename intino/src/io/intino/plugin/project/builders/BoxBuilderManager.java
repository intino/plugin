package io.intino.plugin.project.builders;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.jcabi.aether.Aether;
import io.intino.Configuration;
import io.intino.magritte.Language;
import io.intino.magritte.dsl.Tara;
import io.intino.plugin.IntinoException;
import io.intino.plugin.dependencyresolution.ArtifactoryConnector;
import io.intino.plugin.dependencyresolution.Repositories;
import io.intino.plugin.lang.LanguageManager;
import io.intino.plugin.project.IntinoDirectory;
import io.intino.plugin.project.configuration.Version;
import org.jetbrains.annotations.NotNull;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.repository.RemoteRepository;
import org.sonatype.aether.resolution.DependencyResolutionException;
import org.sonatype.aether.util.artifact.DefaultArtifact;
import org.sonatype.aether.util.artifact.JavaScopes;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.*;
import java.util.stream.Collectors;

import static io.intino.plugin.dependencyresolution.Repositories.LOCAL;
import static org.sonatype.aether.repository.RepositoryPolicy.UPDATE_POLICY_DAILY;

public class BoxBuilderManager {
	public static final String MinimumVersion = "9.7.0";
	public static final String BOX_LANGUAGE = "Konos";
	public static final String GROUP_ID = "io.intino.konos";
	public static final String ARTIFACT_ID = "builder";
	private static final Logger logger = Logger.getInstance(BoxBuilderManager.class.getName());
	private static final Map<String, ClassLoader> loadedVersions = new HashMap<>();
	private final Aether aether;
	private final Module module;
	private final List<Configuration.Repository> repositories;

	public BoxBuilderManager(Module module, List<Configuration.Repository> repositories) {
		this.module = module;
		this.repositories = repositories;
		aether = new Aether(collectRemotes(), LOCAL);
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
		downloadAndSaveClassPath(version);
		loadLanguage(version, effectiveVersion);
		return effectiveVersion;
	}

	private void downloadAndSaveClassPath(String version) {
		if (isDownloaded(version) && classpathContains(version)) return;
		List<Artifact> artifacts = konosLibrary(version);
		if (!artifacts.isEmpty()) saveClassPath(module, librariesOf(artifacts));
	}

	private boolean classpathContains(String version) {
		try {
			Path path = BoxBuilderManager.classpathFile(IntinoDirectory.boxDirectory(module));
			if (!path.toFile().exists()) return false;
			List<String> classpath = Arrays.asList(Files.readString(path).replace("$HOME", System.getProperty("user.home")).split(":"));
			return !classpath.isEmpty() && classpath.get(0).equals(mainArtifact(version).getAbsolutePath());
		} catch (IOException e) {
			logger.error(e);
		}
		return false;
	}

	private void loadLanguage(String version, String effectiveVersion) {
		if (!isSuitable(effectiveVersion)) return;
		if (isLoaded(effectiveVersion)) return;
		final ClassLoader classLoader;
		if (isLoaded(effectiveVersion)) classLoader = loadedVersions.get(effectiveVersion);
		else classLoader = createClassLoader(mainArtifact(effectiveVersion));
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

	private List<Artifact> konosLibrary(String version) {
		try {
			return aether.resolve(new DefaultArtifact(GROUP_ID, ARTIFACT_ID, "jar", version), JavaScopes.COMPILE);
		} catch (DependencyResolutionException e) {
			return Collections.emptyList();
		}
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
			Path path = classpathFile(moduleBoxDirectory);
			path.toFile().getParentFile().mkdirs();
			Files.write(path, String.join(":", libraries).getBytes());
		} catch (IOException e) {
			logger.error(e);
		}
	}

	private URL toURL(File l) {
		try {
			return l.toURI().toURL();
		} catch (MalformedURLException e) {
			return null;
		}
	}

	private ClassLoader createClassLoader(File library) {
		return AccessController.doPrivileged((PrivilegedAction<ClassLoader>) () ->
				new URLClassLoader(new URL[]{toURL(library)}, Tara.class.getClassLoader()));
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
	private Collection<RemoteRepository> collectRemotes() {
		Repositories repositoryManager = new Repositories(module);
		List<RemoteRepository> remotes = new ArrayList<>(repositoryManager.map(repositories));
		remotes.add(repositoryManager.local());
		if (remotes.stream().noneMatch(r -> r.getUrl().equals(Repositories.INTINO_RELEASES)))
			remotes.add(repositoryManager.intino(UPDATE_POLICY_DAILY));
		remotes.add(repositoryManager.maven(UPDATE_POLICY_DAILY));
		return remotes;
	}
}
