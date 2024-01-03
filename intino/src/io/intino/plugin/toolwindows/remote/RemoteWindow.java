package io.intino.plugin.toolwindows.remote;

import com.intellij.execution.filters.TextConsoleBuilder;
import com.intellij.execution.filters.TextConsoleBuilderFactory;
import com.intellij.execution.impl.ConsoleViewImpl;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import io.intino.alexandria.logger.Logger;
import io.intino.alexandria.message.Message;
import io.intino.alexandria.message.MessageReader;
import io.intino.cesar.box.schemas.Application;
import io.intino.plugin.cesar.CesarAccessor;
import io.intino.plugin.cesar.CesarInfo;
import io.intino.plugin.cesar.CesarServerInfoDownloader;
import io.intino.plugin.toolwindows.remote.remoteactions.*;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.StreamSupport;

import static io.intino.plugin.toolwindows.IntinoTopics.REMOTE_CONSOLE;

public class RemoteWindow {
	public static final String CLEAR = "##clear##";
	private final Project project;
	private final CesarAccessor cesarAccessor;
	private JPanel myToolWindowContent;
	private JTabbedPane tabs;

	public RemoteWindow(Project project) {
		this.project = project;
		cesarAccessor = new CesarAccessor(project);
		subscribeToEvents();
		myToolWindowContent.addAncestorListener(new AncestorListener() {
			@Override
			public void ancestorAdded(AncestorEvent ancestorEvent) {
				tabs.removeAll();
				reload();
			}

			@Override
			public void ancestorRemoved(AncestorEvent ancestorEvent) {
			}

			@Override
			public void ancestorMoved(AncestorEvent ancestorEvent) {
			}
		});
	}

	private void subscribeToEvents() {
		project.getMessageBus().connect().subscribe(REMOTE_CONSOLE, this::reload);
	}

	public void reload() {
		ApplicationManager.getApplication().invokeAndWait(() -> {
					new CesarServerInfoDownloader().download(project);
					CesarInfo.getSafeInstance(project).serversInfo().values().forEach(this::refreshServerView);
				}
		);
	}

	public JPanel content() {
		return myToolWindowContent;
	}

	private void refreshServerView(CesarInfo.ServerInfo serverInfo) {
		if (tabs.indexOfTab(serverInfo.name()) < 0) createServerView(serverInfo);
		else refreshServerView((JPanel) tabs.getComponentAt(tabs.indexOfTab(serverInfo.name())), serverInfo);
	}

	private void refreshServerView(JPanel tab, CesarInfo.ServerInfo serverInfo) {
		JComboBox<String> comboBox = (JComboBox<String>) ((JPanel) tab.getComponent(0)).getComponent(0);
		comboBox.removeAllItems();
		serverInfo.processes().stream().map(Application::artifact).forEach(comboBox::addItem);
		if (comboBox.getItemCount() > 0) comboBox.setSelectedIndex(0);
		comboBox.repaint();
	}

	private void createServerView(CesarInfo.ServerInfo server) {
		ConsoleViewImpl consoleView = (ConsoleViewImpl) createConsoleView();
		JComponent processesBoxPanel = createProcessesCombo(server.processes(), e -> {
			new CesarServerInfoDownloader().download(project);
			refreshServerView(CesarInfo.getSafeInstance(project).serversInfo().get(server.name()));
		});
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
		operationActions.add(new ConfigureSshSessionAction(server, cesarAccessor));
		operationActions.add(new OpenSshSessionAction(server, cesarAccessor));
		((ComboBox<Object>) processesBoxPanel.getComponent(0)).addItemListener(e -> {
			if (e.getStateChange() == ItemEvent.DESELECTED)
				operationActions.forEach(a -> a.onApplicationChange(null));
			else if (e.getStateChange() == ItemEvent.SELECTED) {
				String newProcess = e.getItemSelectable().getSelectedObjects()[0].toString();
				List<Application> processes = CesarInfo.getSafeInstance(project).serversInfo().get(server.name()).processes();
				operationActions.forEach(IntinoConsoleAction::onChanging);
				Application processInfo = processes.stream().filter(p -> p.artifact().equals(newProcess)).findFirst().orElse(null);
				if (processInfo == null) return;
				new Thread(() -> {
					Application app = cesarAccessor.application(server.name(), processInfo.id());
					operationActions.forEach(a -> a.onApplicationChange(app));
				}).start();
			}
		});
		final DefaultActionGroup westGroup = getWestGroup(operationActions);
		final DefaultActionGroup eastGroup = new DefaultActionGroup();
		eastGroup.add(new FilterLogLevelAction((ListenLogAction) operationActions.get(0)));
		eastGroup.addSeparator();
		eastGroup.addAll(consoleView.createConsoleActions());
		final ActionToolbar westToolbar = ActionManager.getInstance().createActionToolbar("WestToolbar", westGroup, false);
		final ActionToolbar eastToolbar = ActionManager.getInstance().createActionToolbar("EastToolbar", eastGroup, false);
		westToolbar.setTargetComponent(consoleViewComponent);
		eastToolbar.setTargetComponent(consoleViewComponent);
		ui.add(consoleViewComponent, BorderLayout.CENTER);
		ui.add(westToolbar.getComponent(), BorderLayout.WEST);
		ui.add(eastToolbar.getComponent(), BorderLayout.EAST);
		tabs.addTab(server.name(), container);
	}

	@NotNull
	private static DefaultActionGroup getWestGroup(List<IntinoConsoleAction> operationActions) {
		final DefaultActionGroup westGroup = new DefaultActionGroup();
		westGroup.add((AnAction) operationActions.get(0));
		westGroup.addSeparator();
		operationActions.subList(1, operationActions.size() - 2).forEach(a -> westGroup.add((AnAction) a));
		return westGroup;
	}

	private ConsoleView createConsoleView() {
		TextConsoleBuilderFactory factory = TextConsoleBuilderFactory.getInstance();
		TextConsoleBuilder consoleBuilder = factory.createBuilder(project);
		return consoleBuilder.getConsole();
	}

	@NotNull
	private JPanel createProcessesCombo(List<Application> processInfo, ActionListener refreshListener) {
		ComboBox<Object> processesBox = new ComboBox<>();
		JButton refresh = new JButton(AllIcons.Actions.Refresh);
		refresh.addActionListener(refreshListener);
		if (processInfo != null) processInfo.stream().map(Application::artifact).forEach(processesBox::addItem);
		JPanel container = new JPanel(new BorderLayout());
		container.add(processesBox);
		container.add(refresh, BorderLayout.EAST);
		return container;
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
		compactedMessage = compactedMessage
				.replaceFirst("ts: ", "")
				.replaceFirst("source: ", "")
				.replaceFirst("message: ", "");
		return compactedMessage.substring(compactedMessage.indexOf("\n") + 1);
	}

	private List<Message> toInl(String text) {
		try (MessageReader reader = new MessageReader(text)) {
			return StreamSupport
					.stream(reader.spliterator(), false)
					.toList();
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
