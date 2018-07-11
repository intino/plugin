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
import io.intino.legio.graph.Repository;
import io.intino.legio.graph.level.LevelArtifact;
import io.intino.plugin.project.builders.ModelBuilderManager;
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
import org.sonatype.aether.repository.RepositoryPolicy;
import org.sonatype.aether.resolution.DependencyResolutionException;
import org.sonatype.aether.util.artifact.DefaultArtifact;
import org.sonatype.aether.util.artifact.JavaScopes;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.stream.Collectors;

import static com.intellij.openapi.application.ModalityState.defaultModalityState;

public class LanguageResolver {
	private static final Logger LOG = Logger.getInstance(LanguageResolver.class);

	private final Module module;
	private final List<Repository.Type> repositories;
	private final String version;
	private final LevelArtifact.Model model;
	private File localRepository = new File(System.getProperty("user.home") + File.separator + ".m2" + File.separator + "repository");

	public LanguageResolver(Module module, List<Repository.Type> repositories, LevelArtifact.Model model, String version) {
		this.module = module;
		this.repositories = repositories;
		this.model = model;
		this.version = version;
	}

	public List<Library> resolve() {
		if (model == null) return Collections.emptyList();
		LanguageManager.silentReload(this.module.getProject(), model.language(), version);
		final List<Library> libraries = isMagritteLibrary(this.model.language()) ? magritte(version) : languageFramework();
		new ModelBuilderManager(module.getProject(), model).resolveBuilder();
		return libraries;
	}

	private List<Library> magritte(String version) {
		List<Library> libraries = new ArrayList<>();
		final Application application = ApplicationManager.getApplication();
		if (application.isWriteAccessAllowed())
			libraries.addAll(application.runWriteAction((Computable<List<Library>>) () -> loadMagritteLibrary(version, libraries)));
		else
			application.invokeAndWait(() -> libraries.addAll(application.runWriteAction((Computable<List<Library>>) () ->
					loadMagritteLibrary(version, libraries))), defaultModalityState());
		return libraries;
	}

	private List<Library> loadMagritteLibrary(String version, List<Library> libraries) {
		final LibraryManager manager = new LibraryManager(module);
		final Map<Artifact, DependencyScope> framework = findLanguageFramework(magritteID(version));
		if (framework.isEmpty()) return libraries;
		model.effectiveVersion(framework.keySet().iterator().next().getVersion());
		final Map<DependencyScope, List<Library>> registeredLibraries = manager.registerOrGetLibrary(framework, sources(framework.keySet().iterator().next()));
		libraries.addAll(flat(registeredLibraries));
		manager.addToModule(libraries, DependencyScope.COMPILE);
		return libraries;
	}

	private List<Library> flat(Map<DependencyScope, List<Library>> registeredLibraries) {
		return registeredLibraries.values().stream().flatMap(Collection::stream).collect(Collectors.toList());
	}

	private List<Library> languageFramework() {
		final List<Library> libraries = new ArrayList<>();
		final Module module = moduleDependencyOf(this.module, model.language(), version);
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
		if (configuration != null) model.effectiveVersion(configuration.version());
	}

	private void addExternalLibraries(List<Library> libraries) {
		final LibraryManager manager = new LibraryManager(this.module);
		if (!LanguageManager.getLanguageFile(model.language(), version).exists()) importLanguage();
		final Map<Artifact, DependencyScope> framework = findLanguageFramework(languageID(model.language(), version));
		if (!framework.isEmpty())
			libraries.addAll(flat(manager.registerOrGetLibrary(framework, sources(framework.keySet().iterator().next()))));
		model.effectiveVersion(!framework.isEmpty() ? framework.keySet().iterator().next().getVersion() : "");
		manager.addToModule(libraries, DependencyScope.COMPILE);
	}

	private Map<Artifact, Artifact> sources(Artifact artifact) {
		return Collections.singletonMap(artifact, sourcesOf(artifact));
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

	private Artifact sourcesOf(Artifact artifact) {
		try {
			return new Aether(collectRemotes(), localRepository).
					resolve(new DefaultArtifact(artifact.getGroupId(), artifact.getArtifactId(), "sources", "jar", artifact.getVersion()), JavaScopes.COMPILE).get(0);
		} catch (DependencyResolutionException e) {
			return null;
		}
	}

	private Map<Artifact, DependencyScope> toMap(List<Artifact> artifacts, DependencyScope scope) {
		Map<Artifact, DependencyScope> map = new LinkedHashMap<>();
		artifacts.forEach(a -> map.put(a, scope));
		return map;
	}


	public static String languageID(String language, String version) {
		if (isMagritteLibrary(language)) return magritteID(version);
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
		new LanguageImporter(module, TaraUtil.configurationOf(module)).importLanguage(model.language(), version);
	}

	@NotNull
	private List<RemoteRepository> collectRemotes() {
		List<RemoteRepository> remotes = new ArrayList<>();
		remotes.addAll(repositories.stream().map(this::remoteFrom).filter(Objects::nonNull).collect(Collectors.toList()));
		remotes.add(new RemoteRepository("maven-central", "default", "http://repo1.maven.org/maven2/").
				setPolicy(false, new RepositoryPolicy().setEnabled(true).setUpdatePolicy("always")));
		return remotes;
	}

	private RemoteRepository remoteFrom(Repository.Type remote) {
		if (remote.core$().variables().get("mavenID").get(0) == null) return null;
		return new RemoteRepository(remote.mavenID(), "default", remote.url()).
				setAuthentication(provideAuthentication(remote.mavenID())).
				setPolicy(false, new RepositoryPolicy().setEnabled(true).setUpdatePolicy("always"));
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
