package io.intino.plugin.actions;

import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.ide.plugins.PluginManagerCore;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationInfo;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.extensions.PluginDescriptor;
import com.intellij.openapi.extensions.PluginId;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.text.StringUtil;
import io.intino.plugin.IntinoIcons;
import io.intino.plugin.actions.dialog.SubmitFeedbackDialogPane;
import io.intino.plugin.errorreporting.PivotalLoggingEventSubmitter;
import io.intino.plugin.errorreporting.PluginErrorReportSubmitterBundle;
import io.intino.plugin.settings.IntinoSettings;
import org.jetbrains.annotations.NotNull;

import java.util.Properties;

public class SubmitFeedbackAction extends AnAction implements DumbAware {
	public static final Logger LOG = Logger.getInstance("Config module Action");
	private static final String PLUGIN_ID_PROPERTY_KEY = "plugin.id";
	public static final String PLUGIN_NAME_PROPERTY_KEY = "plugin.name";
	public static final String PLUGIN_VERSION_PROPERTY_KEY = "plugin.version";
	public static final String IDE_VERSION_PROPERTY_KEY = "ide.version";
	private static final String REPORT_DESCRIPTION = "report.description";
	private static final String REPORT_TITLE = "report.title";
	private static final String REPORT_TYPE = "report.type";

	@Override
	public void update(@NotNull AnActionEvent e) {
		e.getPresentation().setVisible(true);
		e.getPresentation().setEnabled(true);
		e.getPresentation().setIcon(IntinoIcons.MODEL_16);
	}

	@Override
	public void actionPerformed(@NotNull AnActionEvent e) {
		if (e.getProject() == null) {
			LOG.error("actionPerformed no project for " + e);
			return;
		}
		SubmitFeedbackDialogPane configDialog = new SubmitFeedbackDialogPane(e.getProject());
		configDialog.show();
		if (configDialog.getExitCode() == DialogWrapper.OK_EXIT_CODE) {
			sendReport(e.getProject(), configDialog.getReportTitle(), configDialog.getReportDescription(), configDialog.getReportType());
			Messages.showInfoMessage(e.getProject(), PluginErrorReportSubmitterBundle.message("successful.dialog.message"), PluginErrorReportSubmitterBundle.message("successful.dialog.title"));
		}
	}

	private void sendReport(Project project, String reportTitle, String reportDescription, String type) {
		IdeaPluginDescriptor plugin = PluginManagerCore.getPlugin(PluginId.getId("io.intino.plugin"));
		final Properties properties = createErrorProperties(plugin, reportTitle, reportDescription, type);
		final IntinoSettings settings = IntinoSettings.getInstance(project);
		PivotalLoggingEventSubmitter submitter = new PivotalLoggingEventSubmitter(properties, settings.trackerProjectId(), settings.trackerApiToken());
		submitter.submit();
	}

	private Properties createErrorProperties(PluginDescriptor descriptor, String title, String description, String type) {
		Properties properties = new Properties();
		PluginId descPluginId = descriptor.getPluginId();
		properties.put(IDE_VERSION_PROPERTY_KEY, ApplicationInfo.getInstance().getMajorVersion() + "." + ApplicationInfo.getInstance().getMinorVersion());
		if (descPluginId != null && !StringUtil.isEmptyOrSpaces(descPluginId.getIdString()))
			properties.put(PLUGIN_ID_PROPERTY_KEY, descPluginId.getIdString());
		if (descriptor instanceof IdeaPluginDescriptor) {
			IdeaPluginDescriptor ideaPluginDescriptor = (IdeaPluginDescriptor) descriptor;
			if (!StringUtil.isEmptyOrSpaces(ideaPluginDescriptor.getName()))
				properties.put(PLUGIN_NAME_PROPERTY_KEY, ideaPluginDescriptor.getName());
			String descVersion = ideaPluginDescriptor.getVersion();
			if (!StringUtil.isEmptyOrSpaces(descVersion))
				properties.put(PLUGIN_VERSION_PROPERTY_KEY, descVersion);
			if (description != null)
				properties.put(REPORT_DESCRIPTION, description);
			if (title != null)
				properties.put(REPORT_TITLE, title);
			if (type != null)
				properties.put(REPORT_TYPE, type);
		}
		return properties;
	}
}
