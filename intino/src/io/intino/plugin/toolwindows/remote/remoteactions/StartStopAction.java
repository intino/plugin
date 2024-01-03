package io.intino.plugin.toolwindows.remote.remoteactions;

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

import static com.intellij.icons.AllIcons.Actions.Suspend;
import static com.intellij.icons.AllIcons.RunConfigurations.TestState.Run;
import static com.intellij.openapi.actionSystem.CommonDataKeys.PROJECT;

public class StartStopAction extends AnAction implements DumbAware, IntinoConsoleAction {
	private final Configuration.Server.Type serverType;
	private final CesarAccessor cesarAccessor;
	private final DataContext dataContext;
	private Application app;
	private boolean inOperation = false;

	public StartStopAction(List<Application> infos, Configuration.Server.Type serverType, CesarAccessor cesarAccessor) {
		this.serverType = serverType;
		this.app = infos.isEmpty() ? null : infos.get(0);
		this.cesarAccessor = cesarAccessor;
		this.dataContext = dataContext();
		final Presentation templatePresentation = getTemplatePresentation();
		templatePresentation.setIcon(Run);
		templatePresentation.setDisabledIcon(AnimatedIcon.Default.INSTANCE);
		templatePresentation.setText("Run Remote Application");
		templatePresentation.setDescription("Run remote application");
	}

	@Override
	public @NotNull ActionUpdateThread getActionUpdateThread() {
		return ActionUpdateThread.EDT;
	}

	@Override
	public void onChanging() {
		inOperation = true;
	}

	public void onApplicationChange(Application newApp) {
		inOperation = true;
		new Thread(() -> {
			app = newApp;
			inOperation = false;
			update();
		}).start();
	}

	@Override
	public void actionPerformed(@NotNull AnActionEvent e) {
		inOperation = true;
		update(e);
		boolean sure = askAndContinue(e);
		if (!sure) return;
		new Thread(() -> {
			app = cesarAccessor.application(app.container(), app.id());
			try {
				Boolean success = cesarAccessor.accessor().postApplicationStatus(app.container(), app.id(), !app.running(), false);
				if (success) app.running(!app.running());
			} catch (BadRequest | InternalServerError | Unauthorized | NotFound ignored) {
			}
			inOperation = false;
			update(e);
		}).start();
	}

	private boolean askAndContinue(@NotNull AnActionEvent e) {
		if (!serverType.equals(Configuration.Server.Type.Pro)) return true;
		AtomicBoolean response = new AtomicBoolean(false);
		ApplicationManager.getApplication().invokeAndWait(() -> response.set(new IntinoConfirmationDialog(Objects.requireNonNull(e.getData(PROJECT)), "Are you sure to " + (app.running() ? "stop" : "start") + " this process?", "Change Application Status").showAndGet()));
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

	public void update(Presentation presentation) {
		presentation.setVisible(true);
		if (app == null || inOperation) presentation.setEnabled(false);
		else {
			presentation.setEnabled(true);
			presentation.setVisible(true);
			presentation.setIcon(app.running() ? Suspend : Run);
			presentation.setText(app.running() ? "Stop Application" : "Run Application");
			presentation.setDescription(app.running() ? "Stop application" : "Run application");
		}
	}


	@Nullable
	private DataContext dataContext() {
		DataContext dataContext = null;
		try {
			dataContext = DataManager.getInstance().getDataContextFromFocusAsync().blockingGet(1000);
		} catch (TimeoutException | ExecutionException e) {
			io.intino.alexandria.logger.Logger.error(e);
		}
		return dataContext;
	}
}
