package io.intino.plugin.project;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import io.intino.cesar.box.schemas.ProcessInfo;
import io.intino.ness.inl.Message;
import io.intino.plugin.toolwindows.output.ConsoleWindowComponent;
import io.intino.plugin.toolwindows.output.OutputsToolWindow;

import java.util.List;
import java.util.function.Consumer;

public class ProcessOutputLoader {
	private Project project;
	private final CesarAccessor cesarAccessor;

	public ProcessOutputLoader(Project project) {
		this.project = project;
		this.cesarAccessor = new CesarAccessor(project);
	}

	void loadOutputs(List<ProcessInfo> processInfos) {
		ApplicationManager.getApplication().invokeLater(() -> {
			OutputsToolWindow window = ConsoleWindowComponent.getInstance(project).outputsToolWindow();
			if (window == null) return;
			for (ProcessInfo info : processInfos) {
				Consumer<Message> consumer = window.addProcessOutputTab(info.artifact());
//				consumeLog(consumer, info.id());
			}
		});
	}

	private void consumeLog(Consumer<Message> consumer) {
//		cesarAccessor.accessor().getProcessLog(project.getName(), )
	}
}
