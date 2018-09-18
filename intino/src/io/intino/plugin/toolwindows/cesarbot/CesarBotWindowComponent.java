package io.intino.plugin.toolwindows.cesarbot;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowAnchor;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.content.ContentFactory;
import io.intino.plugin.IntinoIcons;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class CesarBotWindowComponent implements ProjectComponent {
	private static final Logger logger = Logger.getInstance(CesarBotWindowComponent.class);

	private static final String CESAR_BOT_WINDOW_ID = "Cesar bot";
	private ToolWindow consoleWindow;
	private Project project;
	private CesarBot outputsToolWindow;

	public CesarBotWindowComponent(Project project) {
		this.project = project;
	}

	@Override
	public void projectOpened() {
		createToolWindows();
	}

	private void createToolWindows() {
		ToolWindowManager toolWindowManager = ToolWindowManager.getInstance(project);
		CesarBot.project = project;
		outputsToolWindow = new CesarBot(project);
		ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
		consoleWindow = toolWindowManager.registerToolWindow(CESAR_BOT_WINDOW_ID, true, ToolWindowAnchor.BOTTOM);
		consoleWindow.getContentManager().addContent(contentFactory.createContent(outputsToolWindow.content(), "", false));
		consoleWindow.setIcon(IntinoIcons.INTINO_16);
	}

	@Override
	public void projectClosed() {
		outputsToolWindow = null;
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


	public ToolWindow consoleWindow() {
		return consoleWindow;
	}

	public static CesarBotWindowComponent getInstance(Project project) {
		if (project == null) {
			logger.error("getInstance: project is null");
			return null;
		}
		CesarBotWindowComponent pc = project.getComponent(CesarBotWindowComponent.class);
		if (pc == null) {
			logger.error("getInstance: getComponent() for " + project.getName() + " returns null");
		}
		return pc;
	}

	public static void showConsoleWindow(final Project project) {
		ApplicationManager.getApplication().invokeLater(() -> Objects.requireNonNull(CesarBotWindowComponent.getInstance(project)).consoleWindow().show(null));
	}
}
