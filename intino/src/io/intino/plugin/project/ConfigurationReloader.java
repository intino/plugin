package io.intino.plugin.project;

import com.intellij.execution.RunManager;
import com.intellij.execution.application.ApplicationConfiguration;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleTypeWithWebFeatures;
import io.intino.legio.graph.Artifact;
import io.intino.legio.graph.Artifact.Level.Model;
import io.intino.legio.graph.LegioGraph;
import io.intino.legio.graph.Repository;
import io.intino.legio.graph.RunConfiguration;
import io.intino.plugin.dependencyresolution.*;
import io.intino.plugin.project.builders.InterfaceBuilderManager;
import io.intino.plugin.project.run.IntinoRunConfiguration;
import io.intino.tara.plugin.lang.LanguageManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static io.intino.plugin.project.LegioConfiguration.parametersOf;
import static io.intino.plugin.project.Safe.safe;
import static io.intino.plugin.project.Safe.safeList;

public class ConfigurationReloader {
	private final DependencyAuditor auditor;
	private final LegioGraph graph;
	private final String updatePolicy;
	private Module module;

	public ConfigurationReloader(Module module, DependencyAuditor auditor, LegioGraph graph, String updatePolicy) {
		this.module = module;
		this.auditor = auditor;
		this.graph = graph;
		this.updatePolicy = updatePolicy;
	}

	void reloadInterfaceBuilder() {
		final Artifact.Box box = safe(() -> graph.artifact().box());
		if (box != null) box.effectiveVersion(new InterfaceBuilderManager().reload(module.getProject(), box.version()));
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
		resolveWebDependencies();
	}

	void reloadLanguage() {
		Model model = safe(() -> graph.artifact().asLevel().model());
		if (model == null) return;
		final String effectiveVersion = model.effectiveVersion();
		String version = effectiveVersion == null || effectiveVersion.isEmpty() ? model.version() : effectiveVersion;
		LanguageManager.silentReload(this.module.getProject(), model.language(), version);
	}

	private void resolveJavaDependencies() {
		DependencyCatalog dependencies = resolveLanguage();
		if (!safeList(() -> graph.artifact().imports().dependencyList()).isEmpty())
			dependencies.merge(new ImportsResolver(module, auditor, updatePolicy, repositories()).resolve(graph.artifact().imports().dependencyList()));
		if (!safeList(() -> graph.artifact().imports().webList()).isEmpty()) {
			dependencies.merge(new ImportsResolver(module, auditor, updatePolicy, repositories()).resolveWeb(graph.artifact().imports().webList()));
		}
		new DependencyConflictResolver().resolve(dependencies);
		new ProjectLibrariesManager(module.getProject()).register(dependencies);
		new ModuleLibrariesManager(module).merge(dependencies);
		new UnusedLibrariesInspection(module.getProject()).cleanUp();
	}

	private void resolveWebDependencies() {
		if (ModuleTypeWithWebFeatures.isAvailable(module) && graph.artifact().webImports() != null)
			new WebDependencyResolver(module, graph.artifact(), repositories()).resolve();
	}

	private DependencyCatalog resolveLanguage() {
		Model model = safe(() -> graph.artifact().asLevel().model());
		if (model == null) return new DependencyCatalog();
		final String effectiveVersion = model.effectiveVersion();
		String version = effectiveVersion == null || effectiveVersion.isEmpty() ? model.version() : effectiveVersion;
		return new LanguageResolver(module, auditor, model, version, repositories()).resolve();
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
