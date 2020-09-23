package io.intino.plugin.toolwindows.dependencytree;

import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import io.intino.plugin.lang.psi.impl.IntinoUtil;
import io.intino.plugin.project.LegioConfiguration;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;


public class DependencyTreeWindowFactory implements ToolWindowFactory, DumbAware {

	@Override
	public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
		DependencyTreeToolWindow factorytoolWindow = new DependencyTreeToolWindow(project);
		Content content = ContentFactory.SERVICE.getInstance().
				createContent(factorytoolWindow, "", false);
		toolWindow.getContentManager().addContent(content);
		toolWindow.getContentManager().setSelectedContent(content, false);
	}

	@Override
	public boolean shouldBeAvailable(@NotNull Project project) {
		return Arrays.stream(ModuleManager.getInstance(project).getModules()).anyMatch(module -> IntinoUtil.configurationOf(module) instanceof LegioConfiguration);
	}

}
