package io.intino.plugin.project.builders;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.jcabi.aether.Aether;
import io.intino.magritte.Language;
import io.intino.magritte.dsl.Tara;
import io.intino.plugin.dependencyresolution.ArtifactoryConnector;
import io.intino.plugin.lang.LanguageManager;
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

import static io.intino.plugin.dependencyresolution.ArtifactoryConnector.MAVEN_URL;
import static org.eclipse.aether.repository.RepositoryPolicy.UPDATE_POLICY_DAILY;

public class InterfaceBuilderManager {
	public static final String INTINO_RELEASES = "https://artifactory.intino.io/artifactory/releases";
	public static final String BOX_LANGUAGE = "Konos";
	public static final String GROUP_ID = "io.intino.konos";
	public static final String ARTIFACT_ID = "builder";
	private static final Logger logger = Logger.getInstance(InterfaceBuilderManager.class.getName());
	private static final File LOCAL_REPOSITORY = new File(System.getProperty("user.home") + File.separator + ".m2" + File.separator + "repository");
	private static final Map<String, ClassLoader> loadedVersions = new HashMap<>();
	private static final Aether aether = new Aether(collectRemotes(), LOCAL_REPOSITORY);
	public static String minimunVersion = "8.5.1";

	public String load(Module module, String version) {
		String effectiveVersion = version;
		if (version.equals("LATEST")) {
			List<String> versions = new ArtifactoryConnector(Collections.emptyList()).boxBuilderVersions();
			effectiveVersion = versions.get(versions.size() - 1);
		}
		if (isLoaded(effectiveVersion)) {
			downloadAndSaveClassPath(module, effectiveVersion);
			Logger.getInstance(InterfaceBuilderManager.class).info("Konos " + version + " is already downloaded");
			return effectiveVersion;
		}
		downloadAndSaveClassPath(module, version);
		loadLanguage(module, version, effectiveVersion);
		return effectiveVersion;
	}

	private void downloadAndSaveClassPath(Module module, String version) {
		if (isDownloaded(version) && classpathContains(module, version)) return;
		List<Artifact> artifacts = konosLibrary(version);
		if (!artifacts.isEmpty()) saveClassPath(module, librariesOf(artifacts));
	}

	private boolean classpathContains(Module module, String version) {
		try {
			Path path = InterfaceBuilderManager.classpathFile(IntinoDirectory.boxDirectory(module));
			if (!path.toFile().exists()) return false;
			List<String> classpath = Arrays.asList(Files.readString(path).replace("$HOME", System.getProperty("user.home")).split(":"));
			return !classpath.isEmpty() && classpath.get(0).equals(mainArtifact(version).getAbsolutePath());
		} catch (IOException e) {
			logger.error(e);
		}
		return false;
	}

	private void loadLanguage(Module module, String version, String effectiveVersion) {
		if (effectiveVersion.compareTo(minimunVersion) < 0) return;
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

	private Language loadLanguage(ClassLoader classLoader) {
		try {
			return (Language) classLoader.loadClass(LanguageManager.DSL_GROUP_ID + ".Konos").getConstructors()[0].newInstance();
		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException | InvocationTargetException e) {
			return null;
		}
	}

	private boolean isDownloaded(String version) {
		return mainArtifact(version).exists();
	}

	@NotNull
	private File mainArtifact(String version) {
		return new File(LOCAL_REPOSITORY, "io/intino/konos/builder/" + version + "/" + "builder-" + version + ".jar");
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
	private static Collection<RemoteRepository> collectRemotes() {
		Collection<RemoteRepository> remotes = new ArrayList<>();
		try {
			remotes.add(new RemoteRepository("local", "default", LOCAL_REPOSITORY.toURI().toURL().toString()).setPolicy(false, new RepositoryPolicy().setEnabled(true).setUpdatePolicy(UPDATE_POLICY_DAILY)));
		} catch (MalformedURLException ignored) {
		}
		remotes.add(new RemoteRepository("intino-maven", "default", INTINO_RELEASES).setPolicy(false, new RepositoryPolicy().setEnabled(true).setUpdatePolicy(UPDATE_POLICY_DAILY)));
		remotes.add(new RemoteRepository("maven-central", "default", MAVEN_URL).setPolicy(false, new RepositoryPolicy().setEnabled(true).setUpdatePolicy(UPDATE_POLICY_DAILY)));
		return remotes;
	}
}
