package io.intino.plugin.toolwindows.store;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.SimpleToolWindowPanel;

class IntinoStoreToolWindow extends SimpleToolWindowPanel {
	private final StoreInspectorView view;

	IntinoStoreToolWindow(Project project) {
		super(true, true);
		view = new StoreInspectorView(project);
		add(view.contentPane());
	}
}
