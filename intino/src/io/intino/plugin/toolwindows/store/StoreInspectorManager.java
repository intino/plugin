package io.intino.plugin.toolwindows.store;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.SimpleToolWindowPanel;

class StoreInspectorManager extends SimpleToolWindowPanel {
	private static final Logger LOG = Logger.getInstance("StoreInspectorManager");

	private final StoreInspectorView view;

	StoreInspectorManager(Project project) {
		super(true, true);
		view = new StoreInspectorView(project);
		add(view.getContentPane());
	}
}
