package io.intino.plugin.project;

import org.jetbrains.annotations.NotNull;
import tara.intellij.lang.LanguageManager;
import tara.intellij.project.configuration.ConfigurationManager;

public class LegioAppComponent implements com.intellij.openapi.components.ApplicationComponent {
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