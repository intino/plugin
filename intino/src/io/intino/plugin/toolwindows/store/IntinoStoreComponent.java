package io.intino.plugin.toolwindows.store;

import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupManager;
import com.intellij.openapi.wm.ToolWindowAnchor;
import com.intellij.openapi.wm.ex.ToolWindowEx;
import com.intellij.openapi.wm.ex.ToolWindowManagerEx;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.ui.content.ContentManager;
import org.jetbrains.annotations.NotNull;

import static io.intino.plugin.IntinoIcons.STASH_16;

public class IntinoStoreComponent implements ProjectComponent {

	private final Project project;
	private IntinoStoreToolWindow inspectorPanel;
	private ToolWindowEx myToolWindow;
	private String ID_TOOL_WINDOW = "Intino Store";

	public IntinoStoreComponent(Project project) {
		this.project = project;
	}

	@Override
	public void projectOpened() {
	}

	@Override
	public void projectClosed() {
		unregisterToolWindow();
	}

	private void unregisterToolWindow() {
		if (inspectorPanel != null) inspectorPanel = null;
	}

	@NotNull
	@Override
	public String getComponentName() {
		return "Intino" + '.' + "StoreProjectComponent";
	}

	@Override
	public void initComponent() {
		StartupManager.getInstance(project).registerPostStartupActivity(this::doInit);
	}

	private void doInit() {
		inspectorPanel = new IntinoStoreToolWindow(project);
		final ToolWindowManagerEx manager = ToolWindowManagerEx.getInstanceEx(project);
		myToolWindow = (ToolWindowEx) manager.registerToolWindow(ID_TOOL_WINDOW, false, ToolWindowAnchor.LEFT, project, false);
		myToolWindow.setIcon(STASH_16);
		final ContentFactory contentFactory = ServiceManager.getService(ContentFactory.class);
		final Content content = contentFactory.createContent(inspectorPanel, "", false);
		ContentManager contentManager = myToolWindow.getContentManager();
		contentManager.addContent(content);
		contentManager.setSelectedContent(content, false);
	}

	@Override
	public void disposeComponent() {
		unregisterToolWindow();
		myToolWindow = null;
	}
}
