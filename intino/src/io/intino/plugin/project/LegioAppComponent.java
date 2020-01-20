package io.intino.plugin.project;

import io.intino.plugin.lang.LanguageManager;
import io.intino.plugin.project.configuration.ConfigurationManager;
import org.jetbrains.annotations.NotNull;

public class LegioAppComponent implements com.intellij.openapi.components.BaseComponent {
	@Override
	public void initComponent() {
		LanguageManager.register(new tara.dsl.Legio());
		ConfigurationManager.registerProvider(LegioConfiguration.class);
	}

	@Override
	public void disposeComponent() {
		ConfigurationManager.unregisterProvider(LegioConfiguration.class);
	}

	@NotNull
	@Override
	public String getComponentName() {
		return "ConfigurationRegisterer";
	}
}
