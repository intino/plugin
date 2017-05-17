package io.intino.plugin.toolwindows.project;

import com.intellij.openapi.actionSystem.DataProvider;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.SimpleToolWindowPanel;


class IntinoToolWindow extends SimpleToolWindowPanel implements DataProvider {

	IntinoToolWindow(Project project) {
		super(true, true);
		add(new IntinoFactoryView(project).contentPane());
	}
}
