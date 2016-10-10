package org.siani.legio.plugin.actions;

import com.intellij.notification.NotificationGroup;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;
import org.siani.legio.plugin.LegioIcons;
import org.siani.legio.plugin.project.LegioConfiguration;
import tara.compiler.shared.Configuration;
import tara.intellij.lang.psi.impl.TaraUtil;

public class ReloadConfigurationAction extends AnAction implements DumbAware {
	@Override
	public void actionPerformed(AnActionEvent e) {
		final Project project = e.getProject();
		if (project == null) return;
		final Module[] modules = ModuleManager.getInstance(project).getModules();
		for (Module module : modules) {
			final Configuration configuration = TaraUtil.configurationOf(module);
			if (configuration != null && configuration instanceof LegioConfiguration) {
				((LegioConfiguration) configuration).reloadInfo(null);
				notifyReload(module);
			}
		}
	}

	private void notifyReload(Module module) {
		final NotificationGroup balloon = NotificationGroup.toolWindowGroup("Tara Language", "Balloon");
		balloon.createNotification("Configuration of " + module.getName() + " reloaded", MessageType.INFO).setImportant(false).notify(module.getProject());
	}

	@Override
	public void update(AnActionEvent e) {
		final Project project = e.getProject();
		boolean visible = project != null && hasLegioModules(project);
		e.getPresentation().setVisible(visible);
		e.getPresentation().setEnabled(visible);
		e.getPresentation().setIcon(LegioIcons.ICON_16);
	}

	private boolean hasLegioModules(Project project) {
		final Module[] modules = ModuleManager.getInstance(project).getModules();
		for (Module module : modules) {
			final Configuration configuration = TaraUtil.configurationOf(module);
			if (configuration != null && configuration instanceof LegioConfiguration)
				return true;
		}
		return false;
	}
}
