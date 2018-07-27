package io.intino.plugin.project;

import com.intellij.execution.RunManager;
import com.intellij.execution.application.ApplicationConfiguration;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.WebModuleType;
import com.intellij.openapi.roots.libraries.Library;
import io.intino.legio.graph.Artifact;
import io.intino.legio.graph.LegioGraph;
import io.intino.legio.graph.Repository;
import io.intino.legio.graph.RunConfiguration;
import io.intino.legio.graph.level.LevelArtifact;
import io.intino.plugin.dependencyresolution.JavaDependencyResolver;
import io.intino.plugin.dependencyresolution.LanguageResolver;
import io.intino.plugin.dependencyresolution.LibraryManager;
import io.intino.plugin.dependencyresolution.WebDependencyResolver;
import io.intino.plugin.project.builders.InterfaceBuilderManager;
import io.intino.plugin.project.run.IntinoRunConfiguration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static io.intino.plugin.project.LegioConfiguration.parametersOf;
import static io.intino.plugin.project.LibraryConflictResolver.libraryOf;
import static io.intino.plugin.project.LibraryConflictResolver.mustAdd;
import static io.intino.plugin.project.Safe.safe;
import static io.intino.plugin.project.Safe.safeList;

public class ConfigurationReloader {
	private Module module;
	private final LegioGraph graph;

	public ConfigurationReloader(Module module, LegioGraph graph) {
		this.module = module;
		this.graph = graph;
	}

	void reloadInterfaceBuilder() {
		final Artifact.Box boxing = safe(() -> graph.artifact().box());
		if (boxing != null) new InterfaceBuilderManager().reload(module.getProject(), boxing.sdk());
	}

	void reloadRunConfigurations() {
		@SuppressWarnings("Convert2MethodRef") final List<RunConfiguration> runConfigurations = safeList(() -> graph.runConfigurationList());
		for (RunConfiguration runConfiguration : runConfigurations) {
			ApplicationConfiguration configuration = findRunConfiguration(runConfiguration.name$());
			if (configuration != null) configuration.setProgramParameters(parametersOf(runConfiguration));
		}
	}

	void reloadArtifactoriesMetaData() {
		new ArtifactorySensor(repositories()).update();
	}

	void reloadDependencies() {
		if (graph == null || graph.artifact() == null) return;
		resolveJavaDependencies();
		if (WebModuleType.isWebModule(module) && graph.artifact().webImports() != null)
			new WebDependencyResolver(module, graph.artifact(), repositories()).resolve();
	}

	private void resolveJavaDependencies() {
		if (safeList(() -> graph.artifact().imports().dependencyList()) == null) return;
		final JavaDependencyResolver resolver = new JavaDependencyResolver(module, repositories(), safeList(() -> graph.artifact().imports().dependencyList()));
		final List<Library> newLibraries = resolver.resolve();
		final List<Library> languageLibraries = resolveLanguages();
		for (Library languageLibrary : languageLibraries)
			if (mustAdd(languageLibrary, newLibraries)) replace(newLibraries, languageLibrary);
		LibraryManager.clean(module, newLibraries);
	}

	private void replace(List<Library> newLibraries, Library languageLibrary) {
		newLibraries.remove(libraryOf(newLibraries, languageLibrary.getName()));
		newLibraries.add(languageLibrary);
	}

	List<Library> resolveLanguages() {
		List<Library> libraries = new ArrayList<>();
		LevelArtifact.Model model = safe(() -> graph.artifact().asLevel().model());
		if (model == null) return libraries;
		final String effectiveVersion = model.effectiveVersion();
		String version = effectiveVersion == null || effectiveVersion.isEmpty() ? model.version() : effectiveVersion;
		libraries.addAll(new LanguageResolver(module, repositories(), model, version).resolve());
		return libraries;
	}

	private ApplicationConfiguration findRunConfiguration(String name) {
		final List<com.intellij.execution.configurations.RunConfiguration> list = RunManager.getInstance(module.getProject()).
				getAllConfigurationsList().stream().filter(r -> r instanceof IntinoRunConfiguration).collect(Collectors.toList());
		return (ApplicationConfiguration) list.stream().filter(r -> (r.getName()).equalsIgnoreCase(graph.artifact().name$().toLowerCase() + "-" + name)).findFirst().orElse(null);
	}

	public List<Repository.Type> repositories() {
		List<Repository.Type> repos = new ArrayList<>();
		if (graph == null) return Collections.emptyList();
		safeList(graph::repositoryList).stream().map(Repository::typeList).forEach(repos::addAll);
		return repos;
	}


}
