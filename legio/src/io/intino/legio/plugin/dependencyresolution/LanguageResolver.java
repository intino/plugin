package io.intino.legio.plugin.dependencyresolution;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.roots.libraries.Library;
import com.jcabi.aether.Aether;
import io.intino.legio.Project;
import io.intino.legio.Project.Repositories.Repository;
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

	public LanguageResolver(Module module, List<Repository> repositories, Project.Factory factory, String version) {
		this.module = module;
		this.repositories = repositories;
		this.factory = factory;
		this.version = version;
	}

	public List<Library> resolve() {
		final String language = factory.modeling().language();
		LanguageManager.reloadLanguage(this.module.getProject(), language, version);
		final List<Library> libraries = new ArrayList<>();
		if (language.equals(PROTEO) || language.equals(VERSO))
			libraries.addAll(proteoFramework(version));
		else libraries.addAll(frameworkOfLanguage(language, version));
		return libraries;
	}

	private List<Library> proteoFramework(String version) {
		List<Library> libraries = new ArrayList<>();
		ApplicationManager.getApplication().runWriteAction(() -> {
			final LibraryManager manager = new LibraryManager(module);
			final String proteoID = proteoGroupId + ":" + proteoArtifactId + ":" + version;
			libraries.addAll(manager.registerOrGetLibrary(findLanguageFramework(proteoID)));
			manager.addToModule(libraries, false);
		});
		return libraries;
	}

	private List<Library> frameworkOfLanguage(String language, String version) {
		List<Library> libraries = new ArrayList<>();
		ApplicationManager.getApplication().runWriteAction(() -> {
			final Module module = moduleOf(language, version);
			if (module == null) addExternalLibraries(language, version, libraries);
			else addModuleDependency(module, libraries);
		});
		return libraries;
	}

	private void addModuleDependency(Module dependency, List<Library> libraries) {
		final LibraryManager manager = new LibraryManager(this.module);
		libraries.addAll(manager.resolveAsModuleDependency(dependency));
	}

	private void addExternalLibraries(String language, String version, List<Library> libraries) {
		final LibraryManager manager = new LibraryManager(this.module);
		libraries.addAll(manager.registerOrGetLibrary(findLanguageFramework(findLanguageId(language, version))));
		manager.addToModule(libraries, false);
	}

	private Module moduleOf(String language, String version) {
		final List<Module> modules = Arrays.stream(ModuleManager.getInstance(module.getProject()).getModules()).filter(m -> !m.equals(this.module)).collect(Collectors.toList());
		for (Module m : modules) {
			final Configuration configuration = TaraUtil.configurationOf(m);
			if (configuration == null) continue;
			if (language.equalsIgnoreCase(configuration.artifactId())) return m;
		}
		return null;
	}


	private List<Artifact> findLanguageFramework(String dependencyID) {
		try {
			if (dependencyID == null) return Collections.emptyList();
			File local = new File(System.getProperty("user.home") + File.separator + ".m2" + File.separator + "repository");
			return new Aether(collectRemotes(), local).resolve(new DefaultArtifact(dependencyID), JavaScopes.COMPILE);
		} catch (DependencyResolutionException ignored) {
			return Collections.emptyList();
		}
	}

	public static String findLanguageId(String language, String version) {
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

	@NotNull
	private Collection<RemoteRepository> collectRemotes() {
		Collection<RemoteRepository> remotes = new ArrayList<>();
		remotes.add(new RemoteRepository("maven-central", "default", "http://repo1.maven.org/maven2/"));
		remotes.addAll(repositories.stream().map(remote -> new RemoteRepository(remote.name(), "default", remote.url())).collect(Collectors.toList()));
		return remotes;
	}
}
