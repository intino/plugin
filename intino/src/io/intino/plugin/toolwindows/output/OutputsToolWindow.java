package io.intino.plugin.toolwindows.output;

import com.google.gson.JsonParser;
import com.intellij.execution.executors.DefaultRunExecutor;
import com.intellij.execution.filters.TextConsoleBuilder;
import com.intellij.execution.filters.TextConsoleBuilderFactory;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.execution.ui.actions.CloseAction;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import io.intino.alexandria.exceptions.BadRequest;
import io.intino.alexandria.exceptions.Unknown;
import io.intino.alexandria.inl.InlReader;
import io.intino.alexandria.inl.Message;
import io.intino.cesar.box.schemas.ProcessInfo;
import io.intino.plugin.project.CesarAccessor;
import io.intino.plugin.project.ProcessOutputLoader;
import org.apache.commons.collections.IteratorUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.io.ByteArrayInputStream;
import java.util.*;
import java.util.List;
import java.util.function.Consumer;

import static com.intellij.icons.AllIcons.General.Run;
import static com.intellij.icons.AllIcons.Ide.Macro.Recording_stop;

public class OutputsToolWindow {

	static Project project;
	private JPanel myToolWindowContent;
	private JTabbedPane tabs;
	private ConsoleView buildOutput;
	private List<ConsoleView> processOutputs = new ArrayList<>();
	private Map<String, Consumer<String>> consumers = new HashMap<>();

	public OutputsToolWindow(Project project) {
		OutputsToolWindow.project = project;
		this.buildOutput = createBuildView();
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

	public boolean existsOutputTab(ProcessInfo info) {
		return tabs.indexOfTab(displayOf(info)) > -1;
	}

	public void addProcessOutputTab(ProcessInfo processInfo) {
		ConsoleView consoleView = createConsoleView(processInfo);
		consumers.put(processInfo.id(), text -> {
			List<Message> messages;
			if (text.startsWith("[") && !(messages = toInl(text)).isEmpty())
				for (Message message : messages) {
					String level = levelFrom(message);
					consoleView.print("\n\n" + compactLog(message), level.equalsIgnoreCase("error") ? ConsoleViewContentType.ERROR_OUTPUT : ConsoleViewContentType.NORMAL_OUTPUT);
				}
			else consoleView.print("\n" + text, ConsoleViewContentType.NORMAL_OUTPUT);
		});
	}

	private String compactLog(Message message) {
		String level = message.remove("level").toString();
		return level.substring(level.indexOf("\n") + 1);
	}

	private List<Message> toInl(String text) {
		try {
			return IteratorUtils.toList(new InlReader(new ByteArrayInputStream(text.getBytes())));
		} catch (Exception e) {
			return Collections.emptyList();
		}
	}

	private String levelFrom(Message message) {
		String level = message.get("level");
		return level == null ? "INFO" : level;
	}

	@NotNull
	private ConsoleView createConsoleView(ProcessInfo info) {
		ConsoleView consoleView = createConsoleView();
		processOutputs.add(consoleView);
		String displayName = displayOf(info);
		final RunContentDescriptor descriptor = new RunContentDescriptor(consoleView, null, new JPanel(new BorderLayout()), displayName);
		final JComponent ui = descriptor.getComponent();
		JComponent consoleViewComponent = consoleView.getComponent();
		final DefaultActionGroup actionGroup = new DefaultActionGroup();
		actionGroup.add(new SyncAction(info, descriptor));
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
		ConsoleView consoleView = createConsoleView();
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
		return info.runtime().serverName() + " : " + info.artifact();
	}


	private ConsoleView createConsoleView() {
		TextConsoleBuilderFactory factory = TextConsoleBuilderFactory.getInstance();
		TextConsoleBuilder consoleBuilder = factory.createBuilder(project);
		return consoleBuilder.getConsole();

	}

	private class SyncAction extends AnAction implements DumbAware {

		private final ProcessInfo info;
		private final CesarAccessor cesarAccessor;
		boolean inited = false;
		boolean listening = false;
		private RunContentDescriptor myContentDescriptor;

		public SyncAction(ProcessInfo info, RunContentDescriptor contentDescriptor) {
			this.info = info;
			myContentDescriptor = contentDescriptor;
			cesarAccessor = new CesarAccessor(project);
			final Presentation templatePresentation = getTemplatePresentation();
			templatePresentation.setIcon(Run);
			templatePresentation.setText("Run");
			templatePresentation.setDescription("Run");
		}

		@Override
		public void actionPerformed(@NotNull AnActionEvent e) {
			if (inited) {
				stop();
				inited = false;
				return;
			}
			initLog();
			listenLog();
			update(e);
		}

		@Override
		public void update(AnActionEvent e) {
			super.update(e);
			e.getPresentation().setIcon(inited ? Recording_stop : Run);
		}

		public void stop() {
			cesarAccessor.accessor().stopListenLog();
		}

		private void listenLog() {
			try {
				Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
				cesarAccessor.accessor().listenLog(project.getName(), text -> {
					text = new JsonParser().parse(text).getAsJsonObject().get("name").getAsString();
					int endIndex = text.indexOf("#");
					if (endIndex < 0) return;
					int messageStart = text.indexOf("#", endIndex + 1);
					Consumer<String> consumer = consumers.get(text.substring(0, messageStart));
					if (consumer != null) consumer.accept(text.substring(messageStart + 1));
				});
			} catch (io.intino.alexandria.exceptions.Unknown e) {
				Logger.getInstance(ProcessOutputLoader.class.getName()).info(e.getMessage(), e);
			}
		}

		private void initLog() {
			String processLog = null;
			try {
				CesarAccessor cesarAccessor = new CesarAccessor(project);
				processLog = cesarAccessor.accessor().getProcessLog(project.getName(), info.id()).replace("\\n", "\n");
			} catch (BadRequest | Unknown e) {
				Logger.getInstance(ProcessOutputLoader.class.getName()).error(e.getMessage(), e);
			}
			consumers.get(info.id()).accept(processLog);
			inited = true;
		}

	}
}
