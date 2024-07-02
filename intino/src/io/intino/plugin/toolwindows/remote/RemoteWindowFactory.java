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
import io.intino.plugin.project.configuration.ArtifactLegioConfiguration;
import io.intino.plugin.project.configuration.LegioFileCreator;
import kotlin.coroutines.Continuation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

	@Nullable
	@Override
	public Object isApplicableAsync(@NotNull Project project, @NotNull Continuation<? super Boolean> $completion) {
		final Module[] modules = ModuleManager.getInstance(project).getModules();
		return modules.length == 0 ? IntinoDirectory.exists(project) : Arrays.stream(modules).anyMatch(module -> IntinoUtil.configurationOf(module) instanceof ArtifactLegioConfiguration || new LegioFileCreator(module, Collections.emptyList()).getArtifact() != null);
	}
}
