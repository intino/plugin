package io.intino.plugin.project;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import io.intino.cesar.box.schemas.ProcessInfo;
import io.intino.plugin.toolwindows.output.ConsoleWindowComponent;
import io.intino.plugin.toolwindows.output.OutputsToolWindow;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.List;

public class ProcessOutputLoader {
	private Project project;

	public ProcessOutputLoader(Project project) {
		this.project = project;
	}

	void loadOutputs(List<ProcessInfo> processInfos) {
		ApplicationManager.getApplication().invokeLater(() -> {
			ConsoleWindowComponent instance = ConsoleWindowComponent.getInstance(project);
			if (instance == null) return;
			OutputsToolWindow window = instance.outputsToolWindow();
			if (window == null) return;
			processInfos.stream().sorted(Comparator.comparing(this::id)).filter(p -> !window.existsOutputTab(p)).forEach(window::addProcessOutputTab);
		});
	}

	@NotNull
	private String id(ProcessInfo o1) {
		return o1.server().name() + " : " + o1.artifact();
	}
}