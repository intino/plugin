package org.siani.legio.plugin.errorreporting;

import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.openapi.diagnostic.ErrorReportSubmitter;
import com.intellij.openapi.diagnostic.IdeaLoggingEvent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.diagnostic.SubmittedReportInfo;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.ui.Messages;
import com.intellij.util.Consumer;
import org.jetbrains.annotations.NotNull;
import tara.intellij.diagnostic.errorreporting.PluginErrorReportSubmitterBundle;

import java.awt.*;
import java.io.IOException;

import static com.intellij.openapi.diagnostic.SubmittedReportInfo.SubmissionStatus.FAILED;

public class PluginErrorReportSubmitter extends ErrorReportSubmitter {
	private static final Logger LOG = Logger.getInstance(PluginErrorReportSubmitter.class.getName());
	private static final String TOKEN = "xoxb-64358527573-8ZxA5dbk7lcOxP2uOlr8ADmo";

	@Override
	public String getReportActionText() {
		return PluginErrorReportSubmitterBundle.message("report.error.to.plugin.vendor");
	}


	@Override
	public boolean submit(@NotNull IdeaLoggingEvent[] events, String additionalInfo, @NotNull Component parentComponent, @NotNull Consumer<SubmittedReportInfo> consumer) {
		final Exception[] ex = new Exception[]{null};
		Runnable runnable = createRunnable(events, additionalInfo, ex);
		ProgressManager progressManager = ProgressManager.getInstance();
		progressManager.runProcessWithProgressSynchronously(runnable, PluginErrorReportSubmitterBundle.message("progress.dialog.title"), true, null);
		if (processExceptions(parentComponent, ex[0]))
			consumer.consume(new SubmittedReportInfo(null, null, FAILED));
		LOG.info("Error submission successful");
		Messages.showInfoMessage(parentComponent, PluginErrorReportSubmitterBundle.message("successful.dialog.message"), PluginErrorReportSubmitterBundle.message("successful.dialog.title"));
		consumer.consume(new SubmittedReportInfo(null, null, FAILED));
		return true;
	}

	private Runnable createRunnable(IdeaLoggingEvent[] events, String additionalInfo, Exception[] exception) {
		return () -> {
			ProgressIndicator indicator = ProgressManager.getInstance().getProgressIndicator();
			indicator.setText(PluginErrorReportSubmitterBundle.message("progress.dialog.text"));
			indicator.setIndeterminate(true);
			IdeaPluginDescriptor pluginDescriptor = (IdeaPluginDescriptor) getPluginDescriptor();
			try {
				Slack slack = new Slack(TOKEN);
				slack.sendMessageToAChannel("pandora", "Error in plugin v." + pluginDescriptor.getVersion(), processEvents(events) + "\n" + additionalInfo);
				slack.disconnect();
			} catch (IOException e) {
				exception[0] = e;
			}
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

	private boolean processExceptions(Component parentComponent, Exception e) {
		if (e != null) {
			LOG.info("Error submission failed", e);
			Messages.showErrorDialog(parentComponent, e.getMessage(), PluginErrorReportSubmitterBundle.message("error.dialog.title"));
			return true;
		}
		return false;
	}
}
