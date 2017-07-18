package io.intino.plugin.toolwindows.project;

import com.intellij.openapi.actionSystem.DataProvider;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.SimpleToolWindowPanel;


class IntinoFactoryToolWindow extends SimpleToolWindowPanel implements DataProvider {

	IntinoFactoryToolWindow(Project project) {
		super(true, true);
		add(new IntinoFactoryView(project).contentPane());
	}
}
