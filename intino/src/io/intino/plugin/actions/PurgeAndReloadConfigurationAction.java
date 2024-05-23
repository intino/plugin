package io.intino.plugin.actions;

import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import io.intino.Configuration;
import io.intino.plugin.IntinoIcons;
import io.intino.plugin.lang.psi.impl.IntinoUtil;
import io.intino.plugin.project.configuration.ArtifactLegioConfiguration;
import org.jetbrains.annotations.NotNull;

public class PurgeAndReloadConfigurationAction extends IntinoAction implements DumbAware {
	@Override
	public @NotNull ActionUpdateThread getActionUpdateThread() {
		return ActionUpdateThread.BGT;
	}

	@Override
	public void actionPerformed(AnActionEvent e) {
		final Project project = e.getProject();
		if (project == null) return;
		Module module = e.getData(LangDataKeys.MODULE);
		if (module != null) execute(module);
	}

	@Override
	public void execute(Module module) {
		final Configuration configuration = IntinoUtil.configurationOf(module);
		if (configuration instanceof ArtifactLegioConfiguration) {
			FileDocumentManager.getInstance().saveAllDocuments();
			((ArtifactLegioConfiguration) configuration).purgeAndReload();
			notifyReload(module);
		}

	}

	@Override
	public void update(AnActionEvent e) {
		super.update(e);
		e.getPresentation().setIcon(IntinoIcons.LEGIO_16);
	}
}
