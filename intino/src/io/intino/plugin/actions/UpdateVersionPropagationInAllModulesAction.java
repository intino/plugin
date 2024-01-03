package io.intino.plugin.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import io.intino.plugin.lang.psi.impl.IntinoUtil;
import io.intino.plugin.project.configuration.ArtifactLegioConfiguration;
import io.intino.plugin.project.configuration.Version;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static io.intino.plugin.project.Safe.safe;

public class UpdateVersionPropagationInAllModulesAction extends UpdateVersionAction {
	@Override
	public void actionPerformed(@NotNull AnActionEvent e) {
		execute(e.getData(LangDataKeys.PROJECT));
	}

	public void execute(Project project) {
		Map<ArtifactLegioConfiguration, Version.Level> evolvedConfigurations = new AllModuleDependencyPropagator(Arrays.asList(ModuleManager.getInstance(project).getModules())).execute();
		Map<ArtifactLegioConfiguration, Version.Level> configurations = evolvedConfigurations.entrySet().stream().
				filter(c -> !isRunnable(c.getKey()) && hasDistribution(c.getKey())).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
		if (configurations.isEmpty()) return;
//		upgrade(project, configurations);
	}

	private void upgrade(Project project, Map<ArtifactLegioConfiguration, Version.Level> configurations) {
		boolean ask = askForDistributeNewReleases(project);
		if (ask) {
			for (Map.Entry<ArtifactLegioConfiguration, Version.Level> e : configurations.entrySet()) {
				try {
					if (!safe(() -> e.getKey().artifact().packageConfiguration().isRunnable(), false)) {
						upgrade(e.getKey(), e.getValue());
						distribute(e.getKey().module());
					}
				} catch (Exception ignored) {
				}
			}
		}
	}

	private boolean hasDistribution(ArtifactLegioConfiguration c) {
		return safe(() -> c.artifact().distribution().release()) != null;
	}

	private Boolean isRunnable(ArtifactLegioConfiguration c) {
		return safe(() -> c.artifact().packageConfiguration().isRunnable());
	}

	@Override
	public void execute(Module module) {

	}

	@Override
	public void update(AnActionEvent e) {
		final Project project = e.getProject();
		boolean visible = project != null && hasLegioModules(project);
		e.getPresentation().setEnabled(visible);
		e.getPresentation().setVisible(visible);
		super.update(e);
	}

	private boolean hasLegioModules(Project project) {
		return Arrays.stream(ModuleManager.getInstance(project).getModules()).
				anyMatch(module -> IntinoUtil.configurationOf(module) instanceof ArtifactLegioConfiguration);
	}

	private boolean askForDistributeNewReleases(Project project) {
		AtomicBoolean response = new AtomicBoolean(false);
		ApplicationManager.getApplication().invokeAndWait(() -> response.set(new IntinoConfirmationDialog(project,
				"Do you want to distribute new version of the updated modules? Only *Non Runnable* artifacts will be distributed",
				"Release Updated Modules.").showAndGet()));
		return response.get();
	}
}
