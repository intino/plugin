package io.intino.plugin.toolwindows.dependencytree;

import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupManager;
import com.intellij.openapi.wm.ToolWindowAnchor;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.openapi.wm.ex.ToolWindowEx;
import com.intellij.openapi.wm.ex.ToolWindowManagerEx;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.ui.content.ContentManager;
import io.intino.plugin.IntinoIcons;
import org.jetbrains.annotations.NotNull;

import static io.intino.plugin.toolwindows.dependencytree.DependencyTreeComponent.ViewConstants.*;


public class DependencyTreeComponent implements ProjectComponent {
	private final Project project;
	private DependencyTreeToolWindow toolWindow;
	private ToolWindowEx myToolWindow;

	public DependencyTreeComponent(Project project) {
		this.project = project;
	}

	@Override
	public void initComponent() {
		StartupManager.getInstance(project).registerPostStartupActivity(this::registerToolWindow);
	}

	@Override
	public void projectOpened() {
	}

	@Override
	public void projectClosed() {
		unregisterToolWindow();
	}

	@NotNull
	@Override
	public String getComponentName() {
		return PLUGIN_NAME + '.' + PROJECT_COMPONENT_NAME;
	}

	private void registerToolWindow() {
		toolWindow = new DependencyTreeToolWindow(project);
		myToolWindow = (ToolWindowEx) ToolWindowManagerEx.getInstanceEx(project).
				registerToolWindow(ID_TOOL_WINDOW, false, ToolWindowAnchor.RIGHT, project, true);
		myToolWindow.setIcon(IntinoIcons.INTINO_13);
		final ContentFactory contentFactory = ServiceManager.getService(ContentFactory.class);
		final Content content = contentFactory.createContent(toolWindow, "", false);
		ContentManager contentManager = myToolWindow.getContentManager();
		contentManager.addContent(content);
		contentManager.setSelectedContent(content, false);
	}

	private void unregisterToolWindow() {
		toolWindow = null;
		if (isToolWindowRegistered()) ToolWindowManager.getInstance(project).unregisterToolWindow(ID_TOOL_WINDOW);
	}

	private boolean isToolWindowRegistered() {
		return ToolWindowManager.getInstance(project).getToolWindow(ID_TOOL_WINDOW) != null;
	}

	@Override
	public void disposeComponent() {
		unregisterToolWindow();
		myToolWindow = null;
	}

	public interface ViewConstants {

		String PLUGIN_NAME = "Intino";
		String PROJECT_COMPONENT_NAME = "DependencyTree";
		String ID_TOOL_WINDOW = "Intino Dependencies";

	}
}