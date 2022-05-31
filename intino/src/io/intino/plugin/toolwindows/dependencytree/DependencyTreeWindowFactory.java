package io.intino.plugin.toolwindows.dependencytree;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import io.intino.plugin.lang.psi.impl.IntinoUtil;
import io.intino.plugin.project.IntinoDirectory;
import io.intino.plugin.project.configuration.LegioConfiguration;
import io.intino.plugin.project.configuration.LegioFileCreator;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collections;

public class DependencyTreeWindowFactory implements ToolWindowFactory, DumbAware {

	@Override
	public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
		Content content = ContentFactory.SERVICE.getInstance().createContent(new DependencyTreeToolWindow(project), "", false);
		toolWindow.getContentManager().addContent(content);
		toolWindow.getContentManager().setSelectedContent(content, false);
	}

	@Override
	public boolean isApplicable(@NotNull Project project) {
		final Module[] modules = ModuleManager.getInstance(project).getModules();
		return modules.length == 0 ? IntinoDirectory.of(project).exists() : Arrays.stream(modules).anyMatch(module -> IntinoUtil.configurationOf(module) instanceof LegioConfiguration || new LegioFileCreator(module, Collections.emptyList()).get() != null);
	}
}