package io.intino.plugin;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.terminal.ui.TerminalWidget;
import kotlin.Unit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.terminal.ShellTerminalWidget;
import org.jetbrains.plugins.terminal.TerminalToolWindowManager;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import static org.jetbrains.plugins.terminal.TerminalToolWindowFactory.TOOL_WINDOW_ID;

public class TerminalWindow {
	private static final Logger LOG = Logger.getInstance(TerminalWindow.class.getName());

	public static void openSSh(Project project, String user, String server, int port, List<Tunnel> tunnels) {
		ToolWindow window = ToolWindowManager.getInstance(project).getToolWindow(TOOL_WINDOW_ID);
		if (window == null) return;
		ApplicationManager.getApplication().invokeAndWait(() -> {
			@NotNull TerminalWidget widget = TerminalToolWindowManager.getInstance(project)
					.createShellWidget(System.getProperty("user.home"), "ssh " + server, true, true);
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
		final ShellTerminalWidget[] widget = new ShellTerminalWidget[1];
		ApplicationManager.getApplication().invokeAndWait(() -> {
			widget[0] = ShellTerminalWidget.toShellJediTermWidgetOrThrow(TerminalToolWindowManager.getInstance(project).createShellWidget(workingDir, command, true, true));
			run(command, widget[0]);
		});
		widget[0].asNewWidget().getTerminalTitle().change(state -> {
			state.setApplicationTitle(title);
			state.setUserDefinedTitle(title);
			return Unit.INSTANCE;
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
			LOG.error(e);
		}
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
