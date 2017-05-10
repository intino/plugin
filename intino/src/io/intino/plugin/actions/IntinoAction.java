package io.intino.plugin.actions;

import com.intellij.notification.NotificationGroup;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;
import io.intino.plugin.IntinoIcons;
import io.intino.plugin.project.LegioConfiguration;
import io.intino.tara.compiler.shared.Configuration;
import io.intino.tara.plugin.lang.psi.impl.TaraUtil;

public abstract class IntinoAction extends AnAction {

	void notifyReload(Module module) {
		final NotificationGroup balloon = NotificationGroup.findRegisteredGroup("Tara Language");
		if (balloon != null)
			balloon.createNotification("Configuration of " + module.getName() + " reloaded", MessageType.INFO).setImportant(false).notify(module.getProject());
	}

	public abstract void execute(Module module);

	@Override
	public void update(AnActionEvent e) {
		final Project project = e.getProject();
		boolean visible = project != null && hasLegioModules(project);
		e.getPresentation().setVisible(visible);
		e.getPresentation().setEnabled(visible);
		e.getPresentation().setIcon(IntinoIcons.LEGIO_16);
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
