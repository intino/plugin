package io.intino.plugin.toolwindows.remote.remoteactions;

import com.intellij.icons.AllIcons;
import com.intellij.ide.DataManager;
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
import io.intino.cesar.box.schemas.Application;
import io.intino.plugin.actions.IntinoConfirmationDialog;
import io.intino.plugin.cesar.CesarAccessor;
import io.intino.plugin.toolwindows.remote.IntinoConsoleAction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

public class RestartAction extends AnAction implements DumbAware, IntinoConsoleAction {
	private final Configuration.Server.Type serverType;
	private final CesarAccessor cesarAccessor;
	private final DataContext dataContext;
	private final AtomicBoolean isChanging = new AtomicBoolean(false);
	private Application selectedApp;

	public RestartAction(List<Application> infos, Configuration.Server.Type serverType, CesarAccessor cesarAccessor) {
		this.serverType = serverType;
		this.selectedApp = infos.isEmpty() ? null : infos.get(0);
		this.cesarAccessor = cesarAccessor;
		dataContext = dataContext();
		final Presentation presentation = getTemplatePresentation();
		presentation.setText("Rerun Remote Process");
		presentation.setDescription("Rerun remote process");
		presentation.setDisabledIcon(AnimatedIcon.Default.INSTANCE);
		presentation.setIcon(AllIcons.Actions.Restart);
	}

	@Override
	public void onChanging() {
		isChanging.set(true);
	}

	@Override
	public @NotNull ActionUpdateThread getActionUpdateThread() {
		return ActionUpdateThread.EDT;
	}

	public void onApplicationChange(Application newApp) {
		isChanging.set(true);
		new Thread(() -> {
			selectedApp = newApp;
			isChanging.set(false);
			update();
		}).start();
	}

	@Override
	public void actionPerformed(@NotNull AnActionEvent e) {
		isChanging.set(true);
		update(e);
		boolean sure = askAndContinue(e);
		if (!sure) return;
		new Thread(() -> {
			try {
				cesarAccessor.accessor().postApplicationStatus(selectedApp.container(), selectedApp.id(), true, false);
			} catch (BadRequest | InternalServerError | Unauthorized | NotFound ignored) {
			}
			isChanging.set(false);
			update(e);
		}).start();
	}

	private boolean askAndContinue(@NotNull AnActionEvent e) {
		if (!serverType.equals(Configuration.Server.Type.Pro)) return true;
		AtomicBoolean response = new AtomicBoolean(false);
		ApplicationManager.getApplication().invokeAndWait(() -> {
			response.set(new IntinoConfirmationDialog(e.getData(CommonDataKeys.PROJECT),
					"Are you sure to restart this process?",
					"Restart Process").showAndGet());
		});
		return response.get();
	}

	@Override
	public void update(@NotNull AnActionEvent e) {
		super.update(e);
		update(e.getPresentation());
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

	public void update(Presentation p) {
		p.setVisible(true);
		if (selectedApp == null) p.setVisible(false);
		else if (isChanging.get()) p.setEnabled(false);
		else {
			if (selectedApp == null) {
				selectedApp = cesarAccessor.application(selectedApp.container(), selectedApp.id());
				p.setVisible(false);
			} else {
				p.setEnabled(true);
				p.setVisible(selectedApp.running());
			}
		}
	}
}
