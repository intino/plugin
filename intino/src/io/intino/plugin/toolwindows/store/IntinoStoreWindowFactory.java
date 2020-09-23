package io.intino.plugin.toolwindows.store;

import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import io.intino.plugin.IntinoIcons;
import org.jetbrains.annotations.NotNull;

public class IntinoStoreWindowFactory implements ToolWindowFactory, DumbAware {

	@Override
	public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
		IntinoStoreToolWindow factorytoolWindow = new IntinoStoreToolWindow(project);
		Content content = ContentFactory.SERVICE.getInstance().
				createContent(factorytoolWindow, "", false);
		toolWindow.setIcon(IntinoIcons.STASH_16);
		toolWindow.getContentManager().addContent(content);
		toolWindow.getContentManager().setSelectedContent(content, false);
	}
}
