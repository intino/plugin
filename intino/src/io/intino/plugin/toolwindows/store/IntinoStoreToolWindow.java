package io.intino.plugin.toolwindows.store;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.SimpleToolWindowPanel;

class IntinoStoreToolWindow extends SimpleToolWindowPanel {

	IntinoStoreToolWindow(Project project) {
		super(true, true);
		add(new StoreInspectorView(project).contentPane());
	}
}
