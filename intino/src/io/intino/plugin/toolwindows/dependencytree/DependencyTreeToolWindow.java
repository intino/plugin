package io.intino.plugin.toolwindows.dependencytree;

import com.intellij.openapi.actionSystem.DataProvider;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.SimpleToolWindowPanel;


class DependencyTreeToolWindow extends SimpleToolWindowPanel implements DataProvider {

	DependencyTreeToolWindow(Project project) {
		super(true, true);
		add(new DependencyTreeView(project).contentPane());
	}
}
