package io.intino.plugin.project.run;

import com.intellij.execution.application.ApplicationConfiguration;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.openapi.project.Project;

public class IntinoRunConfiguration extends ApplicationConfiguration {

	public IntinoRunConfiguration(String name, Project project, ConfigurationFactory factory) {
		super(name, project, factory);
	}
}
