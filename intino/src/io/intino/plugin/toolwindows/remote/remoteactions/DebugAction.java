package io.intino.plugin.toolwindows.remote.remoteactions;

import com.intellij.icons.AllIcons.Actions;
import com.intellij.ide.DataManager;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.DumbAware;
import com.intellij.ui.AnimatedIcon;
import io.intino.Configuration;
import io.intino.alexandria.exceptions.AlexandriaException;
import io.intino.alexandria.logger.Logger;
import io.intino.cesar.box.schemas.Application;
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
	private final AtomicBoolean isChanging = new AtomicBoolean(false);
	private Application application;

	public DebugAction(List<Application> applications, Configuration.Server.Type serverType, CesarAccessor cesarAccessor) {
		this.serverType = serverType;
		this.cesarAccessor = cesarAccessor;
		application = applications.isEmpty() ? null : applications.get(0);
		dataContext = dataContext();
		final Presentation presentation = getTemplatePresentation();
		presentation.setIcon(Actions.StartDebugger);
		presentation.setText((application != null && application.debugging() ? "Restart " : "") + "Debug Remote Process");
		presentation.setDisabledIcon(AnimatedIcon.Default.INSTANCE);
		presentation.setDescription((application != null && application.debugging() ? "Restart " : "") + "Debug remote process");
	}

	@Override
	public @NotNull ActionUpdateThread getActionUpdateThread() {
		return ActionUpdateThread.EDT;
	}

	@Override
	public void onChanging() {
		isChanging.set(true);
	}

	public void onApplicationChange(Application application) {
		isChanging.set(true);
		new Thread(() -> {
			this.application = application;
			isChanging.set(false);
			update();
		}).start();
	}

	@Override
	public void actionPerformed(@NotNull AnActionEvent e) {
		isChanging.set(true);
		boolean sure = askAndContinue(e);
		if (!sure) return;
		new Thread(() -> {
			application = cesarAccessor.application(application.container(), application.id());
			try {
				Boolean success = cesarAccessor.accessor().postApplicationStatus(application.container(), application.id(), application.running(), true);
				if (success) {
					application = cesarAccessor.application(application.container(), application.id());
					Notifications.Bus.notify(new Notification("Intino", "Process Debugging started", "Port " + application.debugPort(), NotificationType.INFORMATION), null);
				}
			} catch (AlexandriaException ignored) {
			}
			isChanging.set(false);
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
		update(new AnActionEvent(dataContext, new Presentation(), ActionPlaces.UNKNOWN, ActionUiKind.NONE, null, 0, ActionManager.getInstance()));
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
		if (application == null || isChanging.get()) presentation.setEnabled(false);
		else {
			if (application.debugging()) presentation.setIcon(Actions.RestartDebugger);
			else presentation.setIcon(Actions.StartDebugger);
		}
	}
}
