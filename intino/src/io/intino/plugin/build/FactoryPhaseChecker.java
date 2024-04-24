package io.intino.plugin.build;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleTypeWithWebFeatures;
import com.intellij.openapi.roots.CompilerModuleExtension;
import io.intino.Configuration;
import io.intino.plugin.IntinoException;
import io.intino.plugin.lang.psi.impl.IntinoUtil;
import io.intino.plugin.project.configuration.ArtifactLegioConfiguration;
import io.intino.plugin.project.configuration.Version;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import static com.intellij.openapi.roots.ModuleRootManager.getInstance;
import static io.intino.plugin.MessageProvider.message;
import static io.intino.plugin.project.Safe.safe;

public class FactoryPhaseChecker {
	void check(FactoryPhase phase, Configuration configuration) throws IntinoException {
		if (!(configuration instanceof ArtifactLegioConfiguration))
			throw new IntinoException(message("legio.artifact.not.found"));
		if (safe(() -> ((ArtifactLegioConfiguration) configuration).artifact().packageConfiguration()) == null)
			throw new IntinoException(message("packaging.configuration.not.found"));
		if (noDistributionRepository(phase, configuration))
			throw new IntinoException(message("distribution.repository.not.found"));
	}

	public boolean webServiceIsCompiled(Module module) {
		for (Module dependency : collectModuleDependencies(module, new HashSet<>())) {
			if (!ModuleTypeWithWebFeatures.isAvailable(dependency)) continue;
			final CompilerModuleExtension extension = CompilerModuleExtension.getInstance(dependency);
			if (extension == null || extension.getCompilerOutputUrl() == null ||
					!new File(pathOf(extension.getCompilerOutputUrl())).exists() ||
					Objects.requireNonNull(new File(pathOf(extension.getCompilerOutputUrl())).list((dir, name) -> !name.startsWith("."))).length == 0)
				return false;
		}
		return true;
	}

	private String pathOf(String path) {
		if (path.startsWith("file://")) return path.substring("file://".length());
		try {
			return new URL(path).getFile();
		} catch (MalformedURLException e) {
			return path;
		}
	}

	public static Set<Module> collectModuleDependencies(Module module, Set<Module> collection) {
		for (Module dependant : getInstance(module).getModuleDependencies()) {
			if (!collection.contains(dependant)) collection.addAll(collectModuleDependencies(dependant, collection));
			collection.add(dependant);
		}
		return collection;
	}


	boolean shouldDistributeLanguage(FactoryPhase lifeCyclePhase, Module module, Configuration.Artifact.Dsl dsl) {
		Configuration configuration = IntinoUtil.configurationOf(module);
		if (!(configuration instanceof ArtifactLegioConfiguration) || noDistributionRepository(lifeCyclePhase, configuration))
			return false;
		Configuration.Distribution distribution = safe(() -> configuration.artifact().distribution());
		if (distribution != null && !distribution.distributeLanguage()) return false;
		return dsl.outputDsl() != null && lifeCyclePhase.mavenActions().contains("deploy");
	}

	private boolean noDistributionRepository(FactoryPhase lifeCyclePhase, Configuration configuration) {
		try {
			return lifeCyclePhase.mavenActions().contains("deploy") && repositoryExists(configuration);
		} catch (IntinoException e) {
			return false;
		}
	}

	private boolean repositoryExists(Configuration configuration) throws IntinoException {
		Version version = new Version(configuration.artifact().version());
		if (version.isSnapshot()) return safe(() -> configuration.artifact().distribution().snapshot()) == null;
		return safe(() -> configuration.artifact().distribution().release()) == null;
	}


}
