package io.intino.plugin.build;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleTypeWithWebFeatures;
import com.intellij.openapi.roots.CompilerModuleExtension;
import io.intino.Configuration;
import io.intino.plugin.IntinoException;
import io.intino.plugin.lang.psi.impl.IntinoUtil;
import io.intino.plugin.project.LegioConfiguration;
import io.intino.plugin.project.configuration.Version;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import static com.intellij.openapi.roots.ModuleRootManager.getInstance;
import static io.intino.plugin.MessageProvider.message;
import static io.intino.plugin.project.Safe.safe;

public class FactoryPhaseChecker {


	void check(FactoryPhase phase, Module module, Configuration configuration) throws IntinoException {
		if (!(configuration instanceof LegioConfiguration))
			throw new IntinoException(message("legio.artifact.not.found"));
		if (safe(() -> ((LegioConfiguration) configuration).artifact().packageConfiguration()) == null)
			throw new IntinoException(message("packaging.configuration.not.found"));
		if (noDistributionRepository(phase, configuration))
			throw new IntinoException(message("distribution.repository.not.found"));
		if (!webServiceIsCompile(module))
			throw new IntinoException(message("web.service.not.packaged"));
	}

	private boolean webServiceIsCompile(Module module) {
		for (Module dependency : collectModuleDependencies(module, new HashSet<>())) {
			if (ModuleTypeWithWebFeatures.isAvailable(dependency)) {
				final CompilerModuleExtension extension = CompilerModuleExtension.getInstance(dependency);
				if (extension == null || extension.getCompilerOutputUrl() == null ||
						!new File(pathOf(extension.getCompilerOutputUrl())).exists() ||
						new File(pathOf(extension.getCompilerOutputUrl())).list().length == 0) {
					return false;
				}
			}
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

	private Set<Module> collectModuleDependencies(Module module, Set<Module> collection) {
		for (Module dependant : getInstance(module).getModuleDependencies()) {
			if (!collection.contains(dependant)) collection.addAll(collectModuleDependencies(dependant, collection));
			collection.add(dependant);
		}
		return collection;
	}


	boolean shouldDistributeLanguage(Module module, FactoryPhase lifeCyclePhase) {
		Configuration configuration = IntinoUtil.configurationOf(module);
		if (!(configuration instanceof LegioConfiguration)) return false;
		if (configuration.repositories().stream().noneMatch(repository -> repository instanceof Configuration.Repository.Language))
			return false;
		Configuration.Artifact.Model model = safe(() -> configuration.artifact().model());
		return model != null && model.level() != null && !model.level().isSolution() && lifeCyclePhase.mavenActions().contains("deploy");
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
