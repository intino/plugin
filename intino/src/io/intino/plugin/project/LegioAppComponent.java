package io.intino.plugin.project;

import org.jetbrains.annotations.NotNull;
import io.intino.tara.plugin.lang.LanguageManager;
import io.intino.tara.plugin.project.configuration.ConfigurationManager;

public class LegioAppComponent implements com.intellij.openapi.components.ApplicationComponent {
	@Override
	public void initComponent() {
		LanguageManager.register(new tara.dsl.Legio());
		LanguageManager.register(new tara.dsl.Cesar());
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
