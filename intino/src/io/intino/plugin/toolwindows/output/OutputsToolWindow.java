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
import io.intino.alexandria.message.Message;
import io.intino.alexandria.message.MessageReader;
import io.intino.cesar.box.schemas.ProcessInfo;
import org.apache.commons.collections.IteratorUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.*;
import java.util.function.Consumer;

public class OutputsToolWindow {
	public static final String CLEAR = "##clear##";
	private Project project;
	private JPanel myToolWindowContent;
	private JTabbedPane tabs;
	private ConsoleView buildOutput;
	private List<ConsoleView> processOutputs = new ArrayList<>();
	private Map<String, Consumer<String>> consumers = new HashMap<>();

	public OutputsToolWindow(Project project) {
		this.project = project;
		this.buildOutput = createBuildView();
		subscribeBuildConsole(project);
	}

	private void subscribeBuildConsole(Project project) {
		project.getMessageBus().connect().subscribe(IntinoTopics.BUILD_CONSOLE, line -> ApplicationManager.getApplication().invokeLater(() -> {
			ToolWindow consoleWindow = ConsoleWindowComponent.getInstance(project).consoleWindow();
			if (!consoleWindow.isVisible()) {
				consoleWindow.show(null);
				buildOutput.scrollTo(buildOutput.getContentSize());
			}
			if (line.equals(CLEAR)) buildOutput.clear();
			else buildOutput.print("\n" + line, contentType(line));
		}));
	}

	private ConsoleViewContentType contentType(String line) {
		if (line.trim().toUpperCase().startsWith("[ERROR")) return ConsoleViewContentType.LOG_ERROR_OUTPUT;
		if (line.trim().toUpperCase().startsWith("[WARN")) return ConsoleViewContentType.LOG_WARNING_OUTPUT;
		else return ConsoleViewContentType.NORMAL_OUTPUT;
	}

	public JPanel content() {
		return myToolWindowContent;
	}

	public boolean existsOutputTab(ProcessInfo info) {
		return tabs.indexOfTab(displayOf(info)) > -1;
	}

	public void addProcessOutputTab(ProcessInfo processInfo) {
		ConsoleView consoleView = createRemoteProcessView(processInfo);
		consumers.put(processInfo.id(), text -> {
			if (text.equals(CLEAR)) consoleView.clear();
			else printMessages(consoleView, text);
		});
	}

	@NotNull
	private ConsoleView createRemoteProcessView(ProcessInfo info) {
		ConsoleView consoleView = createRemoteProcessView();
		processOutputs.add(consoleView);
		String displayName = displayOf(info);
		final RunContentDescriptor descriptor = new RunContentDescriptor(consoleView, null, new JPanel(new BorderLayout()), displayName);
		final JComponent ui = descriptor.getComponent();
		JComponent consoleViewComponent = consoleView.getComponent();
		final DefaultActionGroup actionGroup = new DefaultActionGroup();
		actionGroup.add(new SyncLogAction(info, descriptor, project, consumers));
		actionGroup.add(new StartAction(info, descriptor, project));
		actionGroup.add(new DebugAction(info, descriptor, project));
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
		tabs.addTab(displayName, ui);
		return consoleView;
	}

	@NotNull
	private ConsoleView createBuildView() {
		ConsoleView consoleView = createRemoteProcessView();
		final RunContentDescriptor descriptor = new RunContentDescriptor(consoleView, null, new JPanel(new BorderLayout()), "build");
		final JComponent ui = descriptor.getComponent();
		JComponent consoleViewComponent = consoleView.getComponent();
		final DefaultActionGroup actionGroup = new DefaultActionGroup();
		actionGroup.addAll(consoleView.createConsoleActions());
		final ActionToolbar toolbar = ActionManager.getInstance().createActionToolbar("BuildConsole", actionGroup, false);
		toolbar.setTargetComponent(consoleViewComponent);
		ui.add(consoleViewComponent, BorderLayout.CENTER);
		ui.add(toolbar.getComponent(), BorderLayout.WEST);
		tabs.addTab("build", ui);
		return consoleView;
	}

	@NotNull
	private String displayOf(ProcessInfo info) {
		return info.server().name() + " : " + info.artifact();
	}


	private ConsoleView createRemoteProcessView() {
		TextConsoleBuilderFactory factory = TextConsoleBuilderFactory.getInstance();
		TextConsoleBuilder consoleBuilder = factory.createBuilder(project);
		return consoleBuilder.getConsole();
	}

	private void printMessages(ConsoleView consoleView, String text) {
		List<Message> messages;
		if (text.startsWith("[") && !(messages = toInl(text)).isEmpty())
			for (Message message : messages) {
				String level = levelFrom(message);
				consoleView.print("\n\n" + compactLog(message), level(level.trim()));
			}
		else consoleView.print("\n" + text, ConsoleViewContentType.NORMAL_OUTPUT);
	}

	private ConsoleViewContentType level(String level) {
		if (level.trim().toLowerCase().startsWith("error")) return ConsoleViewContentType.ERROR_OUTPUT;
		if (level.trim().toLowerCase().startsWith("warn")) return ConsoleViewContentType.LOG_WARNING_OUTPUT;
		else return ConsoleViewContentType.NORMAL_OUTPUT;
	}

	private String compactLog(Message message) {
		String compactedMessage = message.remove("level").toString();
		return compactedMessage.substring(compactedMessage.indexOf("\n") + 1);
	}

	@SuppressWarnings("unchecked")
	private List<Message> toInl(String text) {
		try {
			return IteratorUtils.toList(new MessageReader(new ByteArrayInputStream(text.getBytes())).iterator());
		} catch (Exception e) {
			return Collections.emptyList();
		}
	}

	private String levelFrom(Message message) {
		String level = message.get("level").data();
		if (level != null) return level;
		else return message.type();
	}
}
