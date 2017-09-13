package io.intino.plugin.toolwindows.project;

import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.compiler.CompilerManager;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.module.Module;
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
import io.intino.plugin.actions.InterfaceGenerationAction;
import org.jetbrains.annotations.NotNull;

import static io.intino.plugin.toolwindows.project.IntinoFactoryComponent.ViewConstants.*;

public class IntinoFactoryComponent implements ProjectComponent {
	private final Project project;
	private IntinoFactoryToolWindow toolWindow;
	private ToolWindowEx myToolWindow;

	public IntinoFactoryComponent(Project project) {
		this.project = project;
	}

	@Override
	public void initComponent() {
		StartupManager.getInstance(project).registerPostStartupActivity(this::registerToolWindow);
	}

	@Override
	public void projectOpened() {
		final CompilerManager compilerManager = CompilerManager.getInstance(project);
		compilerManager.addBeforeTask(context -> {
			final InterfaceGenerationAction action = (InterfaceGenerationAction) ActionManager.getInstance().getAction("InterfaceGeneration");
			for (Module module : context.getCompileScope().getAffectedModules()) {
				action.execute(module);
			}
			return true;
		});
	}

	@Override
	public void projectClosed() {
	}

	@NotNull
	@Override
	public String getComponentName() {
		return PLUGIN_NAME + '.' + PROJECT_COMPONENT_NAME;
	}

	private void registerToolWindow() {
		toolWindow = new IntinoFactoryToolWindow(project);
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
		String PROJECT_COMPONENT_NAME = "ProjectComponent";
		String ID_TOOL_WINDOW = "Intino Factory";

	}
}
