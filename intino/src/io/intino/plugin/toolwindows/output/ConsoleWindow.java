package io.intino.plugin.toolwindows.output;

import com.intellij.execution.filters.TextConsoleBuilder;
import com.intellij.execution.filters.TextConsoleBuilderFactory;
import com.intellij.execution.impl.ConsoleViewImpl;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.wm.ToolWindow;
import io.intino.alexandria.logger.Logger;
import io.intino.alexandria.message.Message;
import io.intino.alexandria.message.MessageReader;
import io.intino.cesar.box.schemas.ProcessInfo;
import io.intino.cesar.box.schemas.ProcessStatus;
import io.intino.plugin.cesar.CesarAccessor;
import io.intino.plugin.cesar.CesarInfo;
import io.intino.plugin.toolwindows.output.remoteactions.DebugAction;
import io.intino.plugin.toolwindows.output.remoteactions.ListenLogAction;
import io.intino.plugin.toolwindows.output.remoteactions.RestartAction;
import io.intino.plugin.toolwindows.output.remoteactions.StartStopAction;
import org.apache.commons.collections.IteratorUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.util.List;
import java.util.*;
import java.util.function.Consumer;

public class ConsoleWindow {
	public static final String CLEAR = "##clear##";
	private final Project project;
	private JPanel myToolWindowContent;
	private JTabbedPane tabs;
	private final ConsoleView buildOutput;
	private final List<ConsoleView> remoteConsoleViews;

	private final Map<String, Consumer<Log>> consoleConsumers = new HashMap<>();
	private final CesarAccessor cesarAccessor;

	public ConsoleWindow(Project project) {
		this.project = project;
		this.buildOutput = createBuildView();
		remoteConsoleViews = new ArrayList<>();
		cesarAccessor = new CesarAccessor(project);
		reload(project);
		subscribeToEvents(project);
	}

	private void subscribeToEvents(Project project) {
		project.getMessageBus().connect().subscribe(IntinoTopics.BUILD_CONSOLE, line -> ApplicationManager.getApplication().invokeLater(() -> {
			ToolWindow consoleWindow = ConsoleWindowFactory.getInstance(project);
			if (!consoleWindow.isVisible()) {
				consoleWindow.show(null);
				buildOutput.scrollTo(buildOutput.getContentSize());
			}
			if (line.equals(CLEAR)) buildOutput.clear();
			else buildOutput.print("\n" + line, contentType(line));
		}));
		project.getMessageBus().connect().subscribe(IntinoTopics.REMOTE_CONSOLE, () -> reload(project));
	}

	public void reload(Project project) {
		CesarInfo.getSafeInstance(project).serversInfo().values().forEach(this::refreshServerView);
	}

	private ConsoleViewContentType contentType(String line) {
		if (line.trim().toUpperCase().startsWith("[ERROR")) return ConsoleViewContentType.LOG_ERROR_OUTPUT;
		if (line.trim().toUpperCase().startsWith("[WARN")) return ConsoleViewContentType.LOG_WARNING_OUTPUT;
		else return ConsoleViewContentType.NORMAL_OUTPUT;
	}

