package org.siani.legio.plugin.actions.publish;

import com.intellij.ide.SaveAndSyncHandlerImpl;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.compiler.CompileStatusNotification;
import com.intellij.openapi.compiler.CompilerManager;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ex.ProjectManagerEx;
import com.intellij.openapi.roots.ui.configuration.ChooseModulesDialog;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.util.ui.ConfirmationDialog;
import org.jetbrains.annotations.NotNull;
import org.siani.legio.plugin.LegioIcons;
import org.siani.legio.plugin.dependencyresolution.ArtifactoryConnector;
import tara.compiler.shared.Configuration;
import tara.intellij.lang.TaraIcons;
import tara.intellij.lang.psi.impl.TaraUtil;
import tara.intellij.project.TaraModuleType;
import tara.intellij.settings.TaraSettings;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static com.intellij.openapi.ui.Messages.showErrorDialog;
import static com.intellij.openapi.vcs.VcsShowConfirmationOption.STATIC_SHOW_CONFIRMATION;
import static tara.compiler.shared.Configuration.Level.System;
import static tara.intellij.messages.MessageProvider.message;

public class PublishLanguageAction extends PublishLanguageAbstractAction {

	@Override
	public void actionPerformed(@NotNull AnActionEvent e) {
		final Project project = e.getData(CommonDataKeys.PROJECT);
		if (project == null) return;
		publish(project);
	}

	private void publish(Project project) {
		successMessages.clear();
		errorMessages.clear();
		List<Module> taraModules = collectTaraModules(project).stream().collect(Collectors.toList());
		if (taraModules.isEmpty()) {
			Messages.showInfoMessage(project, message("no.tara.modules"), " Language Publisher");
			return;
		}
		if (taraModules.size() > 1) {
			ChooseModulesDialog dialog = createDialog(project, taraModules);
			dialog.show();
			if (dialog.isOK()) publish(extractDSLs(dialog.getChosenElements()), project);
		} else publish(extractDSLs(taraModules), project);
	}

	private ChooseModulesDialog createDialog(Project project, List<Module> taraModules) {
		final ChooseModulesDialog chooseModulesDialog = new ChooseModulesDialog(project,
				taraModules,
				message("select.tara.module.title"),
				message("select.tara.module.description"));
		chooseModulesDialog.setSingleSelectionMode();
		chooseModulesDialog.selectElements(Collections.singletonList(taraModules.get(0)));
		return chooseModulesDialog;
	}

	private Map<Module, String> extractDSLs(List<Module> modules) {
		Map<Module, String> map = new HashMap<>();
		for (Module module : modules) {
			final Configuration conf = TaraUtil.configurationOf(module);
			if (conf == null) continue;
			map.put(module, conf.outDSL());
		}
		return map;
	}

	void publish(final Map<Module, String> dsls, Project project) {
		final CompilerManager compilerManager = CompilerManager.getInstance(project);
		compilerManager.make(compilerManager.createModulesCompileScope(dsls.keySet().toArray(new Module[dsls.keySet().size()]), true), publish(project, dsls));
	}

	private CompileStatusNotification publish(Project project, final Map<Module, String> modules) {
		return (aborted, errors, warnings, compileContext) -> {
			if (!aborted && errors == 0) doPublish(project, modules);
		};
	}

	private void doPublish(Project project, Map<Module, String> dslMap) {
		ApplicationManager.getApplication().invokeLater(() -> {
			publishLanguage(project, dslMap);
			if (!errorMessages.isEmpty()) showErrorDialog(errorMessages.iterator().next(), message("error.occurred"));
			processMessages(successMessages, dslMap);
		});
	}

	private void publishLanguage(Project project, final Map<Module, String> modules) {
		saveAll(project);
		for (Module module : modules.keySet())
			if (checkOverrideVersion(module, modules.get(module)) && !publish(module)) return;
		reloadProject();
	}

	private boolean checkOverrideVersion(Module module, String dsl) {
		final Configuration configuration = TaraUtil.configurationOf(module);
		ConfirmationDialog dialog = new ConfirmationDialog(module.getProject(), message("artifactory.overrides"), "Artifactory", TaraIcons.LOGO_80, STATIC_SHOW_CONFIRMATION);
		dialog.setDoNotAskOption(null);
		if (configuration == null) return false;
		final String version = configuration.modelVersion();
		return version != null && (version.contains("-SNAPSHOT") || !exists(module, dsl, version) || !TaraSettings.getSafeInstance(module.getProject()).overrides() || dialog.showAndGet());

	}

	private boolean exists(Module module, String dsl, String version) {
		final Configuration configuration = TaraUtil.configurationOf(module);
		try {
			return new ArtifactoryConnector(configuration.releaseRepositories(), configuration.snapshotRepositories(), configuration.languageRepository()).versions(dsl).contains(version);
		} catch (IOException e) {
			return false;
		}
	}

	private void saveAll(Project project) {
		project.save();
		FileDocumentManager.getInstance().saveAllDocuments();
		ProjectManagerEx.getInstanceEx().blockReloadingProjectOnExternalChanges();
	}

	private void reloadProject() {
		SaveAndSyncHandlerImpl.getInstance().refreshOpenFiles();
		VirtualFileManager.getInstance().refreshWithoutFileWatcher(false);
		ProjectManagerEx.getInstanceEx().unblockReloadingProjectOnExternalChanges();
	}

	private List<Module> collectTaraModules(Project project) {
		List<Module> taraModules = new ArrayList<>();
		for (Module module : ModuleManager.getInstance(project).getModules())
			if (TaraModuleType.isTara(module) && !System.equals(TaraUtil.configurationOf(module).level()))
				taraModules.add(module);
		return taraModules;
	}

	private void processMessages(List<String> successMessages, Map<Module, String> modules) {
		StringBuilder messageBuf = new StringBuilder();
		for (String message : successMessages) {
			if (messageBuf.length() != 0) messageBuf.append('\n');
			messageBuf.append(message);
		}
		final Module next = modules.keySet().iterator().next();
		notify(next.getProject(), messageBuf.toString(), modules.size() == 1 ?
				message("success.deployment.message", next.getName()) : message("success.language.exportation.message"));
	}

	private void notify(Project project, String title, String body) {
		Notifications.Bus.notify(new Notification("Tara Language", title, body, NotificationType.INFORMATION), project);
	}

	@Override
	public void update(@NotNull AnActionEvent e) {
		final Project project = e.getData(CommonDataKeys.PROJECT);
		boolean enabled = !collectTaraModules(project).isEmpty();
		e.getPresentation().setVisible(enabled);
		e.getPresentation().setEnabled(enabled);
		e.getPresentation().setIcon(LegioIcons.ICON_16);
		if (enabled) e.getPresentation().setText(message("export.language"));
	}
}
