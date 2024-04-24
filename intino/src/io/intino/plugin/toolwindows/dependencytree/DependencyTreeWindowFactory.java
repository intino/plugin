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
import io.intino.plugin.project.configuration.ArtifactLegioConfiguration;
import io.intino.plugin.project.configuration.LegioFileCreator;
import kotlin.coroutines.Continuation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

public class DependencyTreeWindowFactory implements ToolWindowFactory, DumbAware {

	@Override
	public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
		Content content = ContentFactory.getInstance().createContent(new DependencyTreeToolWindow(project), "", false);
		toolWindow.getContentManager().addContent(content);
		toolWindow.getContentManager().setSelectedContent(content, false);
	}

	@Nullable
	@Override
	public Object isApplicableAsync(@NotNull Project project, @NotNull Continuation<? super Boolean> $completion) {
		final Module[] modules = ModuleManager.getInstance(project).getModules();
		return modules.length == 0 ? IntinoDirectory.of(project).exists() : Arrays.stream(modules).anyMatch(module -> IntinoUtil.configurationOf(module) instanceof ArtifactLegioConfiguration || new LegioFileCreator(module, new String[0]).getArtifact() != null);
	}
}