package io.intino.plugin;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.terminal.ui.TerminalWidget;
import kotlin.Unit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.terminal.TerminalToolWindowManager;

import java.util.List;
import java.util.stream.Collectors;

import static org.jetbrains.plugins.terminal.TerminalToolWindowFactory.TOOL_WINDOW_ID;

public class TerminalWindow {
	public static void openSSh(Project project, String user, String server, int port, List<Tunnel> tunnels) {
		ToolWindow window = ToolWindowManager.getInstance(project).getToolWindow(TOOL_WINDOW_ID);
		if (window == null) return;
		ApplicationManager.getApplication().invokeAndWait(() -> {
			@NotNull TerminalWidget widget = TerminalToolWindowManager.getInstance(project).createNewSession();
			widget.getTerminalTitle().change(state -> {
				state.setApplicationTitle("ssh " + server);
				state.setUserDefinedTitle("ssh " + server);
				return Unit.INSTANCE;
			});
			widget.sendCommandToExecute(buildSshChain(user, server, port, tunnels));
		});
	}

	public static void runCommand(Project project, String workingDir, String title, String command) {
		ToolWindow window = ToolWindowManager.getInstance(project).getToolWindow(TOOL_WINDOW_ID);
		if (window == null) return;
		run(project, workingDir, title, command);
	}

	private static String buildSshChain(String user, String server, int port, List<Tunnel> tunnels) {
		return "ssh " + tunnels.stream().map(Object::toString).collect(Collectors.joining(" ")) + user + "@" + server + "-p" + port;
	}

	private static void run(Project project, String workingDir, String title, String command) {
		final TerminalWidget[] widget = new TerminalWidget[1];
		ApplicationManager.getApplication().invokeAndWait(() -> {
			widget[0] = TerminalToolWindowManager.getInstance(project).createNewSession();
			widget[0].sendCommandToExecute(withWorkingDir(workingDir, command));
		});
		widget[0].getTerminalTitle().change(state -> {
			state.setApplicationTitle(title);
			state.setUserDefinedTitle(title);
			return Unit.INSTANCE;
		});
		try {
			Thread.sleep(5000);
			while (!widget[0].isCommandRunning()) {
				Thread.sleep(100);
			}
			while (widget[0].isCommandRunning()) {
				Thread.sleep(100);
			}
		} catch (InterruptedException ignored) {
		}

	}

	private static String withWorkingDir(String workingDir, String command) {
		if (workingDir == null || workingDir.isBlank()) return command;
		return "cd " + shellQuote(workingDir) + " && " + command;
	}

	private static String shellQuote(String value) {
		return "'" + value.replace("'", "'\"'\"'") + "'";
	}

	public static class Tunnel {
		public enum Location {Local, Remote}

		String sourcePort;
		String destination;
		Location location;

		public Tunnel(String sourcePort, String destination, Location location) {
			this.sourcePort = sourcePort;
			this.destination = destination;
			this.location = location;
		}

		@Override
		public String toString() {
			return location.equals(Location.Local) ? "-L " : "-R " + sourcePort + ":" + destination;
		}
	}
}
