package org.siani.legio.plugin.project;

import org.jetbrains.annotations.NotNull;
import tara.intellij.project.configuration.ConfigurationManager;

public class LegioAppComponent implements com.intellij.openapi.components.ApplicationComponent {
	@Override
	public void initComponent() {
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
