package io.intino.plugin.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import io.intino.plugin.IntinoIcons;
import tara.intellij.lang.psi.impl.TaraUtil;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("Duplicates")
public class IntinoActionGroup extends DefaultActionGroup {
	@Override
	public void update(AnActionEvent e) {
		final Project project = e.getData(CommonDataKeys.PROJECT);
		boolean enabled = !collectTaraModules(project).isEmpty();
		e.getPresentation().setVisible(enabled);
		e.getPresentation().setEnabled(enabled);
		e.getPresentation().setIcon(IntinoIcons.INTINO_16);
	}

	private List<Module> collectTaraModules(Project project) {
		List<Module> modules = new ArrayList<>();
		for (Module module : ModuleManager.getInstance(project).getModules())
			if (TaraUtil.configurationOf(module) != null)
				modules.add(module);
		return modules;
	}
}
