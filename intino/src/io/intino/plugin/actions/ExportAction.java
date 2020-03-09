package io.intino.plugin.actions;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionPlaces;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
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
import io.intino.plugin.MessageProvider;
import io.intino.plugin.actions.box.KonosRunner;
import io.intino.plugin.actions.box.accessor.AccessorsPublisher;
import io.intino.plugin.build.FactoryPhase;
import io.intino.plugin.build.PluginExecutor;
import io.intino.plugin.lang.psi.impl.IntinoUtil;
import io.intino.plugin.project.LegioConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static io.intino.plugin.DataContext.getContext;
import static io.intino.plugin.project.Safe.safeList;

public class ExportAction {
	private static final com.intellij.openapi.diagnostic.Logger Logger = com.intellij.openapi.diagnostic.Logger.getInstance(ExportAction.class);

	public void execute(Module module, FactoryPhase phase) {
		final Configuration configuration = IntinoUtil.configurationOf(module);
		if (!(configuration instanceof LegioConfiguration)) {
			Notifications.Bus.notify(new Notification("Tara Language",
					phase.gerund() + " exports", "Impossible identify module scope", NotificationType.ERROR));
			return;
		}
		runBoxExports(phase, module, (LegioConfiguration) configuration);
		runExportPlugins(module, phase, (LegioConfiguration) configuration);
	}

	private void runBoxExports(FactoryPhase factoryPhase, Module module, LegioConfiguration configuration) {
		Configuration.Artifact.Box box = configuration.artifact().box();
		if (box != null) {
			final String version = box.version();
			if (version != null && !version.isEmpty()) {
				ApplicationManager.getApplication().invokeAndWait(() -> FileDocumentManager.getInstance().saveAllDocuments());
				try {
					Path temp = Files.createTempDirectory("konos_accessors");
					KonosRunner konosRunner = new KonosRunner(module, configuration, KonosBuildConstants.Mode.Accessors, temp.toFile().getAbsolutePath());
					konosRunner.runKonosCompiler();
					AccessorsPublisher publisher = new AccessorsPublisher(module, configuration, temp.toFile());
					if (factoryPhase == FactoryPhase.INSTALL) publisher.install();
					else publisher.publish();
				} catch (IOException e) {
					Logger.error(e);
				}
			}
		}
	}

	private void runExportPlugins(Module module, FactoryPhase factoryPhase, LegioConfiguration configuration) {
		List<Configuration.Artifact.Plugin> intinoPlugins = safeList(() -> configuration.artifact().plugins());
		intinoPlugins.stream().filter(i -> i.phase() == Configuration.Artifact.Plugin.Phase.Export).forEach(plugin -> {
			withTask(new Task.Backgroundable(module.getProject(), "Exports plugins of " + module.getName(), true, PerformInBackgroundOption.ALWAYS_BACKGROUND) {
				@Override
				public void run(@NotNull ProgressIndicator indicator) {
					List<String> errorMessages = new ArrayList<>();
					new PluginExecutor(module, factoryPhase, configuration, plugin.artifact(), plugin.pluginClass(), errorMessages, indicator).execute();
					if (!errorMessages.isEmpty())
						Notifications.Bus.notify(new Notification("Tara Language", MessageProvider.message("error.occurred", "export"), errorMessages.get(0), NotificationType.ERROR), module.getProject());
				}
			});
		});
	}

	private void withTask(Task.Backgroundable runnable) {
		ProgressManager.getInstance().runProcessWithProgressAsynchronously(runnable, new BackgroundableProcessIndicator(runnable));
	}

	private AnActionEvent createActionEvent() {
		return new AnActionEvent(null, getContext(),
				ActionPlaces.UNKNOWN, new Presentation(),
				ActionManager.getInstance(), 0);
	}
}
