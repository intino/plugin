package io.intino.plugin.errorreporting;

import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.openapi.application.ApplicationInfo;
import com.intellij.openapi.diagnostic.ErrorReportSubmitter;
import com.intellij.openapi.diagnostic.IdeaLoggingEvent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.diagnostic.SubmittedReportInfo;
import com.intellij.openapi.diagnostic.SubmittedReportInfo.SubmissionStatus;
import com.intellij.openapi.extensions.PluginDescriptor;
import com.intellij.openapi.extensions.PluginId;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.Consumer;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PluginErrorReportSubmitter extends ErrorReportSubmitter {
	private static final Logger LOG = Logger.getInstance(PluginErrorReportSubmitter.class.getName());
	private static final String ERROR_SUBMITTER_PROPERTIES_PATH = "messages/errorReporter.properties";
	private static final String PLUGIN_ID_PROPERTY_KEY = "plugin.id";
	private static final String IDE_VERSION = "ide.version";
	private static final String IDE_NAME = "ide.name";
	private static final String PLUGIN_NAME_PROPERTY_KEY = "plugin.name";
	private static final String PLUGIN_VERSION_PROPERTY_KEY = "plugin.version";
	private static final String REPORT_ADDITIONAL_INFO = "report.additionalInfo";
	private static final String REPORT_DESCRIPTION = "report.description";
	private static final String REPORT_TITLE = "report.title";
	private static final String PROJECT = "1022010";
	private static final String TOKEN = "ae3d1e4d4bcb011927e2768d7aa39f3a";


	@NonNls
	public String getReportActionText() {
		return PluginErrorReportSubmitterBundle.message("report.error.to.plugin.vendor");
	}

	@Override
	public boolean submit(@NotNull IdeaLoggingEvent[] events, String additionalInfo, @NotNull Component parentComponent, @NotNull Consumer<? super SubmittedReportInfo> consumer) {
		PluginDescriptor plugin = getPluginDescriptor();
		final Properties reportingProperties = createErrorProperties(plugin, null, processEvents(events), additionalInfo);
		LOG.debug("Properties read from plugin descriptor: " + reportingProperties);
		queryPropertiesFile(plugin, reportingProperties);
		LOG.debug("Final properties to be applied: " + reportingProperties);
		ProgressManager.getInstance().runProcessWithProgressSynchronously(getRunnable(reportingProperties), PluginErrorReportSubmitterBundle.message("progress.dialog.title"), false, null);
		LOG.info("Error submission successful");
		Messages.showInfoMessage(parentComponent, PluginErrorReportSubmitterBundle.message("successful.dialog.message"), PluginErrorReportSubmitterBundle.message("successful.dialog.title"));
		consumer.consume(new SubmittedReportInfo(null, null, SubmissionStatus.FAILED));
		return true;
	}

	private Runnable getRunnable(final Properties reportingProperties) {
		return () -> {
			ProgressIndicator indicator = ProgressManager.getInstance().getProgressIndicator();
			indicator.setText(PluginErrorReportSubmitterBundle.message("progress.dialog.text"));
			indicator.setIndeterminate(true);
			PivotalLoggingEventSubmitter submitter = new PivotalLoggingEventSubmitter(reportingProperties, PROJECT, TOKEN);
			submitter.submit();
		};
	}

	private String processEvents(IdeaLoggingEvent[] events) {
		StringBuilder stream = new StringBuilder();
		for (IdeaLoggingEvent event : events) {
			stream.append(event.getMessage() != null ? event.getMessage() : "");
			stream.append(event.getThrowableText() != null ? event.getThrowableText() : "");
		}
		return stream.toString();
	}

	private Properties createErrorProperties(@NotNull PluginDescriptor descriptor, String title, String description, String additionalInfo) {
		Properties properties = new Properties();
		PluginId descPluginId = descriptor.getPluginId();
		properties.put(IDE_NAME, ApplicationInfo.getInstance().getFullApplicationName());
		properties.put(IDE_VERSION, ApplicationInfo.getInstance().getMajorVersion() + "." + ApplicationInfo.getInstance().getMinorVersion());
		if (!StringUtil.isEmptyOrSpaces(descPluginId.getIdString()))
			properties.put(PLUGIN_ID_PROPERTY_KEY, descPluginId.getIdString().trim());
		if (descriptor instanceof IdeaPluginDescriptor ideaPluginDescriptor) {
			if (!StringUtil.isEmptyOrSpaces(ideaPluginDescriptor.getName()))
				properties.put(PLUGIN_NAME_PROPERTY_KEY, ideaPluginDescriptor.getName().trim());
			String descVersion = ideaPluginDescriptor.getVersion();
			if (!StringUtil.isEmptyOrSpaces(descVersion))
				properties.put(PLUGIN_VERSION_PROPERTY_KEY, descVersion.trim());
			if (description != null) properties.put(REPORT_DESCRIPTION, description);
			if (title != null) properties.put(REPORT_TITLE, title);
			if (additionalInfo != null) properties.put(REPORT_ADDITIONAL_INFO, additionalInfo);
		}
		return properties;
	}

	private void queryPropertiesFile(@NotNull PluginDescriptor pluginDescriptor, @NotNull Properties properties) {
		ClassLoader loader = pluginDescriptor.getPluginClassLoader();
		if (loader == null) return;
		try (InputStream stream = loader.getResourceAsStream(ERROR_SUBMITTER_PROPERTIES_PATH)) {
			if (stream != null) {
				LOG.debug("Reading ErrorReporter.properties from file system: " + ERROR_SUBMITTER_PROPERTIES_PATH);
				try {
					properties.load(stream);
				} catch (Exception e) {
					LOG.info("Could not read in ErrorReporter.properties from file system", e);
				}
			}
		} catch (IOException e) {
			LOG.error(e);
		}
	}


}
