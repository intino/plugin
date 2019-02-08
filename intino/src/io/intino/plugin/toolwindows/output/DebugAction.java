package io.intino.plugin.toolwindows.output;

import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.icons.AllIcons;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import io.intino.alexandria.exceptions.BadRequest;
import io.intino.alexandria.exceptions.Unknown;
import io.intino.alexandria.logger.Logger;
import io.intino.cesar.box.schemas.ProcessInfo;
import io.intino.cesar.box.schemas.ProcessStatus;
import io.intino.plugin.project.CesarAccessor;

public class DebugAction extends AnAction implements DumbAware {
	private final CesarAccessor cesarAccessor;
	private ProcessInfo info;
	private ProcessStatus status;
	private RunContentDescriptor myContentDescriptor;

	public DebugAction(ProcessInfo info, RunContentDescriptor contentDescriptor, Project project) {
		this.info = info;
		myContentDescriptor = contentDescriptor;
		cesarAccessor = new CesarAccessor(project);
		status = cesarAccessor.processStatus(this.info.project(), this.info.id());
		final Presentation templatePresentation = getTemplatePresentation();
		templatePresentation.setIcon(AllIcons.Actions.StartDebugger);
		templatePresentation.setText("Debug remote process");
		templatePresentation.setDescription("Debug remote process");
	}

	@Override
	public void actionPerformed(AnActionEvent e) {
		status = cesarAccessor.processStatus(this.info.project(), this.info.id());
		try {
			Boolean success = cesarAccessor.accessor().postProcessStatus(this.info.project(), this.info.id(), status.running(), true);
			if (success) {
				status.running(!status.running());
				status = cesarAccessor.processStatus(this.info.project(), this.info.id());
				Notifications.Bus.notify(new Notification("Intino", "Process Debugging started", "Port " + status.debugPort(), NotificationType.INFORMATION), null);
			}
		} catch (BadRequest | Unknown bd) {
			Logger.error(bd);
		}
	}

	@Override
	public void update(AnActionEvent e) {
		super.update(e);
		super.update(e);
		if (status == null) status = cesarAccessor.processStatus(this.info.project(), this.info.id());
		if (status == null) e.getPresentation().setVisible(false);
		else e.getPresentation().setVisible(!status.debug());
	}
}
