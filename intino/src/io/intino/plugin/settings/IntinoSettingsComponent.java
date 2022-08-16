package io.intino.plugin.settings;

import com.intellij.compiler.CompilerConfigurationImpl;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.util.messages.MessageBus;
import com.intellij.util.messages.MessageBusConnection;
import io.intino.plugin.IntinoIcons;
import io.intino.plugin.cesar.CesarServerInfoDownloader;
import io.intino.plugin.toolwindows.IntinoTopics;
import io.intino.plugin.toolwindows.remote.IntinoRemoteConsoleListener;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.Arrays;
import java.util.List;


public class IntinoSettingsComponent implements Configurable {

	private static final String INTINO_CONTROL_PLUGIN_NAME = "Intino Plugin";
	private static final String INTINO_CONTROL_COMPONENT_NAME = "ArtifactComponent";

	private final io.intino.plugin.settings.IntinoSettings settings;
	private final Project project;
	private IntinoSettingsPanel intinoSettingsPanel;

	public IntinoSettingsComponent(Project project) {
		this.settings = IntinoSettings.getInstance(project);
		this.project = project;
		CompilerConfigurationImpl instance = (CompilerConfigurationImpl) CompilerConfigurationImpl.getInstance(project);
		List<String> patterns = Arrays.asList(instance.getResourceFilePatterns());
		if (!patterns.contains("!?*.tara")) instance.addResourceFilePattern("!?*.tara");
		if (!patterns.contains("!?*.itr")) instance.addResourceFilePattern("!?*.itr");
		if (!patterns.contains("!?*.konos")) instance.addResourceFilePattern("!?*.konos");
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
			new CesarServerInfoDownloader().download(project);
			final MessageBus messageBus = project.getMessageBus();
			final IntinoRemoteConsoleListener mavenListener = messageBus.syncPublisher(IntinoTopics.REMOTE_CONSOLE);
			mavenListener.refresh();
			final MessageBusConnection connect = messageBus.connect();
			connect.deliverImmediately();
			connect.disconnect();
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

}
