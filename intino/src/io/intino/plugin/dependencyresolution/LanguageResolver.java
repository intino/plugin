package io.intino.plugin.dependencyresolution;

import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.roots.DependencyScope;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.util.Computable;
import com.jcabi.aether.Aether;
import io.intino.legio.Project.Factory;
import io.intino.legio.Project.Repositories.Repository;
import io.intino.plugin.settings.ArtifactoryCredential;
import io.intino.plugin.settings.IntinoSettings;
import io.intino.tara.compiler.shared.Configuration;
import io.intino.tara.dsl.Proteo;
import io.intino.tara.dsl.Verso;
import io.intino.tara.plugin.lang.LanguageManager;
import io.intino.tara.plugin.lang.psi.impl.TaraUtil;
import org.jetbrains.annotations.NotNull;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.repository.Authentication;
import org.sonatype.aether.repository.RemoteRepository;
import org.sonatype.aether.resolution.DependencyResolutionException;
import org.sonatype.aether.util.artifact.DefaultArtifact;
import org.sonatype.aether.util.artifact.JavaScopes;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.stream.Collectors;

import static com.intellij.openapi.application.ModalityState.defaultModalityState;

public class LanguageResolver {
	private static final Logger LOG = Logger.getInstance(LanguageResolver.class);

	private final Module module;
	private final List<Repository> repositories;
	private Factory.Language factoryLanguage;
	private final String version;
	private File localRepository = new File(System.getProperty("user.home") + File.separator + ".m2" + File.separator + "repository");

	public LanguageResolver(Module module, List<Repository> repositories, Factory.Language factoryLanguage, String version) {
		this.module = module;
		this.repositories = repositories;
		this.factoryLanguage = factoryLanguage;
		this.version = version;
	}

	public List<Library> resolve() {
		if (factoryLanguage == null) return Collections.emptyList();
		LanguageManager.silentReload(this.module.getProject(), factoryLanguage.name$(), version);
		final List<Library> libraries = isMagritteLibrary(this.factoryLanguage.name$()) ? magritte(version) : languageFramework();
		if (!libraries.isEmpty()) resolveBuilder(libraries);
		return libraries;
	}

	private List<Library> magritte(String version) {
		List<Library> libraries = new ArrayList<>();
		final Application application = ApplicationManager.getApplication();
		if (application.isWriteAccessAllowed())
			libraries.addAll(application.runWriteAction((Computable<List<Library>>) () ->
					loadMagritteLibrary(version, libraries)));
		else
			application.invokeAndWait(() -> libraries.addAll(application.runWriteAction((Computable<List<Library>>) () ->
					loadMagritteLibrary(version, libraries))), defaultModalityState());
		return libraries;
	}

	private List<Library> loadMagritteLibrary(String version, List<Library> libraries) {
		final LibraryManager manager = new LibraryManager(module);
		final Map<Artifact, DependencyScope> languageFramework = findLanguageFramework(magritteID(version));
		factoryLanguage.effectiveVersion(!languageFramework.isEmpty() ? languageFramework.keySet().iterator().next().getVersion() : "");
		final Map<DependencyScope, List<Library>> registeredLibraries = manager.registerOrGetLibrary(languageFramework);
		libraries.addAll(flat(registeredLibraries));
		manager.addToModule(libraries, DependencyScope.COMPILE);
		return libraries;
	}

	private List<Library> flat(Map<DependencyScope, List<Library>> registeredLibraries) {
		return registeredLibraries.values().stream().flatMap(Collection::stream).collect(Collectors.toList());
	}

	private List<Library> languageFramework() {
		final List<Library> libraries = new ArrayList<>();
		final Module module = moduleDependencyOf(this.module, factoryLanguage.name$(), version);
		final Application app = ApplicationManager.getApplication();
		if (app.isDispatchThread()) app.runWriteAction(() -> addExternalLibraries(libraries, module));
		else
			app.invokeAndWait(() -> app.runWriteAction(() -> addExternalLibraries(libraries, module)), defaultModalityState());
		return libraries;
	}

	private void addExternalLibraries(List<Library> libraries, Module module) {
		if (module == null) addExternalLibraries(libraries);
		else addModuleDependency(module, libraries);
	}

	private void addModuleDependency(Module dependency, List<Library> libraries) {
		libraries.addAll(new LibraryManager(this.module).resolveAsModuleDependency(dependency));
		final Configuration configuration = TaraUtil.configurationOf(dependency);
		if (configuration != null) factoryLanguage.effectiveVersion(configuration.version());
	}

	private void addExternalLibraries(List<Library> libraries) {
		final LibraryManager manager = new LibraryManager(this.module);
		if (!LanguageManager.getLanguageFile(factoryLanguage.name$(), version).exists()) importLanguage();
		final Map<Artifact, DependencyScope> languageFramework = findLanguageFramework(languageID(factoryLanguage.name$(), version));
		libraries.addAll(flat(manager.registerOrGetLibrary(languageFramework)));
		factoryLanguage.effectiveVersion(!languageFramework.isEmpty() ? languageFramework.keySet().iterator().next().getVersion() : "");
		manager.addToModule(libraries, DependencyScope.COMPILE);
	}

