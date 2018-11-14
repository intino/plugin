package io.intino.plugin.actions;

import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionPlaces;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.module.Module;
import io.intino.tara.compiler.shared.Configuration;
import io.intino.tara.plugin.lang.psi.impl.TaraUtil;

import static io.intino.plugin.DataContext.getContext;

public class ExportAction {

	public void execute(Module module) {
		final Configuration configuration = TaraUtil.configurationOf(module);
		if (configuration == null) return;
		final String version = configuration.boxVersion();
		if (version == null || version.isEmpty()) return;
		ActionManager.getInstance().getAction("PublishAccessors" + version).actionPerformed(createActionEvent());
	}

	private AnActionEvent createActionEvent() {
		return new AnActionEvent(null, getContext(),
				ActionPlaces.UNKNOWN, new Presentation(),
				ActionManager.getInstance(), 0);
	}
}
