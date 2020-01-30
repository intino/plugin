package io.intino.plugin.project;

import com.intellij.execution.RunManager;
import com.intellij.execution.application.ApplicationConfiguration;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleTypeWithWebFeatures;
import io.intino.plugin.dependencyresolution.*;
import io.intino.plugin.lang.LanguageManager;
import io.intino.plugin.project.builders.InterfaceBuilderManager;
import io.intino.plugin.project.run.IntinoRunConfiguration;
import io.intino.tara.compiler.shared.Configuration;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static io.intino.plugin.project.Safe.safe;
import static io.intino.plugin.project.Safe.safeList;
import static io.intino.tara.compiler.shared.Configuration.Artifact;
import static io.intino.tara.compiler.shared.Configuration.RunConfiguration;

public class ConfigurationReloader {
	private final DependencyAuditor auditor;
	private final Configuration configuration;
	private final String updatePolicy;
	private final Artifact artifact;
	private final Artifact.Model model;
	private Module module;
	private final List<Configuration.Repository> repositories;

	public ConfigurationReloader(Module module, DependencyAuditor auditor, Configuration configuration, String updatePolicy) {
		this.module = module;
		this.auditor = auditor;
		this.configuration = configuration;
		this.updatePolicy = updatePolicy;
		this.artifact = configuration.artifact();
		this.repositories = this.configuration.repositories();
		this.model = safe(artifact::model);
	}

	void reloadInterfaceBuilder() {
		final Artifact.Box box = safe(artifact::box);
		if (box != null && box.version() != null)
			box.effectiveVersion(new InterfaceBuilderManager().download(module.getProject(), box.version()));
	}

	void reloadRunConfigurations() {
		@SuppressWarnings("Convert2MethodRef") final List<RunConfiguration> runConfigurations = safeList(() -> configuration.runConfigurations());
		for (RunConfiguration runConfiguration : runConfigurations) {
			ApplicationConfiguration configuration = findRunConfiguration(runConfiguration.name());
			if (configuration != null)
				configuration.setProgramParameters(runConfiguration.
						arguments().stream().map(r -> r.name() + "=" + r.value()).
						collect(Collectors.joining(" ")));
		}
	}

	void reloadArtifactoriesMetaData() {
		new ArtifactorySensor(configuration.repositories()).update();
	}

	void reloadDependencies() {
		if (configuration == null || configuration.artifact() == null) return;
		resolveJavaDependencies();
		resolveWebDependencies();
	}

	void reloadLanguage() {
		Artifact.Model.Language language = safe(() -> configuration.artifact().model().language());
		if (language == null || language.name() == null) return;
		final String effectiveVersion = language.effectiveVersion();
		String version = effectiveVersion == null || effectiveVersion.isEmpty() ? language.version() : effectiveVersion;
		LanguageManager.silentReload(this.module.getProject(), language.name(), version);
	}

	private void resolveJavaDependencies() {
		DependencyCatalog dependencies = resolveLanguage();
		List<Artifact.Dependency> artifactDependencies = new ArrayList<>(artifact.dependencies());
		artifactDependencies.add(artifact.datahub());
		if (!artifactDependencies.isEmpty())
			dependencies.merge(new ImportsResolver(module, auditor, updatePolicy, repositories).resolve(artifactDependencies));
		dependencies.merge(new ImportsResolver(module, auditor, updatePolicy, repositories).
				resolveWeb(webDependencies(artifactDependencies)));
		new DependencyConflictResolver().resolve(dependencies);
		new ProjectLibrariesManager(module.getProject()).register(dependencies);
		new ModuleLibrariesManager(module).merge(dependencies);
		new UnusedLibrariesInspection(module.getProject()).cleanUp();
	}

	@NotNull
	private List<Artifact.Dependency.Web> webDependencies(List<Artifact.Dependency> artifactDependencies) {
		return artifactDependencies.stream().
				filter(d -> d instanceof Artifact.Dependency.Web).
				map(d -> (Artifact.Dependency.Web) d).
				collect(Collectors.toList());
	}

	private void resolveWebDependencies() {
		if (ModuleTypeWithWebFeatures.isAvailable(module))
			new WebDependencyResolver(module, artifact, repositories).resolve();
	}

	private DependencyCatalog resolveLanguage() {
		Artifact.Model.Language language = model.language();
		if (language.name() == null) return new DependencyCatalog();
		final String effectiveVersion = language.effectiveVersion();
		String version = effectiveVersion == null || effectiveVersion.isEmpty() ? language.version() : effectiveVersion;
		return new LanguageResolver(module, auditor, model, version, repositories).resolve();
	}

	private ApplicationConfiguration findRunConfiguration(String name) {
		final List<com.intellij.execution.configurations.RunConfiguration> list = RunManager.getInstance(module.getProject()).
				getAllConfigurationsList().stream().filter(r -> r instanceof IntinoRunConfiguration).collect(Collectors.toList());
		return (ApplicationConfiguration) list.stream().filter(r -> (r.getName()).equalsIgnoreCase(artifact.name().toLowerCase() + "-" + name)).findFirst().orElse(null);
	}
}