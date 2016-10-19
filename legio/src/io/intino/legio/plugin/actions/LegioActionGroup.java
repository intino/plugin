package io.intino.legio.plugin.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import io.intino.legio.plugin.LegioIcons;
import tara.intellij.project.TaraModuleType;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("Duplicates")
public class LegioActionGroup extends DefaultActionGroup {
	@Override
	public void update(AnActionEvent e) {
		final Project project = e.getData(CommonDataKeys.PROJECT);
		boolean enabled = !collectTaraModules(project).isEmpty();
		e.getPresentation().setVisible(enabled);
		e.getPresentation().setEnabled(enabled);
		e.getPresentation().setIcon(LegioIcons.ICON_16);
	}

	private List<Module> collectTaraModules(Project project) {
		List<Module> taraModules = new ArrayList<>();
		for (Module module : ModuleManager.getInstance(project).getModules())
			if (TaraModuleType.isTara(module))
				taraModules.add(module);
		return taraModules;
	}
}
