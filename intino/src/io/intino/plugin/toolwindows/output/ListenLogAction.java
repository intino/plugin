package io.intino.plugin.toolwindows.output;

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
import io.intino.plugin.project.CesarAccessor;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

class ListenLogAction extends AnAction implements DumbAware, IntinoConsoleAction {
	private final Icon LogIcon = AllIcons.Debugger.Console;
	@NotNull
	private final List<ProcessInfo> infos;
	private final CesarAccessor cesarAccessor;
	private final Map<String, Consumer<String>> consumers;
	private ProcessInfo selectedProcess;
	private boolean ignited = false;
	private boolean inProcess = false;

	public ListenLogAction(List<ProcessInfo> infos, CesarAccessor cesarAccessor, Map<String, Consumer<String>> consumers) {
		this.infos = infos;
		this.cesarAccessor = cesarAccessor;
		this.consumers = consumers;
		selectedProcess = infos.get(0);
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
		consumers.get(selectedProcess.id()).accept(OutputsToolWindow.CLEAR);
		selectedProcess = newProcess;
		inProcess = false;
		update();
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
		if (inProcess) p.setEnabled(false);
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
	}

	private void listenLog() {
		try {
			Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
			cesarAccessor.accessor().listenLog(selectedProcess.id(), text -> {
				int endIndex = text.indexOf("#");
				if (endIndex < 0) return;
				int messageStart = text.indexOf("#", endIndex + 1);
				Consumer<String> consumer = consumers.get(processId(text.substring(0, messageStart)));
				if (consumer != null) consumer.accept(text.substring(messageStart + 1));
			});
		} catch (io.intino.alexandria.exceptions.InternalServerError e) {
			Logger.getInstance(ProcessOutputLoader.class.getName()).info(e.getMessage(), e);
		}
	}

	private String processId(String code) {
		return code.contains("#") ? code.substring(0, code.indexOf("#")) : code;
	}

	private void initLog() {
		String processLog = null;
		try {
			processLog = cesarAccessor.accessor().getProcessLog(selectedProcess.server().name(), selectedProcess.id(), 1).replace("\\n", "\n");
		} catch (BadRequest | InternalServerError e) {
			Logger.getInstance(ProcessOutputLoader.class.getName()).error(e.getMessage(), e);
		}
		if (processLog != null) {
			consumers.get(selectedProcess.id()).accept(OutputsToolWindow.CLEAR);
			consumers.get(selectedProcess.id()).accept(processLog);
			ignited = true;
		}
	}
}
