package io.intino.plugin.project.configuration;

import com.intellij.execution.RunManager;
import com.intellij.execution.application.ApplicationConfiguration;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleTypeWithWebFeatures;
import com.intellij.openapi.progress.ProgressIndicator;
import io.intino.Configuration;
import io.intino.Configuration.Artifact.Dependency;
import io.intino.Configuration.Repository;
import io.intino.plugin.dependencyresolution.*;
import io.intino.plugin.project.ArtifactorySensor;
import io.intino.plugin.project.configuration.model.LegioRunConfiguration;
import io.intino.plugin.project.configuration.model.retrocompatibility.LegioModel;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static io.intino.Configuration.Artifact;
import static io.intino.Configuration.Artifact.Dsl.Builder.ExcludedPhases.ExcludeCodeBaseGeneration;
import static io.intino.Configuration.RunConfiguration;
import static io.intino.plugin.project.Safe.safe;
import static io.intino.plugin.project.Safe.safeList;
import static org.eclipse.aether.util.artifact.JavaScopes.COMPILE;

public class ConfigurationReloader {
	private final Configuration configuration;
	private final String updatePolicy;
	private final Artifact artifact;
	private final List<Artifact.Dsl> dsls;
	private final List<Repository> repositories;
	private final Module module;
	private ProgressIndicator indicator;

	public ConfigurationReloader(Module module, Configuration configuration, String updatePolicy) {
		this.module = module;
		this.configuration = configuration;
		this.updatePolicy = updatePolicy;
		this.artifact = configuration.artifact();
		this.repositories = this.configuration.repositories();
		this.dsls = safe(artifact::dsls);
		this.indicator = null;
	}

	public ConfigurationReloader(Module module, Configuration configuration, String updatePolicy, ProgressIndicator indicator) {
		this(module, configuration, updatePolicy);
		this.indicator = indicator;
	}

	void reloadDsls() {
		dsls.forEach(this::resolve);
	}

	void reloadRunConfigurations() {
		@SuppressWarnings("Convert2MethodRef") final List<RunConfiguration> runConfigurations = safeList(() -> configuration.runConfigurations());
		for (RunConfiguration runConfiguration : runConfigurations) {
			ApplicationConfiguration configuration = findRunConfiguration(runConfiguration.name());
			if (configuration != null)
				configuration.setProgramParameters(((LegioRunConfiguration) runConfiguration).argumentsChain());
		}
	}

	void reloadArtifactoriesMetaData() {
		ArtifactorySensor sensor = new ArtifactorySensor(repositories);
		for (Artifact.Dsl dsl : dsls) {
			sensor.update(dsl);

		}
	}

	void reloadDependencies() {
		if (configuration == null || configuration.artifact() == null) return;
		resolveJavaDependencies();
		resolveWebDependencies();
	}

	private void resolveJavaDependencies() {
		DependencyCatalog dependencies = new DependencyCatalog();
		List<Dependency> artifactDependencies = new ArrayList<>(artifact.dependencies());
		Dependency.DataHub datahub = artifact.datahub();
		if (datahub != null) artifactDependencies.add(0, datahub);
		Dependency.Archetype archetype = artifact.archetype();
		if (archetype != null) artifactDependencies.add(0, archetype);
		for (Artifact.Dsl dsl : dsls) {
			var deps = resolve(dsl);
			if (!dsl.builder().excludedPhases().contains(ExcludeCodeBaseGeneration)) {
				Artifact.Dsl.Runtime runtime = dsl.runtime();
				if (runtime != null)
					artifactDependencies.add(0, asDependency(String.join(":", runtime.groupId(), runtime.artifactId(), runtime.version()), COMPILE));
			}
			String language = language(deps);
			if (language != null && !dsl.level().isModel())
				artifactDependencies.add(0, asDependency(language, COMPILE));
			else if (dsl instanceof LegioModel)
				artifactDependencies.add(0, asDependency("io.intino.tara:language:LATEST", COMPILE));//FIXME done for retro-compatibility
		}
		ImportsResolver resolver = new ImportsResolver(module, updatePolicy, repositories, indicator);
		if (!artifactDependencies.isEmpty()) dependencies.merge(resolver.resolve(artifactDependencies));
		dependencies.merge(resolver.resolveWeb(webDependencies(artifactDependencies)));
		final Application application = ApplicationManager.getApplication();
		if (application.isWriteAccessAllowed()) register(dependencies);
		else application.invokeLater(() -> register(dependencies));
	}

	private static String language(List<org.eclipse.aether.graph.Dependency> deps) {
		return deps.stream().map(org.eclipse.aether.graph.Dependency::getArtifact)
				.filter(d -> d.getArtifactId().equalsIgnoreCase("language"))
				.findFirst().map(d -> String.join(":", d.getGroupId(), d.getArtifactId(), d.getVersion()))
				.orElse(null);
	}

	private List<org.eclipse.aether.graph.Dependency> resolve(Artifact.Dsl dsl) {
		return new LanguageResolver(module, repositories).resolve(dsl);
	}

	private void register(DependencyCatalog dependencies) {
		new ProjectLibrariesManager(module.getProject()).register(dependencies);
		new ModuleLibrariesManager(module).merge(dependencies);
		new UnusedLibrariesInspection(module.getProject()).cleanUp();
	}

	@NotNull
	private List<Dependency.Web> webDependencies(List<Dependency> artifactDependencies) {
		return artifactDependencies.stream().
				filter(d -> d instanceof Dependency.Web).
				map(d -> (Dependency.Web) d).
				collect(Collectors.toList());
	}

	private void resolveWebDependencies() {
		if (ModuleTypeWithWebFeatures.isAvailable(module))
			new WebDependencyResolver(module, artifact, repositories).resolve();
	}

	@NotNull
	private static Dependency.Compile asDependency(String coorsText, String scope) {
		String[] coors = coorsText.split(":");
		return new Dependency.Compile() {
			@Override
			public String groupId() {
				return coors[0];
			}

			@Override
			public String artifactId() {
				return coors[1];
			}

			@Override
			public String version() {
				return coors[2];
			}

			@Override
			public void version(String newVersion) {

			}

			@Override
			public String scope() {
				return scope;
			}

			@Override
			public List<Exclude> excludes() {
				return List.of();
			}

			@Override
			public String effectiveVersion() {
				return null;
			}

			@Override
			public void effectiveVersion(String version) {

			}

			@Override
			public boolean transitive() {
				return false;
			}

			@Override
			public boolean toModule() {
				return false;
			}

			@Override
			public void toModule(boolean toModule) {

			}

			@Override
			public Configuration root() {
				return null;
			}

			@Override
			public Configuration.ConfigurationNode owner() {
				return null;
			}
		};
	}

	private ApplicationConfiguration findRunConfiguration(String name) {
		final List<com.intellij.execution.configurations.RunConfiguration> list = RunManager.getInstance(module.getProject()).
				getAllConfigurationsList().stream().filter(r -> r instanceof ApplicationConfiguration).toList();
		return (ApplicationConfiguration) list.stream().filter(r -> (r.getName()).equalsIgnoreCase(artifact.name().toLowerCase() + "-" + name)).findFirst().orElse(null);
	}
}