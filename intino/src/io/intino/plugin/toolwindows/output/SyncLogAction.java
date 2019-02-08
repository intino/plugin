package io.intino.plugin.toolwindows.output;

import com.google.gson.JsonParser;
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import io.intino.alexandria.exceptions.BadRequest;
import io.intino.alexandria.exceptions.Unknown;
import io.intino.cesar.box.schemas.ProcessInfo;
import io.intino.plugin.project.CesarAccessor;
import io.intino.plugin.project.ProcessOutputLoader;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.Map;
import java.util.function.Consumer;

import static com.intellij.icons.AllIcons.Ide.Macro.Recording_stop;

class SyncLogAction extends AnAction implements DumbAware {
	private final Icon LogIcon = AllIcons.Debugger.Console_log;
	private final ProcessInfo info;
	private final CesarAccessor cesarAccessor;
	private final  RunContentDescriptor myContentDescriptor;
	private final Project project;
	private final Map<String, Consumer<String>> consumers;
	private boolean inited = false;

	public SyncLogAction(ProcessInfo info, RunContentDescriptor contentDescriptor, Project project, Map<String, Consumer<String>> consumers) {
		this.info = info;
		myContentDescriptor = contentDescriptor;
		this.project = project;
		this.consumers = consumers;
		cesarAccessor = new CesarAccessor(project);
		final Presentation templatePresentation = getTemplatePresentation();
		templatePresentation.setIcon(LogIcon);
		templatePresentation.setText("Listen Log");
		templatePresentation.setDescription("Listen Log");
	}

	@Override
	public void actionPerformed(@NotNull AnActionEvent e) {
		if (inited) {
			stop();
			inited = false;
			return;
		}
		initLog();
		listenLog();
		update(e);
	}

	@Override
	public void update(AnActionEvent e) {
		super.update(e);
		e.getPresentation().setIcon(inited ? Recording_stop : LogIcon);
	}

	public void stop() {
		cesarAccessor.accessor().stopListenLog();
	}

	private void listenLog() {
		try {
			Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
			cesarAccessor.accessor().listenLog(project.getName(), text -> {
				text = new JsonParser().parse(text).getAsJsonObject().get("name").getAsString();
				int endIndex = text.indexOf("#");
				if (endIndex < 0) return;
				int messageStart = text.indexOf("#", endIndex + 1);
				Consumer<String> consumer = consumers.get(processId(text.substring(0, messageStart)));
				if (consumer != null) consumer.accept(text.substring(messageStart + 1));
			});
		} catch (io.intino.alexandria.exceptions.Unknown e) {
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
			processLog = cesarAccessor.accessor().getProcessLog(project.getName(), info.id()).replace("\\n", "\n");
		} catch (BadRequest | Unknown e) {
			Logger.getInstance(ProcessOutputLoader.class.getName()).error(e.getMessage(), e);
		}
		consumers.get(info.id()).accept(OutputsToolWindow.CLEAR);
		consumers.get(info.id()).accept(processLog);
		inited = true;
	}
}
