package io.intino.plugin.build.postcompileactions;

import com.intellij.openapi.module.Module;
import io.intino.Configuration;
import io.intino.plugin.build.PostCompileAction;
import io.intino.plugin.lang.psi.impl.IntinoUtil;
import io.intino.plugin.project.LegioConfiguration;
import io.intino.plugin.project.configuration.model.LegioArtifact;

import java.util.List;

import static io.intino.plugin.build.PostCompileAction.FinishStatus.NothingDone;
import static io.intino.plugin.build.PostCompileAction.FinishStatus.RequiresReload;
import static io.intino.plugin.project.Safe.safeList;


public class ConfigurationParameterCreationAction extends PostCompileAction {
	String name;

	public ConfigurationParameterCreationAction(Module module, List<String> parameters) {
		this(module, parameters.get(1));
	}

	public ConfigurationParameterCreationAction(Module module, String name) {
		super(module);
		this.name = name;
	}

	@Override
	public FinishStatus execute() {
		if (name == null) return NothingDone;
		Configuration configuration = IntinoUtil.configurationOf(module);
		if (!(configuration instanceof LegioConfiguration)) return NothingDone;
		if (safeList(() -> configuration.artifact().parameters()).stream().noneMatch(p -> name.equals(p.name()))) {
			((LegioArtifact) configuration.artifact()).addParameters(name);
			return RequiresReload;
		}
		return NothingDone;
	}
}
