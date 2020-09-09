package io.intino.plugin.toolwindows.output;

import com.intellij.execution.filters.TextConsoleBuilder;
import com.intellij.execution.filters.TextConsoleBuilderFactory;
import com.intellij.execution.impl.ConsoleViewImpl;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.wm.ToolWindow;
import io.intino.alexandria.message.Message;
import io.intino.alexandria.message.MessageReader;
import io.intino.cesar.box.schemas.ProcessInfo;
import org.apache.commons.collections.IteratorUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class OutputsToolWindow {
	public static final String CLEAR = "##clear##";
	private final Project project;
	private JPanel myToolWindowContent;
	private JTabbedPane tabs;
	private final ConsoleView buildOutput;
	private final Map<String, Consumer<String>> consumers = new HashMap<>();

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

	public boolean existsOutputTab(String server) {
		return tabs.indexOfTab(server) > -1;
	}

	public void addProcess(String server, List<ProcessInfo> processes) {
		ConsoleView consoleView = createServerTab(server, processes);
		for (ProcessInfo info : processes) {
			consumers.put(info.id(), text -> {
				if (text.equals(CLEAR)) consoleView.clear();
				else printMessages(consoleView, text);
			});
		}
	}

	@NotNull
	private ConsoleView createServerTab(String server, List<ProcessInfo> processesInfo) {
		ConsoleViewImpl consoleView = (ConsoleViewImpl) createConsoleView();
		JComponent processesBox = processesCombo(processesInfo);
		JPanel container = new JPanel(new BorderLayout());
		container.add(processesBox, BorderLayout.NORTH);
		final JComponent ui = new RunContentDescriptor(consoleView, null, new JPanel(new BorderLayout()), server).getComponent();
		container.add(ui);
		JComponent consoleViewComponent = consoleView.getComponent();
		final DefaultActionGroup actionGroup = new DefaultActionGroup();
		actionGroup.add(new ListenLogAction(processesInfo, (ComboBox<Object>) processesBox.getComponent(0), project, consumers));
		actionGroup.add(new StartStopAction(processesInfo, (ComboBox<Object>) processesBox.getComponent(0), project));
		actionGroup.add(new DebugAction(processesInfo, (ComboBox<Object>) processesBox.getComponent(0), project));
		actionGroup.addAll(consoleView.createConsoleActions());
		final ActionToolbar toolbar = ActionManager.getInstance().createActionToolbar("IntinoConsole", actionGroup, false);
		toolbar.setTargetComponent(consoleViewComponent);
		ui.add(consoleViewComponent, BorderLayout.CENTER);
		ui.add(toolbar.getComponent(), BorderLayout.WEST);
		tabs.addTab(server, container);
		return consoleView;
	}

	@NotNull
	private JPanel processesCombo(List<ProcessInfo> processInfo) {
		ComboBox<Object> processesBox = new ComboBox<>();
		processInfo.stream().map(ProcessInfo::artifact).forEach(processesBox::addItem);
		JPanel container = new JPanel(new BorderLayout());
		container.add(processesBox);
		return container;
	}

	@NotNull
	private ConsoleView createBuildView() {
		ConsoleViewImpl buildView = (ConsoleViewImpl) createConsoleView();
		if (buildView.getEditor() != null && !buildView.getEditor().isDisposed())
			EditorFactory.getInstance().releaseEditor(buildView.getEditor());
		final RunContentDescriptor descriptor = new RunContentDescriptor(buildView, null, new JPanel(new BorderLayout()), "Build");
		final JComponent ui = descriptor.getComponent();
		JComponent consoleViewComponent = buildView.getComponent();
		final DefaultActionGroup actionGroup = new DefaultActionGroup();
		actionGroup.addAll(buildView.createConsoleActions());
		final ActionToolbar toolbar = ActionManager.getInstance().createActionToolbar("BuildConsole", actionGroup, false);
		toolbar.setTargetComponent(consoleViewComponent);
		ui.add(consoleViewComponent, BorderLayout.CENTER);
		ui.add(toolbar.getComponent(), BorderLayout.WEST);
		tabs.addTab("build", ui);
		return buildView;

	}

	private ConsoleView createConsoleView() {
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
			return IteratorUtils.toList(new MessageReader(text).iterator());
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
