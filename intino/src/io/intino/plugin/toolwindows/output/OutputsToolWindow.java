package io.intino.plugin.toolwindows.output;

import com.intellij.execution.filters.TextConsoleBuilder;
import com.intellij.execution.filters.TextConsoleBuilderFactory;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import io.intino.ness.inl.Message;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class OutputsToolWindow {

	static Project project;

	private JPanel myToolWindowContent;
	private JTabbedPane tabs;
	private JPanel outputPanel;
	private ConsoleView buildOutput;
	private List<ConsoleView> processOutputs = new ArrayList<>();


	public OutputsToolWindow(Project project) {
		OutputsToolWindow.project = project;
		project.getMessageBus().connect().subscribe(IntinoTopics.MAVEN, line -> ApplicationManager.getApplication().invokeLater(() -> {
			ToolWindow consoleWindow = ConsoleWindowComponent.getInstance(project).consoleWindow();
			if (!consoleWindow.isVisible()) {
				consoleWindow.show(null);
				buildOutput.scrollTo(buildOutput.getContentSize());
			}
			buildOutput.print("\n" + line, contentType(line));
		}));
	}

	private ConsoleViewContentType contentType(String line) {
		if (line.trim().startsWith("[ERROR]")) return ConsoleViewContentType.LOG_ERROR_OUTPUT;
		if (line.trim().startsWith("[WARNING]")) return ConsoleViewContentType.LOG_WARNING_OUTPUT;
		else return ConsoleViewContentType.NORMAL_OUTPUT;
	}

	public JPanel content() {
		return myToolWindowContent;
	}

	public Consumer<Message> addProcessOutputTab(String title) {
		ConsoleView consoleView = createConsoleView();
		processOutputs.add(consoleView);
		tabs.addTab(title, consoleView.getComponent());
		return message -> consoleView.print("\n" + message.toString(), message.get("level").equalsIgnoreCase("error") ? ConsoleViewContentType.ERROR_OUTPUT : ConsoleViewContentType.NORMAL_OUTPUT);
	}

	private ConsoleView createConsoleView() {
		TextConsoleBuilderFactory factory = TextConsoleBuilderFactory.getInstance();
		TextConsoleBuilder consoleBuilder = factory.createBuilder(project);
		return consoleBuilder.getConsole();

	}

	private void createUIComponents() {
		TextConsoleBuilderFactory factory = TextConsoleBuilderFactory.getInstance();
		TextConsoleBuilder consoleBuilder = factory.createBuilder(project);
		this.buildOutput = consoleBuilder.getConsole();
		outputPanel = (JPanel) buildOutput.getComponent();
	}
}
