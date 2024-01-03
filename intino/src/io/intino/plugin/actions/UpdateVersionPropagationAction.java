package io.intino.plugin.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import io.intino.Configuration;
import io.intino.plugin.lang.psi.impl.IntinoUtil;
import io.intino.plugin.project.configuration.ArtifactLegioConfiguration;
import io.intino.plugin.project.configuration.Version;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicBoolean;

import static io.intino.plugin.project.Safe.safe;

public class UpdateVersionPropagationAction extends UpdateVersionAction {
	private static final Logger logger = Logger.getInstance(UpdateVersionPropagationAction.class);

	@Override
	public void actionPerformed(@NotNull AnActionEvent e) {
		execute(e.getData(LangDataKeys.MODULE));
	}

	@Override
	public void execute(Module module) {
		Configuration configuration = IntinoUtil.configurationOf(module);
		if (!(configuration instanceof ArtifactLegioConfiguration)) return;
		Version.Level changeLevel = new ModuleDependencyPropagator(module, configuration).execute();
//		upgrade(module, configuration, changeLevel);
	}

	private void upgrade(Module module, Configuration configuration, Version.Level changeLevel) {
		if (changeLevel != null) {
			if (!safe(() -> configuration.artifact().packageConfiguration().isRunnable(), false)) {
				boolean ask = askForDistributeNewReleases(module.getProject());
				if (ask) {
					try {
						upgrade((ArtifactLegioConfiguration) configuration, changeLevel);
						distribute(module);
					} catch (Exception e) {
						logger.error(e);
					}
				}
			}
		}
	}

	private boolean askForDistributeNewReleases(Project project) {
		AtomicBoolean response = new AtomicBoolean(false);
		ApplicationManager.getApplication().invokeAndWait(() ->
				response.set(new IntinoConfirmationDialog(project,
						"Do you want to distribute new version of the updated module?",
						"Release Updated Module.").showAndGet()));
		return response.get();
	}

	@Override
	public void update(AnActionEvent e) {
		e.getPresentation().setEnabled(e.getData(LangDataKeys.MODULE) != null);
		e.getPresentation().setVisible(e.getData(LangDataKeys.MODULE) != null);
		super.update(e);
	}
}
