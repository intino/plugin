package io.intino.plugin.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import io.intino.plugin.lang.psi.impl.IntinoUtil;
import io.intino.plugin.project.LegioConfiguration;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public class UpdateVersionPropagationInAllModulesAction extends IntinoAction implements DumbAware {
	@Override
	public void actionPerformed(@NotNull AnActionEvent e) {
		execute(e.getData(LangDataKeys.PROJECT));
	}

	public void execute(Project project) {
		new AllModuleDependencyPropagator(Arrays.asList(ModuleManager.getInstance(project).getModules())).execute();
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
				anyMatch(module -> IntinoUtil.configurationOf(module) instanceof LegioConfiguration);
	}
}
