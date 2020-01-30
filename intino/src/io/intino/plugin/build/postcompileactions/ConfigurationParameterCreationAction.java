package io.intino.plugin.build.postcompileactions;

import com.intellij.openapi.module.Module;
import io.intino.plugin.build.PostCompileAction;
import io.intino.plugin.lang.psi.impl.TaraUtil;
import io.intino.plugin.project.LegioConfiguration;
import io.intino.tara.compiler.shared.Configuration;

import java.util.List;


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
	public void execute() {
		Configuration configuration = TaraUtil.configurationOf(module);
		if (configuration.artifact().parameters().stream().noneMatch(p -> p.name().equals(name)))
			((LegioConfiguration) configuration.artifact()).artifact().addParameters(name);
	}
}
