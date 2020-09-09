package io.intino.plugin.toolwindows.output;

import com.intellij.icons.AllIcons;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import io.intino.alexandria.exceptions.BadRequest;
import io.intino.alexandria.exceptions.InternalServerError;
import io.intino.alexandria.logger.Logger;
import io.intino.cesar.box.schemas.ProcessInfo;
import io.intino.cesar.box.schemas.ProcessStatus;
import io.intino.plugin.project.CesarAccessor;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DebugAction extends AnAction implements DumbAware {
	private final CesarAccessor cesarAccessor;
	private final Map<String, ProcessInfo> infos;
	private ProcessInfo selectedProcess;
	private ProcessStatus status;

	public DebugAction(List<ProcessInfo> infos, ComboBox<Object> processSelector, Project project) {
		this.infos = infos.stream().collect(Collectors.toMap(ProcessInfo::id, i -> i));
		cesarAccessor = new CesarAccessor(project);
		selectedProcess = infos.get(0);
		processSelector.addItemListener(e -> {
			String anObject = e.getItem().toString();
			selectedProcess = infos.stream().filter(p -> p.artifact().equals(anObject)).findFirst().get();
			status = cesarAccessor.processStatus(selectedProcess.server().name(), selectedProcess.id());
		});
		status = cesarAccessor.processStatus(selectedProcess.server().name(), selectedProcess.id());
		final Presentation templatePresentation = getTemplatePresentation();
		templatePresentation.setIcon(AllIcons.Actions.StartDebugger);
		templatePresentation.setText("Debug Remote Process");
		templatePresentation.setDescription("Debug remote process");
	}

	@Override
	public void actionPerformed(@NotNull AnActionEvent e) {
		status = cesarAccessor.processStatus(selectedProcess.server().name(), selectedProcess.id());
		try {
			Boolean success = cesarAccessor.accessor().postProcessStatus(selectedProcess.server().name(), selectedProcess.id(), status.running(), true);
			if (success) {
				status.running(!status.running());
				status = cesarAccessor.processStatus(selectedProcess.server().name(), selectedProcess.id());
				Notifications.Bus.notify(new Notification("Intino", "Process Debugging started", "Port " + status.debugPort(), NotificationType.INFORMATION), null);
			}
		} catch (BadRequest | InternalServerError bd) {
			Logger.error(bd);
		}
	}

	@Override
	public void update(AnActionEvent e) {
		super.update(e);
		super.update(e);
		if (status == null) status = cesarAccessor.processStatus(selectedProcess.server().name(), selectedProcess.id());
		if (status == null) e.getPresentation().setVisible(false);
		else e.getPresentation().setVisible(!status.debug());
	}
}
