package io.intino.plugin.build;

import com.intellij.ide.SaveAndSyncHandlerImpl;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications.Bus;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.compiler.CompileScope;
import com.intellij.openapi.compiler.CompileStatusNotification;
import com.intellij.openapi.compiler.CompilerManager;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.progress.PerformInBackgroundOption;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.progress.impl.BackgroundableProcessIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ex.ProjectManagerEx;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.util.ui.ConfirmationDialog;
import io.intino.plugin.MessageProvider;
import io.intino.plugin.dependencyresolution.ArtifactoryConnector;
import org.jetbrains.annotations.NotNull;
import io.intino.tara.compiler.shared.Configuration;
import io.intino.tara.plugin.lang.LanguageManager;
import io.intino.tara.plugin.lang.TaraIcons;
import io.intino.tara.plugin.lang.psi.impl.TaraUtil;
import io.intino.tara.plugin.settings.TaraSettings;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static com.intellij.openapi.vcs.VcsShowConfirmationOption.STATIC_SHOW_CONFIRMATION;

public class ArtifactBuilder extends AbstractArtifactBuilder {
	private final Project project;
	private List<Module> modules;
	private LifeCyclePhase lifeCyclePhase;

	public ArtifactBuilder(Project project, final List<Module> modules, LifeCyclePhase phase) {
		this.project = project;
		this.modules = modules;
		this.lifeCyclePhase = phase;
	}

	public void build() {
		final CompilerManager compilerManager = CompilerManager.getInstance(project);
		CompileScope scope = compilerManager.createModulesCompileScope(modules.toArray(new Module[modules.size()]), true);
		if (languageExists()) compilerManager.make(scope, processArtifact());
		else compilerManager.compile(scope, processArtifact());
	}

	private boolean languageExists() {
		for (Module module : modules) {
			Configuration configuration = TaraUtil.configurationOf(module);
			File languageFile = LanguageManager.getLanguageFile(configuration.outDSL(), configuration.modelVersion());
			if (shouldDistributeLanguage(module, lifeCyclePhase) && !languageFile.exists()) return false;
		}
		return true;
	}

	private CompileStatusNotification processArtifact() {
		return (aborted, errors, warnings, compileContext) -> {
			if (!aborted && errors == 0) doProcess();
		};
	}

	private void doProcess() {
		saveAll();
		for (Module module : modules) if (!checkOverrideVersion(module, extractDSL(module))) return;
		withTask(new Task.Backgroundable(project, firstUpperCase(lifeCyclePhase.gerund()) + " Artifact", true, PerformInBackgroundOption.ALWAYS_BACKGROUND) {
			@Override
			public void run(@NotNull ProgressIndicator indicator) {
				for (Module module : modules) process(module, lifeCyclePhase, indicator);
				ApplicationManager.getApplication().invokeLater(() -> {
					reloadProject();
					if (!errorMessages.isEmpty())
						Bus.notify(new Notification("Tara Language", MessageProvider.message("error.occurred", lifeCyclePhase.gerund().toLowerCase()), errorMessages.get(0), NotificationType.ERROR), project);
					else processMessages(successMessages, modules);
				});
			}
		});
	}

	private String firstUpperCase(String input) {
		return input.substring(0, 1).toUpperCase() + input.substring(1);
	}

	private void withTask(Task.Backgroundable runnable) {
		ProgressManager.getInstance().runProcessWithProgressAsynchronously(runnable, new BackgroundableProcessIndicator(runnable));
	}

	private boolean checkOverrideVersion(Module module, String dsl) {
		final Configuration configuration = TaraUtil.configurationOf(module);
		ConfirmationDialog dialog = new ConfirmationDialog(module.getProject(), MessageProvider.message("artifactory.overrides"), "Artifactory", TaraIcons.LOGO_80, STATIC_SHOW_CONFIRMATION);
		dialog.setDoNotAskOption(null);
		if (configuration == null) return false;
		final String version = configuration.modelVersion();
		return version != null && (version.contains("-SNAPSHOT") || !exists(module, dsl, version) || !TaraSettings.getSafeInstance(module.getProject()).overrides() || dialog.showAndGet());
	}

	private boolean exists(Module module, String dsl, String version) {
		final Configuration configuration = TaraUtil.configurationOf(module);
		try {
			return new ArtifactoryConnector(configuration.releaseRepositories(), configuration.snapshotRepository(), configuration.languageRepository()).versions(dsl).contains(version);
		} catch (IOException e) {
			return false;
		}
	}

	private void saveAll() {
		Application manager = ApplicationManager.getApplication();
		if (manager.isWriteAccessAllowed()) FileDocumentManager.getInstance().saveAllDocuments();
		ProjectManagerEx.getInstanceEx().blockReloadingProjectOnExternalChanges();
	}

	private void reloadProject() {
		SaveAndSyncHandlerImpl.getInstance().refreshOpenFiles();
		VirtualFileManager.getInstance().refreshWithoutFileWatcher(false);
		ProjectManagerEx.getInstanceEx().unblockReloadingProjectOnExternalChanges();
	}

	private void processMessages(List<String> successMessages, List<Module> modules) {
		StringBuilder messageBuf = new StringBuilder();
		for (String message : successMessages) {
			if (messageBuf.length() != 0) messageBuf.append('\n');
			messageBuf.append(message);
		}
		final Module first = modules.get(0);
		notify(first.getProject(), messageBuf.toString(), modules.size() == 1 ?
				MessageProvider.message("success.publish.message", lifeCyclePhase.participle()) : MessageProvider.message("success.language.publishing.message", lifeCyclePhase.participle()));
	}

	private void notify(Project project, String title, String body) {
		Bus.notify(new Notification("Tara Language", title, body, NotificationType.INFORMATION), project);
	}

	private String extractDSL(Module module) {
		final Configuration conf = TaraUtil.configurationOf(module);
		return conf == null ? "" : conf.outDSL();
	}

}