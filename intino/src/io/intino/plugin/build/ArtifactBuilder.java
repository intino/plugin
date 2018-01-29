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
import io.intino.plugin.IntinoIcons;
import io.intino.plugin.MessageProvider;
import io.intino.plugin.dependencyresolution.ArtifactoryConnector;
import io.intino.plugin.project.LegioConfiguration;
import io.intino.plugin.settings.IntinoSettings;
import io.intino.tara.compiler.shared.Configuration;
import io.intino.tara.plugin.lang.LanguageManager;
import io.intino.tara.plugin.lang.psi.impl.TaraUtil;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.List;

import static com.intellij.openapi.vcs.VcsShowConfirmationOption.STATIC_SHOW_CONFIRMATION;

public class ArtifactBuilder extends AbstractArtifactBuilder {
	private final Project project;
	private List<Module> modules;
	private FactoryPhase factoryPhase;

	public ArtifactBuilder(Project project, final List<Module> modules, FactoryPhase phase) {
		this.project = project;
		this.modules = modules;
		this.factoryPhase = phase;
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
			File languageFile = LanguageManager.getLanguageFile(configuration.outDSL(), configuration.version());
			if (shouldDistributeLanguage(module, factoryPhase) && !languageFile.exists()) return false;
		}
		return true;
	}

	private CompileStatusNotification processArtifact() {
		return (aborted, errors, warnings, compileContext) -> {
			if (!aborted && errors == 0) process();
		};
	}

	private void process() {
		saveAll();
		for (Module module : modules) if (!checkOverrideVersion(module, extractDSL(module))) return;
		withTask(new Task.Backgroundable(project, firstUpperCase(factoryPhase.gerund()) + " Artifact", true, PerformInBackgroundOption.ALWAYS_BACKGROUND) {
			@Override
			public void run(@NotNull ProgressIndicator indicator) {
				for (Module module : modules) {
					if (!(TaraUtil.configurationOf(module) instanceof LegioConfiguration)) continue;
					process(module, factoryPhase, indicator);
				}
				ApplicationManager.getApplication().invokeLater(() -> {
					reloadProject();
					if (!errorMessages.isEmpty())
						Bus.notify(new Notification("Tara Language", MessageProvider.message("error.occurred", factoryPhase.gerund().toLowerCase()), errorMessages.get(0), NotificationType.ERROR), project);
					else processSuccessMessages();
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
		ConfirmationDialog dialog = new ConfirmationDialog(module.getProject(), MessageProvider.message("artifactory.overrides"), "Artifactory", IntinoIcons.INTINO_80, STATIC_SHOW_CONFIRMATION);
		dialog.setDoNotAskOption(null);
		if (configuration == null) return false;
		final String version = configuration.version();
		return version != null && (version.contains("-SNAPSHOT") || !exists(module, dsl, version) || !IntinoSettings.getSafeInstance(module.getProject()).overrides() || dialog.showAndGet());
	}

	private boolean exists(Module module, String dsl, String version) {
		final Configuration configuration = TaraUtil.configurationOf(module);
		return new ArtifactoryConnector(configuration.languageRepositories()).versions(dsl).contains(version);
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

	private void processSuccessMessages() {
		StringBuilder messageBuilder = new StringBuilder();
		for (String message : successMessages) {
			if (messageBuilder.length() != 0) messageBuilder.append('\n');
			messageBuilder.append(message);
		}
		final Module module = modules.get(0);
		final String message = messageBuilder.toString();
		if (message.isEmpty()) return;
		notify(module.getProject(), module.getName(), modules.size() == 1 ?
				MessageProvider.message("success.publish.message", factoryPhase.participle()) : MessageProvider.message("success.language.publishing.message", factoryPhase.participle()));
	}

	private void notify(Project project, String title, String body) {
		Bus.notify(new Notification("Tara Language", title, body, NotificationType.INFORMATION), project);
	}

	private String extractDSL(Module module) {
		final Configuration conf = TaraUtil.configurationOf(module);
		return conf == null ? "" : conf.outDSL();
	}

}
