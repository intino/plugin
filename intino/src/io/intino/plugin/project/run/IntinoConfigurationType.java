package io.intino.plugin.project.run;

import com.intellij.execution.application.ApplicationConfigurationType;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.ConfigurationType;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.openapi.project.Project;
import io.intino.plugin.IntinoIcons;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class IntinoConfigurationType extends ApplicationConfigurationType {

	private final IntinoConfigurationFactory icf = new IntinoConfigurationFactory(this);

	@Override
	public String getDisplayName() {
		return "Intino";
	}

	@Override
	public String getConfigurationTypeDescription() {
		return "Runs an Intino project";
	}

	@Override
	public Icon getIcon() {
		return IntinoIcons.INTINO_16;
	}

	@NotNull
	@Override
	public String getId() {
		return "intino.configuration.type";
	}

	public ConfigurationFactory[] getConfigurationFactories() {
		return new ConfigurationFactory[]{icf};
	}

	public static class IntinoConfigurationFactory extends ConfigurationFactory {

		IntinoConfigurationFactory(@NotNull ConfigurationType type) {
			super(type);
		}

		@NotNull
		@Override
		public RunConfiguration createTemplateConfiguration(@NotNull Project project) {
			return new IntinoRunConfiguration("Intino Configuration", project, this);
		}
	}
}
