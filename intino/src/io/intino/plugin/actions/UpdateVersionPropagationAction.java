package io.intino.plugin.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.DumbAware;
import io.intino.plugin.build.ModuleDependencyPropagator;
import io.intino.plugin.lang.psi.impl.IntinoUtil;
import org.jetbrains.annotations.NotNull;

public class UpdateVersionPropagationAction extends IntinoAction implements DumbAware {
	@Override
	public void actionPerformed(@NotNull AnActionEvent e) {
		execute(e.getData(LangDataKeys.MODULE));
	}

	@Override
	public void execute(Module module) {
		new ModuleDependencyPropagator(module, IntinoUtil.configurationOf(module)).execute();
	}


	@Override
	public void update(AnActionEvent e) {
		e.getPresentation().setEnabled(e.getData(LangDataKeys.MODULE) != null);
		e.getPresentation().setVisible(e.getData(LangDataKeys.MODULE) != null);
		super.update(e);
	}
}
