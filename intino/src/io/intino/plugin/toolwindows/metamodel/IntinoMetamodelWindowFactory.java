package io.intino.plugin.toolwindows.metamodel;

import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.content.ContentFactory;
import org.jetbrains.annotations.NotNull;

public class IntinoMetamodelWindowFactory implements ToolWindowFactory, DumbAware {
	public static final String ID = "Cesar Bot";

	public static ToolWindow getInstance(Project project) {
		return ToolWindowManager.getInstance(project).getToolWindow(ID);
	}

	@Override
	public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
		MetamodelWindow window = new MetamodelWindow(project);
		toolWindow.setTitle("Metamodel");
		toolWindow.getContentManager().addContent(ContentFactory.getInstance().createContent(window.content(), "", true));
	}
}
