package io.intino.plugin.actions;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.DumbAware;
import io.intino.plugin.IntinoException;
import io.intino.plugin.build.ArtifactFactory;
import io.intino.plugin.build.FactoryPhase;
import io.intino.plugin.project.configuration.LegioConfiguration;
import io.intino.plugin.project.configuration.Version;

import static io.intino.plugin.build.AbstractArtifactFactory.ProcessResult.Retry;

public abstract class UpdateVersionAction extends IntinoAction implements DumbAware {
	protected void upgrade(LegioConfiguration configuration, Version.Level level) throws IntinoException {
		Version version = new Version(configuration.artifact().version());
		Version nextVersion = version.isSnapshot() ? version : version.nextRelease(level);
		configuration.artifact().version(nextVersion.toString());
	}

	protected void distribute(Module module) {
		ArtifactFactory artifactFactory = new ArtifactFactory(module, FactoryPhase.DISTRIBUTE);
		artifactFactory.build(result -> {
			if (result.equals(Retry)) artifactFactory.build(null);
		});
	}
}
