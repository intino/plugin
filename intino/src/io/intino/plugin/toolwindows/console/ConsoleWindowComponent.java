package io.intino.plugin.toolwindows.console;

import com.intellij.execution.filters.TextConsoleBuilderFactory;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowAnchor;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import io.intino.plugin.IntinoIcons;
import org.jetbrains.annotations.NotNull;

public class ConsoleWindowComponent implements ProjectComponent {

	private static final String CONSOLE_WINDOW_ID = "Intino Console";
	private ToolWindow consoleWindow;
	private Project project;
	private ConsoleToolWindow consolePanel;
	private ConsoleView consoleView;

	public ConsoleWindowComponent(Project project) {
		this.project = project;
	}

	@Override
	public void projectOpened() {
		createToolWindows();
	}

	private void createToolWindows() {
		ToolWindowManager toolWindowManager = ToolWindowManager.getInstance(project);

		consolePanel = new ConsoleToolWindow(project);

		ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
		Content content = contentFactory.createContent(consolePanel.content(), "", false);
		consoleWindow = toolWindowManager.registerToolWindow(CONSOLE_WINDOW_ID, true, ToolWindowAnchor.BOTTOM);
		consoleWindow.getContentManager().addContent(content);
		consoleWindow.setIcon(IntinoIcons.INTINO_16);
		project.getMessageBus().connect().subscribe(IntinoTopics.MAVEN, line -> ApplicationManager.getApplication().invokeLater(() -> {
			if (!consoleWindow.isVisible()) consoleWindow.show(null);
			consolePanel.addText("\n" + line);

		}));
		consoleWindow.setAutoHide(false);
		consoleView = TextConsoleBuilderFactory.getInstance().createBuilder(project).getConsole();
	}

	@Override
	public void projectClosed() {
		consoleView.dispose();
		consolePanel = null;
		consoleWindow = null;
		project = null;
	}

	@Override
	public void initComponent() {

	}

	@Override
	public void disposeComponent() {

	}

	@NotNull
	@Override
	public String getComponentName() {
		return "intino.console.ProjectComponent";
	}
}
