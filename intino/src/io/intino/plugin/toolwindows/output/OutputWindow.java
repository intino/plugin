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
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.uiDesigner.core.GridConstraints;
import io.intino.plugin.toolwindows.IntinoTopics;
import io.intino.plugin.toolwindows.remote.RemoteWindowFactory;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

public class OutputWindow {
	public static final String CLEAR = "##clear##";
	private final Project project;
	private final ConsoleView buildOutput;
	private JPanel myToolWindowContent;

	public OutputWindow(Project project) {
		this.project = project;
		this.buildOutput = createBuildView();
		subscribeToEvents(project);
	}

	private void subscribeToEvents(Project project) {
		project.getMessageBus().connect().subscribe(IntinoTopics.BUILD_CONSOLE, line -> ApplicationManager.getApplication().invokeLater(() -> {
			ToolWindow consoleWindow = RemoteWindowFactory.getInstance(project);
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
		return ConsoleViewContentType.NORMAL_OUTPUT;
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
		GridConstraints constraints = new GridConstraints();
		constraints.setFill(GridConstraints.FILL_BOTH);
		myToolWindowContent.add(ui, constraints);
		return buildView;
	}

	private ConsoleView createConsoleView() {
		TextConsoleBuilderFactory factory = TextConsoleBuilderFactory.getInstance();
		TextConsoleBuilder consoleBuilder = factory.createBuilder(project);
		return consoleBuilder.getConsole();
	}

	private void createUIComponents() {
		// TODO: place custom component creation code here
	}
}
