package io.intino.plugin.toolwindows.output;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.DumbAware;
import com.intellij.ui.AnimatedIcon;
import com.intellij.util.ui.ConfirmationDialog;
import io.intino.Configuration;
import io.intino.alexandria.exceptions.BadRequest;
import io.intino.alexandria.exceptions.InternalServerError;
import io.intino.cesar.box.schemas.ProcessInfo;
import io.intino.cesar.box.schemas.ProcessStatus;
import io.intino.plugin.IntinoIcons;
import io.intino.plugin.project.CesarAccessor;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.intellij.icons.AllIcons.Actions.Suspend;
import static com.intellij.icons.AllIcons.RunConfigurations.TestState.Run;
import static com.intellij.openapi.vcs.VcsShowConfirmationOption.STATIC_SHOW_CONFIRMATION;

public class StartStopAction extends AnAction implements DumbAware, IntinoConsoleAction {
	@NotNull
	private final List<ProcessInfo> infos;
	private final Configuration.Server.Type serverType;
	private final CesarAccessor cesarAccessor;
	private ProcessInfo selectedProcess;
	private ProcessStatus status;
	private boolean inProcess = false;

	public StartStopAction(List<ProcessInfo> infos, Configuration.Server.Type serverType, CesarAccessor cesarAccessor) {
		this.infos = infos;
		this.serverType = serverType;
		this.selectedProcess = infos.get(0);
		this.cesarAccessor = cesarAccessor;
		status = this.cesarAccessor.processStatus(selectedProcess.server().name(), selectedProcess.id());
		final Presentation templatePresentation = getTemplatePresentation();
		templatePresentation.setIcon(Run);
		templatePresentation.setDisabledIcon(AnimatedIcon.Default.INSTANCE);
		templatePresentation.setText("Run Remote Process");
		templatePresentation.setDescription("Run remote process");
	}

	@Override
	public void onChanging() {
		inProcess = true;
	}

	public void onProcessChange(ProcessInfo newProcess, ProcessStatus newProcessStatus) {
		inProcess = true;
		new Thread(() -> {
			selectedProcess = newProcess;
			status = newProcessStatus;
			inProcess = false;
			update();
		}).start();
	}

	@Override
	public void actionPerformed(@NotNull AnActionEvent e) {
		inProcess = true;
		update(e);
		boolean sure = askAndContinue(e);
		if (!sure) return;
		new Thread(() -> {
			status = cesarAccessor.processStatus(selectedProcess.server().name(), selectedProcess.id());
			try {
				Boolean success = cesarAccessor.accessor().postProcessStatus(selectedProcess.server().name(), selectedProcess.id(), !status.running(), false);
				if (success) status.running(!status.running());
			} catch (BadRequest | InternalServerError bd) {
			}
			inProcess = false;
			update(e);
		}).start();
	}

	private boolean askAndContinue(@NotNull AnActionEvent e) {
		if (!serverType.equals(Configuration.Server.Type.Pro)) return true;
		AtomicBoolean response = new AtomicBoolean(false);
		ApplicationManager.getApplication().invokeAndWait(() -> {
			ConfirmationDialog confirmationDialog = new ConfirmationDialog(e.getData(CommonDataKeys.PROJECT),
					"Are you sure to " + (status.running() ? "stop" : "start") + " this process?",
					"Change Process status", IntinoIcons.INTINO_80, STATIC_SHOW_CONFIRMATION);
			confirmationDialog.setDoNotAskOption(null);
			response.set(confirmationDialog.showAndGet());
		});
		return response.get();
	}

	@Override
	public void update(@NotNull AnActionEvent e) {
		super.update(e);
		update(e.getPresentation());
	}

	public void update() {
		update(this.getTemplatePresentation());
	}

	public void update(Presentation presentation) {
		presentation.setVisible(true);
		if (inProcess) presentation.setEnabled(false);
		else {
			if (status == null)
				status = cesarAccessor.processStatus(selectedProcess.server().name(), selectedProcess.id());
			if (status == null) presentation.setVisible(false);
			else {
				presentation.setEnabled(true);
				presentation.setVisible(true);
				presentation.setIcon(status.running() ? Suspend : Run);
				presentation.setText(status.running() ? "Stop Process" : "Run Process");
				presentation.setDescription(status.running() ? "Stop process" : "Run process");
			}
		}
	}
}
