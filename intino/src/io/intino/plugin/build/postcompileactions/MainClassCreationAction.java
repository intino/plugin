package io.intino.plugin.build.postcompileactions;

import com.intellij.openapi.module.Module;
import io.intino.Configuration;
import io.intino.plugin.build.PostCompileAction;
import io.intino.plugin.lang.psi.impl.IntinoUtil;
import io.intino.plugin.project.configuration.ArtifactLegioConfiguration;
import io.intino.plugin.project.configuration.model.LegioArtifact;

import java.util.List;

import static io.intino.plugin.build.PostCompileAction.FinishStatus.NothingDone;
import static io.intino.plugin.build.PostCompileAction.FinishStatus.RequiresReload;
import static io.intino.plugin.project.Safe.safe;


public class MainClassCreationAction extends PostCompileAction {
	private final String qualifiedName;

	public MainClassCreationAction(Module module, List<String> parameters) {
		this(module, parameters.get(1));
	}

	public MainClassCreationAction(Module module, String qualifiedName) {
		super(module);
		this.qualifiedName = qualifiedName;
	}

	@Override
	public FinishStatus execute() {
		if (qualifiedName == null) return NothingDone;
		Configuration configuration = IntinoUtil.configurationOf(module);
		if (!(configuration instanceof ArtifactLegioConfiguration)) return NothingDone;
		if (safe(() -> configuration.artifact().packageConfiguration().mainClass()) == null) {
			((LegioArtifact) configuration.artifact()).packageConfiguration().mainClass(qualifiedName);
			return RequiresReload;
		}
		return NothingDone;
	}
}
