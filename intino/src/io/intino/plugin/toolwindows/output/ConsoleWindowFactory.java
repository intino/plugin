package io.intino.plugin.toolwindows.output;

import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.content.ContentFactory;
import io.intino.plugin.lang.psi.impl.IntinoUtil;
import io.intino.plugin.project.LegioConfiguration;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public class ConsoleWindowFactory implements ToolWindowFactory, DumbAware {

	public static final String ID = "Intino Console";

	public static ToolWindow getInstance(Project project) {
		return ToolWindowManager.getInstance(project).getToolWindow(ID);
	}

	@Override
	public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
		ConsoleWindow consoleWindow = new ConsoleWindow(project);
		toolWindow.setTitle("Console");
		toolWindow.getContentManager().addContent(ContentFactory.SERVICE.getInstance().
				createContent(consoleWindow.content(), "", false));
	}

	@Override
	public boolean shouldBeAvailable(@NotNull Project project) {
		return Arrays.stream(ModuleManager.getInstance(project).getModules()).anyMatch(module -> IntinoUtil.configurationOf(module) instanceof LegioConfiguration);
	}
}
