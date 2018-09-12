package io.intino.plugin.toolwindows.output;

import com.intellij.execution.executors.DefaultRunExecutor;
import com.intellij.execution.filters.TextConsoleBuilder;
import com.intellij.execution.filters.TextConsoleBuilderFactory;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.execution.ui.actions.CloseAction;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import io.intino.konos.alexandria.Inl;
import io.intino.ness.inl.Message;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
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
		project.getMessageBus().connect().subscribe(IntinoTopics.BUILD_CONSOLE, line -> ApplicationManager.getApplication().invokeLater(() -> {
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

	public boolean existsOutputTab(String title) {
		return tabs.indexOfTab(title) > -1;
	}

	public Consumer<String> addProcessOutputTab(String title) {
		ConsoleView consoleView = createConsoleView(title);
		return text -> {
			List<Message> messages;
			if (!(messages = toInl(text)).isEmpty()) {
				for (Message message : messages) {
					String level = levelFrom(message);
					consoleView.print("\n\n" + compactLog(message), level.equalsIgnoreCase("error") ? ConsoleViewContentType.ERROR_OUTPUT : ConsoleViewContentType.NORMAL_OUTPUT);
				}
			} else consoleView.print("\n" + text, ConsoleViewContentType.NORMAL_OUTPUT);
		};
	}

	private String compactLog(Message message) {
		String level = message.remove("level").toString();
		return level.substring(level.indexOf("\n") + 1);
	}

	private List<Message> toInl(String text) {
		try {
			return Inl.load(text);
		} catch (Exception e) {
			return Collections.emptyList();
		}
	}

	private String levelFrom(Message message) {
		String level = message.get("level");
		return level == null ? "INFO" : level;
	}

	@NotNull
	private ConsoleView createConsoleView(String title) {
		ConsoleView consoleView = createConsoleView();
		processOutputs.add(consoleView);
		final RunContentDescriptor descriptor = new RunContentDescriptor(consoleView, null, new JPanel(new BorderLayout()), title);
		final JComponent ui = descriptor.getComponent();
		JComponent consoleViewComponent = consoleView.getComponent();
		final DefaultActionGroup actionGroup = new DefaultActionGroup();
		actionGroup.addAll(consoleView.createConsoleActions());
		actionGroup.add(new CloseAction(DefaultRunExecutor.getRunExecutorInstance(), descriptor, project) {
			@Override
			public void actionPerformed(@NotNull AnActionEvent e) {
				tabs.remove(ui);
				consoleView.dispose();
				processOutputs.remove(consoleView);
				super.actionPerformed(e);
			}
		});
		final ActionToolbar toolbar = ActionManager.getInstance().createActionToolbar("IntinoConsole", actionGroup, false);
		toolbar.setTargetComponent(consoleViewComponent);
		ui.add(consoleViewComponent, BorderLayout.CENTER);
		ui.add(toolbar.getComponent(), BorderLayout.WEST);
		tabs.addTab(title, ui);
		return consoleView;
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
