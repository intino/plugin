package io.intino.plugin.toolwindows.remote;

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

public class RemoteWindowFactory implements ToolWindowFactory, DumbAware {
	public static final String ID = "Intino Remote";

	public static ToolWindow getInstance(Project project) {
		return ToolWindowManager.getInstance(project).getToolWindow(ID);
	}

	@Override
	public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
		RemoteWindow remoteWindow = new RemoteWindow(project);
		toolWindow.setTitle("Remote");
		toolWindow.getContentManager().addContent(ContentFactory.getInstance().createContent(remoteWindow.content(), "", false));
	}

	@Override
	public boolean isApplicable(@NotNull Project project) {
		final Module[] modules = ModuleManager.getInstance(project).getModules();
		return modules.length == 0 ? IntinoDirectory.of(project).exists() : Arrays.stream(modules).anyMatch(module -> IntinoUtil.configurationOf(module) instanceof LegioConfiguration || new LegioFileCreator(module, Collections.emptyList()).get() != null);
	}
}
