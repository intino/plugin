/*
 *  Copyright 2013-2016 SIANI - ULPGC
 *
 *  This File is part of JavaFMI Project
 *
 *  JavaFMI Project is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as published by
 *  the Free Software Foundation, either version 3 of the License.
 *
 *  JavaFMI Project is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with JavaFMI. If not, see <http://www.gnu.org/licenses/>.
 */

package io.intino.plugin.toolwindow;

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

import static io.intino.plugin.toolwindow.ViewConstants.*;

public class IntinoProjectCompoment implements ProjectComponent {
	private final Project project;
	private IntinoToolWindow toolWindow;
	private ToolWindowEx myToolWindow;

	public IntinoProjectCompoment(Project project) {
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
		toolWindow = new IntinoToolWindow(project);
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

}
