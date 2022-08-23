package io.intino.plugin;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import org.jetbrains.plugins.terminal.ShellTerminalWidget;
import org.jetbrains.plugins.terminal.TerminalView;

import java.io.IOException;

import static org.jetbrains.plugins.terminal.TerminalToolWindowFactory.TOOL_WINDOW_ID;

public class TerminalWindow {

	public static void runCommand(Project project, String workingDir, String command) {
		ToolWindow window = ToolWindowManager.getInstance(project).getToolWindow(TOOL_WINDOW_ID);
		if (window == null) return;
		run(project, workingDir, command);

	}

	private static void run(Project project, String workingDir, String command) {
		final ShellTerminalWidget[] widget = new ShellTerminalWidget[1];
		ApplicationManager.getApplication().invokeAndWait(() -> {
			widget[0] = TerminalView.getInstance(project).createLocalShellWidget(workingDir, "npm");
			run(command, widget[0]);
		});
		try {
			Thread.sleep(5000);
			while (!widget[0].hasRunningCommands()) {
				Thread.sleep(100);
			}
			while (widget[0].hasRunningCommands()) {
				Thread.sleep(100);
			}
		} catch (InterruptedException ignored) {
		}

	}

	private static void run(String command, ShellTerminalWidget widget) {
		try {
			widget.executeCommand(command);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private static void withSyncTask(String title, Runnable runnable, Project project) {
		ProgressManager.getInstance().runProcessWithProgressSynchronously(runnable, title, false, project);
	}
}