	public JPanel content() {
		return myToolWindowContent;
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

	private void refreshServerView(CesarInfo.ServerInfo serverInfo) {
		if (tabs.indexOfTab(serverInfo.name()) < 0) createServerView(serverInfo);
		else refreshServerView((JPanel) tabs.getComponentAt(tabs.indexOfTab(serverInfo.name())), serverInfo);
	}

	private void refreshServerView(JPanel tab, CesarInfo.ServerInfo serverInfo) {
		JComboBox<String> comboBox = (JComboBox<String>) ((JPanel) tab.getComponent(0)).getComponent(0);
		comboBox.removeAllItems();
		serverInfo.processes().stream().map(ProcessInfo::artifact).forEach(comboBox::addItem);
		if (comboBox.getItemCount() > 0) comboBox.setSelectedIndex(0);
		comboBox.repaint();
	}

	private void createServerView(CesarInfo.ServerInfo server) {
		ConsoleViewImpl consoleView = (ConsoleViewImpl) createConsoleView();
		JComponent processesBoxPanel = createProcessesCombo(server.processes());
		JPanel container = new JPanel(new BorderLayout());
		container.add(processesBoxPanel, BorderLayout.NORTH);
		final JComponent ui = new RunContentDescriptor(consoleView, null, new JPanel(new BorderLayout()), server.name()).getComponent();
		container.add(ui);
		JComponent consoleViewComponent = consoleView.getComponent();
		List<IntinoConsoleAction> operationActions = new ArrayList<>();
		Consumer<Log> consoleConsumer = log -> {
			if (log.text.equals(CLEAR)) consoleView.clear();
			else printLog(consoleView, log);
		};
		operationActions.add(new ListenLogAction(server.processes(), cesarAccessor, consoleConsumer));
		operationActions.add(new RestartAction(server.processes(), server.type(), cesarAccessor));
		operationActions.add(new StartStopAction(server.processes(), server.type(), cesarAccessor));
		operationActions.add(new DebugAction(server.processes(), server.type(), cesarAccessor));
		((ComboBox<Object>) processesBoxPanel.getComponent(0)).addItemListener(e -> {
			if (e.getStateChange() != ItemEvent.SELECTED) {
				operationActions.forEach(a -> a.onProcessChange(null, null));
			} else {
				String newProcess = e.getItemSelectable().getSelectedObjects()[0].toString();
				List<ProcessInfo> processes = CesarInfo.getSafeInstance(project).serversInfo().get(server.name()).processes();
				operationActions.forEach(IntinoConsoleAction::onChanging);
				new Thread(() -> {
					ProcessInfo processInfo = processes.stream().filter(p -> p.artifact().equals(newProcess)).findFirst().orElse(null);
					if (processInfo == null) return;
					ProcessStatus newProcessStatus = cesarAccessor.processStatus(server.name(), processInfo.id());
					operationActions.forEach(a -> a.onProcessChange(processInfo, newProcessStatus));
				}).start();
			}
		});
		final DefaultActionGroup westGroup = new DefaultActionGroup();
		final DefaultActionGroup eastGroup = new DefaultActionGroup();
		westGroup.add((AnAction) operationActions.get(0));
		westGroup.addSeparator();
		operationActions.subList(1, operationActions.size()).forEach(a -> westGroup.add((AnAction) a));
		eastGroup.add(new FilterLogLevelAction((ListenLogAction) operationActions.get(0)));
		eastGroup.addSeparator();
		eastGroup.addAll(consoleView.createConsoleActions());
		final ActionToolbar westToolbar = ActionManager.getInstance().createActionToolbar("IntinoConsole", westGroup, false);
		final ActionToolbar eastToolbar = ActionManager.getInstance().createActionToolbar("IntinoConsole", eastGroup, false);
		westToolbar.setTargetComponent(consoleViewComponent);
		ui.add(consoleViewComponent, BorderLayout.CENTER);
		ui.add(westToolbar.getComponent(), BorderLayout.WEST);
		ui.add(eastToolbar.getComponent(), BorderLayout.EAST);
		tabs.addTab(server.name(), container);
		remoteConsoleViews.add(consoleView);
		consoleConsumers.put(server.name(), consoleConsumer);

	}

	@NotNull
	private JPanel createProcessesCombo(List<ProcessInfo> processInfo) {
		ComboBox<Object> processesBox = new ComboBox<>();
		processInfo.stream().map(ProcessInfo::artifact).forEach(processesBox::addItem);
		JPanel container = new JPanel(new BorderLayout());
		container.add(processesBox);
		return container;
	}

	private ConsoleView createConsoleView() {
		TextConsoleBuilderFactory factory = TextConsoleBuilderFactory.getInstance();
		TextConsoleBuilder consoleBuilder = factory.createBuilder(project);
		return consoleBuilder.getConsole();
	}

	private void printLog(ConsoleView consoleView, Log log) {
		List<Message> messages;
		if (log.text.startsWith("[") && !(messages = toInl(log.text)).isEmpty())
			for (Message message : messages) {
				String level = levelFrom(message);
				boolean alexandriaLevel = isAlexandriaLevel(level);
				if (!alexandriaLevel || Logger.Level.valueOf(level).ordinal() <= log.level.ordinal())
					consoleView.print("\n" + compactLog(message), level(level.trim()));
			}
		else consoleView.print("\n" + log.text, ConsoleViewContentType.NORMAL_OUTPUT);
	}

	private boolean isAlexandriaLevel(String level) {
		try {
			Logger.Level.valueOf(level);
			return true;
		} catch (IllegalArgumentException e) {
			return false;
		}
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
