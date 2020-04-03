package io.intino.plugin.build;

import com.intellij.openapi.module.Module;
import io.intino.Configuration;
import io.intino.plugin.IntinoException;
import io.intino.plugin.lang.psi.impl.IntinoUtil;
import io.intino.plugin.project.LegioConfiguration;
import io.intino.plugin.project.configuration.Version;

import static io.intino.plugin.MessageProvider.message;
import static io.intino.plugin.project.Safe.safe;

public class FactoryPhaseChecker {


	void check(FactoryPhase phase, Configuration configuration) throws IntinoException {
		if (!(configuration instanceof LegioConfiguration))
			throw new IntinoException(message("legio.artifact.not.found"));
		if (safe(() -> ((LegioConfiguration) configuration).artifact().packageConfiguration()) == null)
			throw new IntinoException(message("packaging.configuration.not.found"));
		if (noDistributionRepository(phase, configuration))
			throw new IntinoException(message("distribution.repository.not.found"));
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
