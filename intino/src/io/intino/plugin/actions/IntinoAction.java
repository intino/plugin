package io.intino.plugin.actions;

import com.intellij.notification.NotificationGroup;
import com.intellij.notification.NotificationGroupManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;
import io.intino.plugin.IntinoIcons;
import io.intino.plugin.lang.psi.impl.IntinoUtil;
import io.intino.plugin.project.configuration.LegioConfiguration;

import java.util.Arrays;

public abstract class IntinoAction extends AnAction {

	void notifyReload(Module module) {
		NotificationGroup balloon = NotificationGroup.findRegisteredGroup("Intino");
		if (balloon == null) balloon = NotificationGroupManager.getInstance().getNotificationGroup("Intino");
		balloon.createNotification("Artifact " + module.getName() + ": reloaded", MessageType.INFO).setImportant(false).notify(module.getProject());
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
		return Arrays.stream(ModuleManager.getInstance(project).getModules()).
				anyMatch(module -> IntinoUtil.configurationOf(module) instanceof LegioConfiguration);
	}
}
