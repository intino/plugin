package io.intino.plugin.toolwindows.remote.remoteactions;

import com.intellij.icons.AllIcons;
import com.intellij.ide.DataManager;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.DumbAware;
import com.intellij.ui.AnimatedIcon;
import io.intino.Configuration;
import io.intino.alexandria.exceptions.BadRequest;
import io.intino.alexandria.exceptions.InternalServerError;
import io.intino.alexandria.exceptions.NotFound;
import io.intino.alexandria.exceptions.Unauthorized;
import io.intino.alexandria.logger.Logger;
import io.intino.cesar.box.schemas.ProcessInfo;
import io.intino.cesar.box.schemas.ProcessStatus;
import io.intino.plugin.actions.IntinoConfirmationDialog;
import io.intino.plugin.cesar.CesarAccessor;
import io.intino.plugin.toolwindows.remote.IntinoConsoleAction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

import static io.intino.Configuration.Server.Type.Pro;

public class DebugAction extends AnAction implements DumbAware, IntinoConsoleAction {
	private final Configuration.Server.Type serverType;
	private final CesarAccessor cesarAccessor;
	private final DataContext dataContext;
	private ProcessInfo process;
	private ProcessStatus status;
	private boolean inProcess = false;

	public DebugAction(List<ProcessInfo> infos, Configuration.Server.Type serverType, CesarAccessor cesarAccessor) {
		this.serverType = serverType;
		this.cesarAccessor = cesarAccessor;
		process = infos.isEmpty() ? null : infos.get(0);
		status = process == null ? null : this.cesarAccessor.processStatus(process.server().name(), process.id());
		dataContext = dataContext();
		final Presentation presentation = getTemplatePresentation();
		presentation.setIcon(AllIcons.Actions.StartDebugger);
		presentation.setText((status != null && status.debug() ? "Restart " : "") + "Debug Remote Process");
		presentation.setDisabledIcon(AnimatedIcon.Default.INSTANCE);
		presentation.setDescription((status != null && status.debug() ? "Restart " : "") + "Debug remote process");
	}

	@Override
	public void onChanging() {
		inProcess = true;
	}

	public void onProcessChange(ProcessInfo newProcess, ProcessStatus newProcessStatus) {
		inProcess = true;
		new Thread(() -> {
			process = newProcess;
			status = newProcessStatus;
			inProcess = false;
			update();
		}).start();
	}

	@Override
	public void actionPerformed(@NotNull AnActionEvent e) {
		inProcess = true;
		boolean sure = askAndContinue(e);
		if (!sure) return;
		new Thread(() -> {
			status = cesarAccessor.processStatus(process.server().name(), process.id());
			try {
				Boolean success = cesarAccessor.accessor().postProcessStatus(process.server().name(), process.id(), status.running(), true);
				if (success) {
					status.running(!status.running());
					status = cesarAccessor.processStatus(process.server().name(), process.id());
					Notifications.Bus.notify(new Notification("Intino", "Process Debugging started", "Port " + process.debugPort(), NotificationType.INFORMATION), null);
				}
			} catch (BadRequest | InternalServerError | Unauthorized | NotFound ignored) {
			}
			inProcess = false;
			update(e);
		}).start();
	}

	private boolean askAndContinue(@NotNull AnActionEvent e) {
		if (!serverType.equals(Pro)) return true;
		AtomicBoolean response = new AtomicBoolean(false);
		ApplicationManager.getApplication().invokeAndWait(() ->
				response.set(new IntinoConfirmationDialog(Objects.requireNonNull(e.getData(CommonDataKeys.PROJECT)),
						"Are you sure to debug this process?",
						"Change Process Status").showAndGet()));
		return response.get();
	}

	public void update() {
		update(new AnActionEvent(null, dataContext, ActionPlaces.UNKNOWN, new Presentation(), ActionManager.getInstance(), 0));
	}

	@Nullable
	private DataContext dataContext() {
		DataContext dataContext = null;
		try {
			dataContext = DataManager.getInstance().getDataContextFromFocusAsync().blockingGet(1000);
		} catch (TimeoutException | ExecutionException e) {
			Logger.error(e);
		}
		return dataContext;
	}

	@Override
	public void update(@NotNull AnActionEvent e) {
		super.update(e);
		update(e.getPresentation());
	}

	private void update(Presentation presentation) {
		if (process == null || inProcess) presentation.setEnabled(false);
		else {
			if (status == null)
				status = cesarAccessor.processStatus(process.server().name(), process.id());
			if (status == null) presentation.setEnabled(false);
			else {
				if (status.debug()) presentation.setIcon(AllIcons.Actions.RestartDebugger);
				else presentation.setIcon(AllIcons.Actions.StartDebugger);
			}
		}
	}
}