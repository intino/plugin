package io.intino.plugin.toolwindows.output;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.content.ContentFactory;
import io.intino.plugin.lang.psi.impl.IntinoUtil;
import io.intino.plugin.project.IntinoDirectory;
import io.intino.plugin.project.configuration.LegioConfiguration;
import io.intino.plugin.project.configuration.LegioFileCreator;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collections;

import static io.intino.plugin.lang.psi.impl.IntinoUtil.configurationOf;

public class ConsoleWindowFactory implements ToolWindowFactory, DumbAware {
	public static final String ID = "Intino Console";

	public static ToolWindow getInstance(Project project) {
		return ToolWindowManager.getInstance(project).getToolWindow(ID);
	}

	@Override
	public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
		OutputWindow window = new OutputWindow(project);
		toolWindow.setTitle("Console");
		toolWindow.getContentManager().addContent(ContentFactory.getInstance().createContent(window.content(), "", false));
	}

	@Override
	public boolean isApplicable(@NotNull Project project) {
		final Module[] modules = ModuleManager.getInstance(project).getModules();
		return modules.length == 0 ?
				IntinoDirectory.of(project).exists() :
				Arrays.stream(modules).anyMatch(module -> configurationOf(module) instanceof LegioConfiguration ||
						new LegioFileCreator(module, Collections.emptyList()).get() != null);
	}
}
