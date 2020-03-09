package io.intino.plugin.project.builders;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.jcabi.aether.Aether;
import io.intino.magritte.Language;
import io.intino.magritte.dsl.Tara;
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
	private static final File LOCAL_REPOSITORY = new File(System.getProperty("user.home") + File.separator + ".m2" + File.separator + "repository");
	public static String minimunVersion = "8.0.0";
	private static Map<String, ClassLoader> loadedVersions = new HashMap<>();
	private static Map<Project, String> versionsByProject = new HashMap<>();
	private final Aether aether;


	public InterfaceBuilderManager() {
		aether = new Aether(collectRemotes(), LOCAL_REPOSITORY);
	}

	public static boolean exists(String version) {
		//TODO
		return true;
	}

	@NotNull
	public static Path classpathFile(File moduleBoxDirectory) {
		return new File(moduleBoxDirectory, "compiler.classpath").toPath();
	}

	public String load(Module module, String version) {

		if (isDownloaded(version)) {
			Logger.getInstance(InterfaceBuilderManager.class).info("Konos " + version + " is already downloaded");
			return version;
		}
		List<Artifact> artifacts = konosLibrary(version);
		saveClassPath(module, librariesOf(artifacts));
		loadLanguage(List.of(artifacts.get(0).getFile()), module, version);
		if (!artifacts.isEmpty()) return artifacts.get(0).getVersion();
		return version;
	}

	private void loadLanguage(List<File> builderLibrary, Module module, String version) {
		if (version.compareTo(minimunVersion) < 0) return;
		if (isLoaded(module.getProject(), version)) return;
		final ClassLoader classLoader = areClassesLoaded(version) ? loadedVersions.get(version) : createClassLoader(builderLibrary);
		Language language = loadLanguage(classLoader);
		if (language != null) LanguageManager.registerAuxiliar(module.getProject(), language);
	}

	private boolean isLoaded(Project project, String version) {
		return version != null && version.equalsIgnoreCase(versionsByProject.get(project)) && areClassesLoaded(version);
	}

	private boolean areClassesLoaded(String version) {
		return loadedVersions.containsKey(version);
	}

	private Language loadLanguage(ClassLoader classLoader) {
		try {
			return (Language) classLoader.loadClass(LanguageManager.DSL_GROUP_ID + ".Konos").getConstructors()[0].newInstance();
		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException | InvocationTargetException e) {
			return null;
		}
	}

	private boolean isDownloaded(String version) {
		return versionsByProject.containsValue(version);
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
			return aether.resolve(new DefaultArtifact("io.intino.konos", "builder", "jar", version), JavaScopes.COMPILE);
		} catch (DependencyResolutionException e) {
			return Collections.emptyList();
		}
	}

	@NotNull
	private Collection<RemoteRepository> collectRemotes() {
		Collection<RemoteRepository> remotes = new ArrayList<>();
		try {
			remotes.add(new RemoteRepository("local", "default", LOCAL_REPOSITORY.toURI().toURL().toString()).setPolicy(false, new RepositoryPolicy().setEnabled(true).setUpdatePolicy(UPDATE_POLICY_DAILY)));
		} catch (MalformedURLException ignored) {
		}
		remotes.add(new RemoteRepository("intino-maven", "default", INTINO_RELEASES).setPolicy(false, new RepositoryPolicy().setEnabled(true).setUpdatePolicy(UPDATE_POLICY_DAILY)));
		remotes.add(new RemoteRepository("maven-central", "default", MAVEN_URL).setPolicy(false, new RepositoryPolicy().setEnabled(true).setUpdatePolicy(UPDATE_POLICY_DAILY)));
		return remotes;
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
			moduleBoxDirectory.mkdirs();
			Files.write(classpathFile(moduleBoxDirectory), String.join(":", libraries).getBytes());
		} catch (IOException e) {
		}
	}


	private URL toURL(File l) {
		try {
			return l.toURI().toURL();
		} catch (MalformedURLException e) {
			return null;
		}
	}

	private ClassLoader createClassLoader(List<File> libraries) {
		return AccessController.doPrivileged((PrivilegedAction<ClassLoader>) () ->
				new URLClassLoader(libraries.stream().filter(Objects::nonNull).map(this::toURL).toArray(URL[]::new), Tara.class.getClassLoader()));
	}
}
