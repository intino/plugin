package io.intino.plugin.actions;

import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.module.Module;
import io.intino.plugin.build.FactoryPhase;
import io.intino.tara.compiler.shared.Configuration;
import io.intino.tara.plugin.lang.psi.impl.TaraUtil;

import static io.intino.plugin.DataContext.getContext;

public class ExportAction {

	public void execute(Module module, FactoryPhase factoryPhase) {
		final Configuration configuration = TaraUtil.configurationOf(module);
		if (configuration == null || configuration.box() == null) return;
		final String version = configuration.box().version();
		if (version == null || version.isEmpty()) return;
		AnAction action = ActionManager.getInstance().getAction((factoryPhase.equals(FactoryPhase.INSTALL) ? "Install" : "Publish") + "Accessors" + version);
		if (action != null) action.actionPerformed(createActionEvent());
	}

	private AnActionEvent createActionEvent() {
		return new AnActionEvent(null, getContext(),
				ActionPlaces.UNKNOWN, new Presentation(),
				ActionManager.getInstance(), 0);
	}
}