	public static Module moduleDependencyOf(Module languageModule, String language, String version) {
		final List<Module> modules = Arrays.stream(ModuleManager.getInstance(languageModule.getProject()).getModules()).filter(m -> !m.equals(languageModule)).collect(Collectors.toList());
		for (Module m : modules) {
			final Configuration configuration = TaraUtil.configurationOf(m);
			if (configuration == null) continue;
			if (language.equalsIgnoreCase(configuration.artifactId())) return m;
		}
		return null;
	}

	private Map<Artifact, DependencyScope> findLanguageFramework(String languageId) {
		try {
			if (languageId == null) return Collections.emptyMap();
			return toMap(new Aether(collectRemotes(), localRepository).resolve(new DefaultArtifact(languageId), JavaScopes.COMPILE), DependencyScope.COMPILE);
		} catch (DependencyResolutionException e) {
			return Collections.emptyMap();
		}
	}

	private Map<Artifact, DependencyScope> toMap(List<Artifact> artifacts, DependencyScope scope) {
		Map<Artifact, DependencyScope> map = new LinkedHashMap<>();
		artifacts.forEach(a -> map.put(a, scope));
		return map;
	}

	private void resolveBuilder(List<Library> libraries) {
		final Library library = libraries.stream().filter(l -> l.getName() != null && l.getName().contains(Proteo.GROUP_ID + ":" + Proteo.ARTIFACT_ID + ":")).findFirst().orElse(null);
		if (library == null || library.getName() == null) return;
		try {
			final List<RemoteRepository> repos = new ArrayList<>();
			repos.add(new RemoteRepository("intino-maven", "default", "https://artifactory.intino.io/artifactory/releases"));
			repos.add(new RemoteRepository("maven-central", "default", "http://repo1.maven.org/maven2/"));
			String specificVersion = factoryLanguage.ownerAs(Factory.class).version();
			final String version = specificVersion != null && !specificVersion.isEmpty() ? specificVersion : mayorVersionOf(library);
			if (version == null) LOG.error("No version available");
			saveClassPath(new Aether(repos, localRepository).resolve(new DefaultArtifact("io.intino.tara:builder:" + version), JavaScopes.COMPILE));
		} catch (DependencyResolutionException e) {
			LOG.error(e.getMessage());
		}
	}

	private void saveClassPath(List<Artifact> classpath) {
		if (classpath.isEmpty()) return;
		final String home = System.getProperty("user.home");
		List<String> libraries = classpath.stream().map(c -> c.getFile().getAbsolutePath()).collect(Collectors.toList());
		libraries = libraries.stream().map(l -> l.replace(home, "$HOME")).collect(Collectors.toList());
		final File miscDirectory = LanguageManager.getMiscDirectory(this.module.getProject());
		final File file = new File(miscDirectory, "compiler.classpath");
		try {
			Files.write(file.toPath(), String.join(":", libraries).getBytes());
		} catch (IOException e) {
			LOG.error(e.getMessage());
		}
	}

	private String mayorVersionOf(Library library) {
		final String[] split = library.getName().split(":");
		final String version = split[split.length - 1].trim();
		final String mayor = version.split("\\.")[0];
		try {
			int next = Integer.parseInt(mayor) + 1;
			return "[" + mayor + ".0.0" + "," + next + ".0.0]";
		} catch (NumberFormatException e) {
			return null;
		}
	}

	public static String languageID(String language, String version) {
		if (isMagritteLibrary(language))
			return magritteID(version);
		final File languageFile = LanguageManager.getLanguageFile(language, version);
		if (!languageFile.exists()) return null;
		else try {
			Manifest manifest = new JarFile(languageFile).getManifest();
			final Attributes tara = manifest.getAttributes("tara");
			if (tara == null) return null;
			return tara.getValue("framework");
		} catch (IOException e) {
			LOG.error(e.getMessage(), e);
			return null;
		}
	}

	private void importLanguage() {
		new LanguageImporter(module, TaraUtil.configurationOf(module)).importLanguage(factoryLanguage.name$(), version);
	}

	@NotNull
	private List<RemoteRepository> collectRemotes() {
		List<RemoteRepository> remotes = new ArrayList<>();
		remotes.addAll(repositories.stream().map(this::remoteFrom).collect(Collectors.toList()));
		remotes.add(new RemoteRepository("maven-central", "default", "http://repo1.maven.org/maven2/"));
		return remotes;
	}

	private RemoteRepository remoteFrom(Repository remote) {
		return new RemoteRepository(remote.mavenId(), "default", remote.url()).setAuthentication(provideAuthentication(remote.mavenId()));
	}

	private Authentication provideAuthentication(String mavenId) {
		final IntinoSettings settings = IntinoSettings.getSafeInstance(module.getProject());
		for (ArtifactoryCredential credential : settings.artifactories())
			if (credential.serverId.equals(mavenId))
				return new Authentication(credential.username, credential.password);
		return null;
	}

	@NotNull
	private static String magritteID(String version) {
		return Proteo.GROUP_ID + ":" + Proteo.ARTIFACT_ID + ":" + version;
	}

	private static boolean isMagritteLibrary(String language) {
		return language.equals(Proteo.class.getSimpleName()) || language.equals(Verso.class.getSimpleName());
	}
}
