package io.intino.plugin.actions;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.progress.PerformInBackgroundOption;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.progress.impl.BackgroundableProcessIndicator;
import io.intino.Configuration;
import io.intino.plugin.MessageProvider;
import io.intino.plugin.build.FactoryPhase;
import io.intino.plugin.build.PluginExecutor;
import io.intino.plugin.lang.psi.impl.IntinoUtil;
import io.intino.plugin.project.LegioConfiguration;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static io.intino.plugin.DataContext.getContext;
import static io.intino.plugin.project.Safe.safeList;

public class ExportAction {

	public void execute(Module module, FactoryPhase phase) {
		final Configuration configuration = IntinoUtil.configurationOf(module);
		if (configuration == null) {
			Notifications.Bus.notify(new Notification("Tara Language",
					phase.gerund() + " exports", "Impossible identify module scope", NotificationType.ERROR));
			return;
		}
		runBoxExports(phase, configuration);
		runExportPlugins(module, phase, (LegioConfiguration) configuration);
	}

	private void runBoxExports(FactoryPhase factoryPhase, Configuration configuration) {
		Configuration.Artifact.Box box = configuration.artifact().box();
		if (box != null) {
			final String version = box.version();
			if (version != null && !version.isEmpty()) {
				AnAction action = ActionManager.getInstance().getAction((factoryPhase.equals(FactoryPhase.INSTALL) ? "Install" : "Publish") + "Accessors" + version);
				if (action != null) action.actionPerformed(createActionEvent());
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
