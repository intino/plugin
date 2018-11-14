package io.intino.plugin.project;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import io.intino.cesar.box.schemas.ProcessInfo;
import io.intino.konos.alexandria.exceptions.BadRequest;
import io.intino.konos.alexandria.exceptions.Unknown;
import io.intino.plugin.toolwindows.output.ConsoleWindowComponent;
import io.intino.plugin.toolwindows.output.OutputsToolWindow;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class ProcessOutputLoader {
	private Project project;
	private final CesarAccessor cesarAccessor;
	private boolean listening = false;

	public ProcessOutputLoader(Project project) {
		this.project = project;
		this.cesarAccessor = new CesarAccessor(project);
	}

	void loadOutputs(List<ProcessInfo> processInfos) {
		ApplicationManager.getApplication().invokeLater(() -> {
			ConsoleWindowComponent instance = ConsoleWindowComponent.getInstance(project);
			if (instance == null) return;
			OutputsToolWindow window = instance.outputsToolWindow();
			if (window == null) return;
			Map<String, Consumer<String>> consumers = processInfos.stream().filter(p -> !window.existsOutputTab(p.artifact())).collect(Collectors.toMap(ProcessInfo::id, info -> window.addProcessOutputTab(info.artifact()), (a, b) -> b));
			for (String artifactId : consumers.keySet()) initLog(consumers, artifactId);
			if (!listening) consumeLog(consumers);
		});
	}

	private void consumeLog(Map<String, Consumer<String>> consumers) {
		try {
			Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
			cesarAccessor.accessor().listenLog(project.getName(), text -> {
				int endIndex = text.indexOf("#");
				if (endIndex < 0) return;
				Consumer<String> consumer = consumers.get(text.substring(0, endIndex));
				if (consumer != null) consumer.accept(text.substring(endIndex + 1));
			});
			listening = true;
		} catch (Unknown e) {
			Logger.getInstance(ProcessOutputLoader.class.getName()).info(e.getMessage(), e);
		}
	}

	private void initLog(Map<String, Consumer<String>> consumers, String artifactId) {
		String processLog = null;
		try {
			processLog = cesarAccessor.accessor().getProcessLog(project.getName(), artifactId).replace("\\n", "\n");
		} catch (BadRequest | Unknown e) {
			Logger.getInstance(ProcessOutputLoader.class.getName()).error(e.getMessage(), e);
		}
		consumers.get(artifactId).accept(processLog);
	}
}