package io.intino.plugin.actions;

import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.progress.PerformInBackgroundOption;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.progress.impl.BackgroundableProcessIndicator;
import io.intino.legio.graph.Artifact;
import io.intino.plugin.build.FactoryPhase;
import io.intino.plugin.build.PluginExecutor;
import io.intino.plugin.project.LegioConfiguration;
import io.intino.tara.compiler.shared.Configuration;
import io.intino.tara.plugin.lang.psi.impl.TaraUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static io.intino.plugin.DataContext.getContext;
import static io.intino.plugin.project.Safe.safeList;

public class ExportAction {

	public void execute(Module module, FactoryPhase factoryPhase) {
		final Configuration configuration = TaraUtil.configurationOf(module);
		if (configuration == null) return;
		runBoxExports(factoryPhase, configuration);
		runExportPlugins(module, factoryPhase, (LegioConfiguration) configuration);
	}

	private void runBoxExports(FactoryPhase factoryPhase, Configuration configuration) {
		if (configuration.box() != null) {
			final String version = configuration.box().version();
			if (version != null && !version.isEmpty()) {
				AnAction action = ActionManager.getInstance().getAction((factoryPhase.equals(FactoryPhase.INSTALL) ? "Install" : "Publish") + "Accessors" + version);
				if (action != null) action.actionPerformed(createActionEvent());
			}
		}
	}

	private void runExportPlugins(Module module, FactoryPhase factoryPhase, LegioConfiguration configuration) {
		List<Artifact.IntinoPlugin> intinoPlugins = safeList(() -> configuration.graph().artifact().intinoPluginList());
		intinoPlugins.stream().filter(i -> i.phase() == Artifact.IntinoPlugin.Phase.Export).forEach(plugin -> {
			withTask(new Task.Backgroundable(module.getProject(), "Exports plugins of " + module.getName(), true, PerformInBackgroundOption.ALWAYS_BACKGROUND) {
				@Override
				public void run(@NotNull ProgressIndicator indicator) {
					List<String> errorMessages = new ArrayList<>();
					new PluginExecutor(module, factoryPhase, configuration, plugin.artifact(), plugin.pluginClass(), errorMessages, indicator).execute();
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
