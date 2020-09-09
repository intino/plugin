package io.intino.plugin.toolwindows.output;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import io.intino.alexandria.exceptions.BadRequest;
import io.intino.alexandria.exceptions.InternalServerError;
import io.intino.cesar.box.schemas.ProcessInfo;
import io.intino.plugin.project.CesarAccessor;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

class ListenLogAction extends AnAction implements DumbAware {
	private final Icon LogIcon = AllIcons.Debugger.Console;
	private final CesarAccessor cesarAccessor;
	private final Project project;
	private final Map<String, Consumer<String>> consumers;
	private ProcessInfo selectedProcess;
	private boolean inited = false;

	public ListenLogAction(List<ProcessInfo> infos, ComboBox<Object> processesSelector, Project project, Map<String, Consumer<String>> consumers) {
		this.project = project;
		this.consumers = consumers;
		cesarAccessor = new CesarAccessor(project);
		selectedProcess = infos.get(0);
		processesSelector.addItemListener(e -> {
			stop();
			consumers.get(selectedProcess.id()).accept(OutputsToolWindow.CLEAR);
			String anObject = e.getItem().toString();
			selectedProcess = infos.stream().filter(p -> p.artifact().equals(anObject)).findFirst().get();
		});
		final Presentation templatePresentation = getTemplatePresentation();
		templatePresentation.setIcon(LogIcon);
		templatePresentation.setText("Listen Log");
		templatePresentation.setDescription("Listen log");
	}

	@Override
	public void actionPerformed(@NotNull AnActionEvent e) {
		if (inited) stop();
		else {
			initLog();
			listenLog();
			update(e);
		}
	}

	@Override
	public void update(@NotNull AnActionEvent e) {
		super.update(e);
		e.getPresentation().setIcon(inited ? AllIcons.Actions.Suspend : LogIcon);
		e.getPresentation().setText(inited ? "Stop Listen Log" : "Listen Log");
		e.getPresentation().setDescription(inited ? "Stop listen log" : "Listen log");
	}

	public void stop() {
		cesarAccessor.accessor().stopListenLog();
		inited = false;
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
			CesarAccessor cesarAccessor = new CesarAccessor(project);
			processLog = cesarAccessor.accessor().getProcessLog(selectedProcess.server().name(), selectedProcess.id(), 1).replace("\\n", "\n");
		} catch (BadRequest | InternalServerError e) {
			Logger.getInstance(ProcessOutputLoader.class.getName()).error(e.getMessage(), e);
		}
		if (processLog != null) {
			consumers.get(selectedProcess.id()).accept(OutputsToolWindow.CLEAR);
			consumers.get(selectedProcess.id()).accept(processLog);
			inited = true;
		}
	}
}
