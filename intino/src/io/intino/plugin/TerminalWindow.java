package io.intino.plugin;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import io.intino.plugin.build.AbstractArtifactFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.terminal.ShellTerminalWidget;
import org.jetbrains.plugins.terminal.TerminalView;

import java.io.IOException;

import static org.jetbrains.plugins.terminal.TerminalToolWindowFactory.TOOL_WINDOW_ID;

public class TerminalWindow {

	public static void runCommand(Project project, @NotNull String workingDir, String command) {
		org.jetbrains.plugins.terminal.TerminalView terminalView = TerminalView.getInstance(project);
		ToolWindow window = ToolWindowManager.getInstance(project).getToolWindow(TOOL_WINDOW_ID);
		if (window == null) return;
		try {
			final ShellTerminalWidget widget = terminalView.createLocalShellWidget(workingDir, "npm");
			widget.executeCommand(command);
			while (widget.hasRunningCommands()) {
				try {
					Thread.currentThread().wait(1000);
				} catch (InterruptedException e) {
				}
			}

		} catch (IOException e) {
			Logger.getInstance(AbstractArtifactFactory.class.getName()).warn("Cannot execute command", e);
		}
	}
}
