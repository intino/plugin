package io.intino.plugin.actions;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications.Bus;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.progress.impl.BackgroundableProcessIndicator;
import io.intino.Configuration;
import io.intino.Configuration.Artifact.Plugin.Phase;
import io.intino.builder.BuildConstants;
import io.intino.plugin.IntinoException;
import io.intino.plugin.actions.export.DslExportRunner;
import io.intino.plugin.actions.export.accessor.ExportsPublisher;
import io.intino.plugin.build.FactoryPhase;
import io.intino.plugin.build.plugins.PluginExecutor;
import io.intino.plugin.lang.psi.impl.IntinoUtil;
import io.intino.plugin.project.configuration.ArtifactLegioConfiguration;
import io.intino.plugin.project.configuration.Version;
import io.intino.plugin.project.configuration.model.LegioDistribution;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import static com.intellij.notification.NotificationType.ERROR;
import static io.intino.plugin.project.Safe.safeList;

public class ExportAction {
	private static final com.intellij.openapi.diagnostic.Logger Logger = com.intellij.openapi.diagnostic.Logger.getInstance(ExportAction.class);

	public void execute(Module module, FactoryPhase phase) {
		final Configuration configuration = IntinoUtil.configurationOf(module);
		if (!(configuration instanceof ArtifactLegioConfiguration)) {
			Bus.notify(new Notification("Intino",
					phase.gerund() + " exports", "Impossible identify module scope", NotificationType.ERROR));
			return;
		}
		ApplicationManager.getApplication().invokeAndWait(() -> FileDocumentManager.getInstance().saveAllDocuments());
		withTask(new Task.Backgroundable(module.getProject(), "Exporting " + module.getName(), true) {
			@Override
			public void run(@NotNull ProgressIndicator indicator) {
				runDslExports(phase, module, (ArtifactLegioConfiguration) configuration, indicator);
				runPlugins(module, phase, (ArtifactLegioConfiguration) configuration, indicator);
			}
		});
	}

	private void runDslExports(FactoryPhase factoryPhase, Module module, ArtifactLegioConfiguration configuration, ProgressIndicator indicator) {
		if (factoryPhase == FactoryPhase.DISTRIBUTE && !hasDistribution(configuration)) {
			notifyError("Distribution repository not found", module);
			return;
		}
		for (Configuration.Artifact.Dsl dsl : configuration.artifact().dsls()) {
			final String version = dsl.version();
			if (version == null || version.isEmpty()) return;
			try {
				File temp = Files.createTempDirectory(dsl.name() + "_export").toFile();
				new DslExportRunner(module, configuration, dsl, BuildConstants.Mode.Export, factoryPhase, temp.getAbsolutePath(), indicator).runExport();
				ExportsPublisher publisher = new ExportsPublisher(module, configuration, temp);
				if (factoryPhase == FactoryPhase.INSTALL) publisher.install();
				else publisher.publish();
			} catch (IOException | InterruptedException e) {
				Logger.error(e);
			} catch (IntinoException e) {
				notifyError(e.getMessage(), module);
			}
		}
	}

	private boolean hasDistribution(ArtifactLegioConfiguration configuration) {
		LegioDistribution distribution = configuration.artifact().distribution();
		if (distribution == null) return false;
		try {
			if (new Version(configuration.artifact().version()).isSnapshot()) return distribution.snapshot() != null;
			return distribution.release() != null;
		} catch (IntinoException e) {
			return false;
		}
	}

	private void runPlugins(Module module, FactoryPhase factoryPhase, ArtifactLegioConfiguration configuration, ProgressIndicator indicator) {
		List<Configuration.Artifact.Plugin> intinoPlugins = safeList(() -> configuration.artifact().plugins());
		intinoPlugins.stream().filter(i -> i.phase() == Phase.Export).forEach(plugin -> new PluginExecutor(module, factoryPhase, configuration, plugin, indicator).execute());
	}

	private void withTask(Task.Backgroundable runnable) {
		ProgressManager.getInstance().runProcessWithProgressAsynchronously(runnable, new BackgroundableProcessIndicator(runnable));
	}


	private void notifyError(String message, Module module) {
		Bus.notify(new Notification("Intino", "Elements cannot be generated. ", message, ERROR), module.getProject());
	}
}
