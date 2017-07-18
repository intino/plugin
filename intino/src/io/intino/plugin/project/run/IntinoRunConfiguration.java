package io.intino.plugin.project.run;

import com.intellij.execution.application.ApplicationConfiguration;
import com.intellij.execution.application.ApplicationConfigurationType;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.openapi.project.Project;

/**
 * Created by oroncal on 7/7/17.
 */
public class IntinoRunConfiguration extends ApplicationConfiguration {

	public IntinoRunConfiguration(String name, Project project, ApplicationConfigurationType applicationConfigurationType) {
		super(name, project, applicationConfigurationType);
	}

	public IntinoRunConfiguration(String name, Project project, ConfigurationFactory factory) {
		super(name, project, factory);
	}
}
