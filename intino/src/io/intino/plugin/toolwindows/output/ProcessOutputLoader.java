package io.intino.plugin.toolwindows.output;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import io.intino.Configuration;
import io.intino.cesar.box.schemas.ProcessInfo;

import java.util.List;

public class ProcessOutputLoader {
	private final Project project;

	public ProcessOutputLoader(Project project) {
		this.project = project;
	}

	public void loadProcessReference(String server, Configuration.Server.Type type, List<ProcessInfo> processInfos) {
		ApplicationManager.getApplication().invokeLater(() -> {
			ConsoleWindowComponent instance = ConsoleWindowComponent.getInstance(project);
			if (instance == null) return;
			OutputsToolWindow window = instance.outputsToolWindow();
			if (window == null) return;
			if (!window.existsOutputTab(server)) window.addProcess(server, type, processInfos);
		});
	}
}