package io.intino.plugin.actions;

import com.intellij.ide.SaveAndSyncHandlerImpl;
import com.intellij.notification.Notification;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ex.ProjectManagerEx;
import com.intellij.psi.PsiFile;
import io.intino.plugin.IntinoIcons;
import io.intino.plugin.MessageProvider;
import io.intino.plugin.dependencyresolution.LegioUtil;
import io.intino.plugin.project.LegioConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tara.compiler.shared.Configuration;
import tara.intellij.lang.psi.impl.TaraUtil;
import tara.intellij.project.TaraModuleType;

import static com.intellij.notification.NotificationType.ERROR;
import static com.intellij.notification.NotificationType.INFORMATION;
import static org.apache.maven.artifact.Artifact.LATEST_VERSION;
import static tara.intellij.project.module.ModuleProvider.moduleOf;

public class UpdateLanguageAction extends AnAction implements DumbAware {
	@Override
	public void actionPerformed(AnActionEvent e) {
		final PsiFile file = e.getData(LangDataKeys.PSI_FILE);
		if (file != null) updateLanguage(file, LATEST_VERSION);
		else updateLanguage(e.getData(LangDataKeys.MODULE), LATEST_VERSION);
	}

	private void updateLanguage(PsiFile psiFile, String version) {
		updateLanguage(moduleOf(psiFile), version);
	}

	private void updateLanguage(Module module, String version) {
		saveAll(module.getProject());
		updateDsl(version, module, TaraUtil.configurationOf(module));
		reloadProject();
	}

	private void updateDsl(String version, Module module, Configuration conf) {
		ProgressManager.getInstance().runProcessWithProgressSynchronously(() -> {
			createProgressIndicator();
			final String dsl = conf.dsl();
			if (dsl == null || dsl.isEmpty()) {
				error(module.getProject());
				return;
			}
			updateLanguage(conf, dsl, version);
			success(module.getProject(), dsl, conf.dslVersion());
		}, MessageProvider.message("updating.language"), true, module.getProject());
	}

	private void updateLanguage(Configuration configuration, String dsl, String version) {
		configuration.dslVersion(LegioUtil.effectiveVersionOf(dsl, version, (LegioConfiguration) configuration));
	}

	private void success(Project project, String language, String version) {
		final Notification notification = new Notification("Tara Language", MessageProvider.message("language.updated.sucessfully"), language + " " + version, INFORMATION).setImportant(true);
		Notifications.Bus.notify(notification, project);
	}

	private void error(Project project) {
		final Notification notification = new Notification("Tara Language", MessageProvider.message("language.update.error"), "Language name is empty", ERROR).setImportant(true);
		Notifications.Bus.notify(notification, project);
	}

	@Nullable
	private ProgressIndicator createProgressIndicator() {
		final ProgressIndicator indicator = ProgressManager.getInstance().getProgressIndicator();
		if (indicator != null) {
			indicator.setText(MessageProvider.message("import.language.message"));
			indicator.setIndeterminate(true);
		}
		return indicator;
	}

	private void saveAll(Project project) {
		project.save();
		ApplicationManager.getApplication().invokeLater(() -> FileDocumentManager.getInstance().saveAllDocuments());
		ProjectManagerEx.getInstanceEx().blockReloadingProjectOnExternalChanges();
	}

	private void reloadProject() {
		SaveAndSyncHandlerImpl.getInstance().refreshOpenFiles();
//		VirtualFileManager.getInstance().refreshWithoutFileWatcher(false);
		ProjectManagerEx.getInstanceEx().unblockReloadingProjectOnExternalChanges();
	}

	@Override
	public void update(@NotNull AnActionEvent e) {
		final Module module = e.getData(LangDataKeys.MODULE);
		boolean enabled = module != null && TaraModuleType.isTara(module);
		e.getPresentation().setVisible(enabled);
		e.getPresentation().setEnabled(enabled);
		e.getPresentation().setIcon(IntinoIcons.INTINO_16);
		if (enabled) e.getPresentation().setText(MessageProvider.message("update.language"));
	}
}
