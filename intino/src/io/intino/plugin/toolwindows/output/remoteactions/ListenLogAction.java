package io.intino.plugin.toolwindows.output.remoteactions;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.DumbAware;
import com.intellij.ui.AnimatedIcon;
import io.intino.alexandria.exceptions.BadRequest;
import io.intino.alexandria.exceptions.InternalServerError;
import io.intino.cesar.box.schemas.ProcessInfo;
import io.intino.cesar.box.schemas.ProcessStatus;
import io.intino.plugin.IntinoIcons;
import io.intino.plugin.cesar.CesarAccessor;
import io.intino.plugin.toolwindows.output.ConsoleWindow;
import io.intino.plugin.toolwindows.output.IntinoConsoleAction;
import io.intino.plugin.toolwindows.output.Log;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.List;
import java.util.function.Consumer;

import static io.intino.alexandria.logger.Logger.Level;

public class ListenLogAction extends AnAction implements DumbAware, IntinoConsoleAction {
	private final Icon LogIcon = AllIcons.Debugger.Console;
	private final CesarAccessor cesarAccessor;
	private final Consumer<Log> console;
	private ProcessInfo selectedProcess;
	private boolean ignited = false;
	private boolean inProcess = false;
	private Level level;
	private String currentLog = "";

	public ListenLogAction(List<ProcessInfo> infos, CesarAccessor cesarAccessor, Consumer<Log> console) {
		this.cesarAccessor = cesarAccessor;
		this.console = console;
		selectedProcess = infos.isEmpty() ? null : infos.get(0);
		this.level = Level.TRACE;
		final Presentation p = getTemplatePresentation();
		p.setIcon(LogIcon);
		p.setDisabledIcon(AnimatedIcon.Default.INSTANCE);
		p.setText("Listen Log");
		p.setDescription("Listen log");
	}

	@Override
	public void onChanging() {
		inProcess = true;
	}

	public void onProcessChange(ProcessInfo newProcess, ProcessStatus newProcessStatus) {
		inProcess = true;
		new Thread(this::stop).start();
		console.accept(new Log(ConsoleWindow.CLEAR, Level.DEBUG));
		selectedProcess = newProcess;
		inProcess = false;
		update();
	}

	public void onLevelChange(Level level) {
		this.level = level;
		console.accept(new Log(ConsoleWindow.CLEAR, Level.DEBUG));
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
		update(this.getTemplatePresentation());
	}

	private void update(Presentation p) {
		if (selectedProcess == null || inProcess) p.setEnabled(false);
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
			cesarAccessor.accessor().listenLog(selectedProcess.id(), text -> {
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
			processLog = cesarAccessor.accessor().getProcessLog(selectedProcess.server().name(), selectedProcess.id(), 1).replace("\\n", "\n");
		} catch (BadRequest | InternalServerError e) {
			Logger.getInstance(ListenLogAction.class.getName()).error(e.getMessage(), e);
		}
		if (processLog != null) {
			console.accept(new Log(ConsoleWindow.CLEAR, Level.DEBUG));
			console.accept(new Log(processLog, level));
			ignited = true;
			currentLog = processLog;
		}
	}
}
