package io.intino.plugin.build.postcompileactions;

import com.intellij.openapi.module.Module;
import io.intino.Configuration;
import io.intino.plugin.build.PostCompileAction;
import io.intino.plugin.lang.psi.impl.TaraUtil;
import io.intino.plugin.project.configuration.model.LegioArtifact;

import java.util.List;

import static io.intino.plugin.build.PostCompileAction.FinishStatus.NothingDone;
import static io.intino.plugin.build.PostCompileAction.FinishStatus.RequiresReload;


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
		Configuration configuration = TaraUtil.configurationOf(module);
		if (configuration.artifact().parameters().stream().noneMatch(p -> p.name().equals(name))) {
			((LegioArtifact) configuration.artifact()).addParameters(name);
			return RequiresReload;
		}
		return NothingDone;
	}
}
