package io.intino.plugin.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import io.intino.plugin.project.LegioConfiguration;
import io.intino.tara.compiler.shared.Configuration;
import io.intino.tara.plugin.lang.psi.impl.TaraUtil;

public class PurgeAndReloadConfigurationAction extends IntinoAction implements DumbAware {
	@Override
	public void actionPerformed(AnActionEvent e) {
		final Project project = e.getProject();
		if (project == null) return;
		Module module = e.getData(LangDataKeys.MODULE);
		final Configuration configuration = TaraUtil.configurationOf(module);
		if (configuration != null && configuration instanceof LegioConfiguration) {
			FileDocumentManager.getInstance().saveAllDocuments();
			((LegioConfiguration)configuration).purgeAndReload();
			notifyReload(module);
		}
	}


}
