package io.intino.plugin.toolwindows.cesarbot;

import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.content.ContentFactory;
import org.jetbrains.annotations.NotNull;

public class CesarBotWindowFactory implements ToolWindowFactory, DumbAware {
	public static final String ID = "Cesar Bot";

	public static ToolWindow getInstance(Project project) {
		return ToolWindowManager.getInstance(project).getToolWindow(ID);
	}

	@Override
	public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
		CesarBot cesarBot = new CesarBot(project);
		toolWindow.setTitle("Cesar Bot");
		toolWindow.getContentManager().addContent(ContentFactory.SERVICE.getInstance().
				createContent(cesarBot.content(), "", false));
	}

}
