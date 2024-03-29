package io.intino.plugin.toolwindows.remote.remoteactions;

import com.intellij.icons.AllIcons;
import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.DumbAware;
import com.intellij.ui.AnimatedIcon;
import io.intino.alexandria.exceptions.BadRequest;
import io.intino.alexandria.exceptions.InternalServerError;
import io.intino.alexandria.exceptions.NotFound;
import io.intino.alexandria.exceptions.Unauthorized;
import io.intino.cesar.box.schemas.Application;
import io.intino.plugin.IntinoIcons;
import io.intino.plugin.cesar.CesarAccessor;
import io.intino.plugin.toolwindows.remote.IntinoConsoleAction;
import io.intino.plugin.toolwindows.remote.Log;
import io.intino.plugin.toolwindows.remote.RemoteWindow;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

import static io.intino.alexandria.logger.Logger.Level;

public class ListenLogAction extends AnAction implements DumbAware, IntinoConsoleAction {
	private final Icon LogIcon = AllIcons.Debugger.Console;
	private final CesarAccessor cesarAccessor;
	private final Consumer<Log> console;
	private final DataContext dataContext;
	private Application selectedApplication;
	private boolean ignited = false;
	private boolean inProcess = false;
	private Level level;
	private String currentLog = "";

	public ListenLogAction(List<Application> apps, CesarAccessor cesarAccessor, Consumer<Log> console) {
		this.cesarAccessor = cesarAccessor;
		this.console = console;
		this.selectedApplication = apps.isEmpty() ? null : apps.get(0);
		this.dataContext = dataContext();
		this.level = Level.TRACE;
		final Presentation p = getTemplatePresentation();
		p.setIcon(LogIcon);
		p.setDisabledIcon(AnimatedIcon.Default.INSTANCE);
		p.setText("Listen Log");
		p.setDescription("Listen log");
	}

	@Override
	public @NotNull ActionUpdateThread getActionUpdateThread() {
		return ActionUpdateThread.EDT;
	}

	@Override
	public void onChanging() {
		inProcess = true;
	}

	public void onApplicationChange(Application newApplication) {
		inProcess = true;
		new Thread(this::stop).start();
		console.accept(new Log(RemoteWindow.CLEAR, Level.DEBUG));
		selectedApplication = newApplication;
		inProcess = false;
		update();
	}

	public void onLevelChange(Level level) {
		this.level = level;
		console.accept(new Log(RemoteWindow.CLEAR, Level.DEBUG));
		if (!currentLog.isEmpty()) console.accept(new Log(currentLog, level));
	}

	@Override
	public void actionPerformed(@NotNull AnActionEvent e) {
		inProcess = true;
		update(e);
		new Thread(() -> {
			if (ignited) stop();
			else {
				initLog();
				listenLog();
				update(e);
			}
			inProcess = false;
			update(e);
		}).start();

	}

	@Override
	public void update(@NotNull AnActionEvent e) {
		super.update(e);
		update(e.getPresentation());
	}

	public void update() {
		update(new AnActionEvent(null, dataContext, ActionPlaces.UNKNOWN, new Presentation(), ActionManager.getInstance(), 0));
	}

	private void update(Presentation p) {
		if (selectedApplication == null || inProcess) p.setEnabled(false);
		else {
			p.setEnabled(true);
			p.setIcon(ignited ? IntinoIcons.STOP_CONSOLE : LogIcon);
			p.setText(ignited ? "Stop Listen Log" : "Listen Log");
			p.setDescription(ignited ? "Stop listen log" : "Listen log");
		}
	}

	private void stop() {
		cesarAccessor.accessor().stopListenLog();
		ignited = false;
		currentLog = "";
	}

	private void listenLog() {
		try {
			Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
			cesarAccessor.accessor().listenLog(selectedApplication.id(), text -> {
				int endIndex = text.indexOf("#");
				if (endIndex < 0) return;
				int messageStart = text.indexOf("#", endIndex + 1);
				String log = text.substring(messageStart + 1);
				currentLog += "\n" + log;
				console.accept(new Log(log, level));
			});
		} catch (io.intino.alexandria.exceptions.InternalServerError e) {
			Logger.getInstance(ListenLogAction.class.getName()).info(e.getMessage(), e);
		}
	}

	private void initLog() {
		String processLog = null;
		try {
			processLog = cesarAccessor.accessor().getApplicationLog(selectedApplication.container(), selectedApplication.id(), 1).replace("\\n", "\n");
		} catch (BadRequest | InternalServerError | Unauthorized | NotFound e) {
			Logger.getInstance(ListenLogAction.class.getName()).error(e.getMessage(), e);
		}
		if (processLog != null) {
			console.accept(new Log(RemoteWindow.CLEAR, Level.DEBUG));
			console.accept(new Log(processLog, level));
			ignited = true;
			currentLog = processLog;
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
