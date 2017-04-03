package io.intino.plugin.settings;

import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import io.intino.plugin.IntinoIcons;
import io.intino.tara.plugin.lang.LanguageManager;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;


public class IntinoSettingsComponent implements ProjectComponent, Configurable {

	private static final String INTINO_CONTROL_PLUGIN_NAME = "Intino Plugin";
	private static final String INTINO_CONTROL_COMPONENT_NAME = "ArtifactComponent";

	private final io.intino.plugin.settings.IntinoSettings settings;
	private final Project project;
	private IntinoSettingsPanel intinoSettingsPanel;

	public IntinoSettingsComponent(Project project) {
		this.settings = IntinoSettings.getSafeInstance(project);
		this.project = project;
	}

	public void projectOpened() {
	}

	public void projectClosed() {
		LanguageManager.remove(project);
	}

	public JComponent createComponent() {
		if (intinoSettingsPanel == null) intinoSettingsPanel = new IntinoSettingsPanel();
		return intinoSettingsPanel.getRootPanel();
	}

	public boolean isModified() {
		return intinoSettingsPanel != null && intinoSettingsPanel.isModified(settings);
	}

	public void disposeUIResources() {
		intinoSettingsPanel = null;
	}

	public String getHelpTopic() {
		return null;
	}


	public void apply() throws ConfigurationException {
		if (intinoSettingsPanel != null) try {
			intinoSettingsPanel.applyConfigurationData(settings);
		} catch (Exception ex) {
			throw new ConfigurationException(ex.getMessage());
		}
	}

	@NotNull
	public String getComponentName() {
		return INTINO_CONTROL_COMPONENT_NAME;
	}


	@Nls
	public String getDisplayName() {
		return INTINO_CONTROL_PLUGIN_NAME;
	}


	public Icon getIcon() {
		return IntinoIcons.INTINO_16;
	}


	public void reset() {
		intinoSettingsPanel.loadConfigurationData(settings);
	}


	public void initComponent() {

	}

	public void disposeComponent() {

	}
}
