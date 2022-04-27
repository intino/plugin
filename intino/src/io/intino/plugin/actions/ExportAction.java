package io.intino.plugin.actions;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.progress.PerformInBackgroundOption;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.progress.impl.BackgroundableProcessIndicator;
import io.intino.Configuration;
import io.intino.konos.compiler.shared.KonosBuildConstants;
import io.intino.plugin.IntinoException;
import io.intino.plugin.MessageProvider;
import io.intino.plugin.actions.box.KonosRunner;
import io.intino.plugin.actions.box.accessor.AccessorsPublisher;
import io.intino.plugin.build.FactoryPhase;
import io.intino.plugin.build.PluginExecutor;
import io.intino.plugin.lang.psi.impl.IntinoUtil;
import io.intino.plugin.project.configuration.LegioConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import static com.intellij.notification.NotificationType.ERROR;
import static io.intino.Configuration.Artifact.Plugin.Phase.Export;
import static io.intino.plugin.project.Safe.safeList;

public class ExportAction {
	private static final com.intellij.openapi.diagnostic.Logger Logger = com.intellij.openapi.diagnostic.Logger.getInstance(ExportAction.class);

	public void execute(Module module, FactoryPhase phase) {
		final Configuration configuration = IntinoUtil.configurationOf(module);
		if (!(configuration instanceof LegioConfiguration)) {
			Notifications.Bus.notify(new Notification("Intino",
					phase.gerund() + " exports", "Impossible identify module scope", NotificationType.ERROR));
			return;
		}
		ApplicationManager.getApplication().invokeAndWait(() -> FileDocumentManager.getInstance().saveAllDocuments());
		withTask(new Task.Backgroundable(module.getProject(), "Exporting accessors of " + module.getName(), true, PerformInBackgroundOption.ALWAYS_BACKGROUND) {
			@Override
			public void run(@NotNull ProgressIndicator indicator) {
				runBoxExports(phase, module, (LegioConfiguration) configuration, indicator);
				runPlugins(module, phase, (LegioConfiguration) configuration, indicator);
			}
		});
	}

	private void runBoxExports(FactoryPhase factoryPhase, Module module, LegioConfiguration configuration, ProgressIndicator indicator) {
		Configuration.Artifact.Box box = configuration.artifact().box();
		if (box != null) {
			final String version = box.version();
			if (version == null || version.isEmpty()) return;
			try {
				File temp = Files.createTempDirectory("konos_accessors").toFile();
				KonosRunner konosRunner = new KonosRunner(module, configuration, KonosBuildConstants.Mode.Accessors, temp.getAbsolutePath());
				konosRunner.runKonosCompiler();
				AccessorsPublisher publisher = new AccessorsPublisher(module, configuration, temp);
				if (factoryPhase == FactoryPhase.INSTALL) publisher.install();
				else publisher.publish();
			} catch (IOException e) {
				Logger.error(e);
			} catch (IntinoException e) {
				notifyError(e.getMessage(), module);
			}
		}
	}

	private void runPlugins(Module module, FactoryPhase factoryPhase, LegioConfiguration configuration, ProgressIndicator indicator) {
		List<Configuration.Artifact.Plugin> intinoPlugins = safeList(() -> configuration.artifact().plugins());
		intinoPlugins.stream().filter(i -> i.phase() == Export).forEach(plugin -> {
			List<String> errorMessages = new ArrayList<>();
			new PluginExecutor(module, factoryPhase, configuration, plugin.artifact(), plugin.pluginClass(), errorMessages, indicator).execute();
			if (!errorMessages.isEmpty())
				Notifications.Bus.notify(new Notification("Intino", MessageProvider.message("error.occurred", "export"), errorMessages.get(0), NotificationType.ERROR), module.getProject());
		});
	}

	private void withTask(Task.Backgroundable runnable) {
		ProgressManager.getInstance().runProcessWithProgressAsynchronously(runnable, new BackgroundableProcessIndicator(runnable));
	}


	private void notifyError(String message, Module module) {
		Notifications.Bus.notify(new Notification("Intino", "Elements cannot be generated. ", message, ERROR), module.getProject());
	}
}
