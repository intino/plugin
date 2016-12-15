package io.intino.plugin.dependencyresolution;

import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.roots.libraries.Library;
import com.jcabi.aether.Aether;
import io.intino.legio.Project;
import io.intino.legio.Project.Repositories.Repository;
import io.intino.plugin.project.LegioConfiguration;
import org.jetbrains.annotations.NotNull;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.repository.RemoteRepository;
import org.sonatype.aether.resolution.DependencyResolutionException;
import org.sonatype.aether.util.artifact.DefaultArtifact;
import org.sonatype.aether.util.artifact.JavaScopes;
import tara.compiler.shared.Configuration;
import tara.intellij.lang.LanguageManager;
import tara.intellij.lang.psi.impl.TaraUtil;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.stream.Collectors;

import static tara.dsl.ProteoConstants.PROTEO;
import static tara.dsl.ProteoConstants.VERSO;

public class LanguageResolver {
	private final Module module;
	private final List<Repository> repositories;
	private final Project.Factory factory;
	private final String version;
	private static final String proteoGroupId = "org.siani.tara";
	private static final String proteoArtifactId = "proteo";
	private final String language;

	public LanguageResolver(Module module, List<Repository> repositories, Project.Factory factory, String version) {
		this.module = module;
		this.repositories = repositories;
		this.factory = factory;
		this.language = LegioConfiguration.safe(() -> factory.asLevel().language());
		this.version = version;
	}

	public List<Library> resolve() {
		if (language == null) return Collections.emptyList();
		LanguageManager.silentReload(this.module.getProject(), language, version);
		final List<Library> libraries = new ArrayList<>();
		if (language.equals(PROTEO) || language.equals(VERSO))
			libraries.addAll(proteoFramework(version));
		else libraries.addAll(frameworkOfLanguage());
		return libraries;
	}

	private List<Library> proteoFramework(String version) {
		List<Library> libraries = new ArrayList<>();
		final Application application = ApplicationManager.getApplication();
		if (application.isWriteAccessAllowed())
			application.runWriteAction(() -> loadProteoLibrary(version, libraries));
		else application.invokeLater(() -> application.runWriteAction(() -> loadProteoLibrary(version, libraries)));
		return libraries;
	}

	private void loadProteoLibrary(String version, List<Library> libraries) {
		final LibraryManager manager = new LibraryManager(module);
		final List<Artifact> languageFramework = findLanguageFramework(proteoGroupId + ":" + proteoArtifactId + ":" + version);
		if (!languageFramework.isEmpty())
			factory.asLevel().effectiveVersion(languageFramework.get(0).getVersion());
		else factory.asLevel().effectiveVersion("");
		libraries.addAll(manager.registerOrGetLibrary(languageFramework));
		manager.addToModule(libraries, false);
	}

	private List<Library> frameworkOfLanguage() {
		final List<Library> libraries = new ArrayList<>();
		final Module module = moduleOf(this.module, language, version);
		final Application app = ApplicationManager.getApplication();
		if (app.isDispatchThread()) app.runWriteAction(() -> addExternalLibraries(libraries, module));
		else app.invokeLater(() -> app.runWriteAction(() -> addExternalLibraries(libraries, module)));
		return libraries;
	}

	private void addExternalLibraries(List<Library> libraries, Module module) {
		if (module == null) addExternalLibraries(libraries);
		else addModuleDependency(module, libraries);
	}

	private void addModuleDependency(Module dependency, List<Library> libraries) {
		libraries.addAll(new LibraryManager(this.module).resolveAsModuleDependency(dependency));
	}

	private void addExternalLibraries(List<Library> libraries) {
		final LibraryManager manager = new LibraryManager(this.module);
		if (!LanguageManager.getLanguageFile(language, version).exists()) importLanguage();
		final List<Artifact> languageFramework = findLanguageFramework();
		libraries.addAll(manager.registerOrGetLibrary(languageFramework));
		if (!languageFramework.isEmpty()) factory.asLevel().effectiveVersion(languageFramework.get(0).getVersion());
		else factory.asLevel().effectiveVersion("");
		manager.addToModule(libraries, false);
	}

	public static Module moduleOf(Module languageModule, String language, String version) {
		final List<Module> modules = Arrays.stream(ModuleManager.getInstance(languageModule.getProject()).getModules()).filter(m -> !m.equals(languageModule)).collect(Collectors.toList());
		for (Module m : modules) {
			final Configuration configuration = TaraUtil.configurationOf(m);
			if (configuration == null) continue;
			if (language.equalsIgnoreCase(configuration.artifactId())) return m;
		}
		return null;
	}

	public List<Artifact> findLanguageFramework() {
		final String languageId = languageID(this.language, this.version);
		return findLanguageFramework(languageId);
	}

	private List<Artifact> findLanguageFramework(String languageId) {
		try {
			if (languageId == null) return Collections.emptyList();
			File local = new File(System.getProperty("user.home") + File.separator + ".m2" + File.separator + "repository");
			return new Aether(collectRemotes(), local).resolve(new DefaultArtifact(languageId), JavaScopes.COMPILE);
		} catch (DependencyResolutionException ignored) {
			return Collections.emptyList();
		}
	}


	public static String languageID(String language, String version) {
		if (language.equals(PROTEO) || language.equals(VERSO))
			return proteoGroupId + ":" + proteoArtifactId + ":" + version;
		final File languageFile = LanguageManager.getLanguageFile(language, version);
		if (!languageFile.exists()) return null;
		else try {
			Manifest manifest = new JarFile(languageFile).getManifest();
			final Attributes tara = manifest.getAttributes("tara");
			if (tara == null) return null;
			return tara.getValue("framework");
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	private void importLanguage() {
		new LanguageImporter(module, TaraUtil.configurationOf(module)).importLanguage(factory.asLevel().language(), version);
	}

	@NotNull
	private Collection<RemoteRepository> collectRemotes() {
		Collection<RemoteRepository> remotes = new ArrayList<>();
		remotes.add(new RemoteRepository("maven-central", "default", "http://repo1.maven.org/maven2/"));
		remotes.addAll(repositories.stream().map(remote -> new RemoteRepository(remote.name(), "default", remote.url())).collect(Collectors.toList()));
		return remotes;
	}
}
