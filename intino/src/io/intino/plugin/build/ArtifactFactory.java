package io.intino.plugin.build;

import com.intellij.configurationStore.StoreReloadManager;
import com.intellij.ide.SaveAndSyncHandler;
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
import com.intellij.openapi.vfs.VirtualFileManager;
import io.intino.Configuration;
import io.intino.plugin.MessageProvider;
import io.intino.plugin.lang.LanguageManager;
import io.intino.plugin.project.LegioConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.File;

import static io.intino.plugin.lang.psi.impl.IntinoUtil.configurationOf;
import static io.intino.plugin.project.Safe.safe;

public class ArtifactFactory extends AbstractArtifactFactory {
	private final Project project;
	private Module module;
	private FactoryPhase phase;

	public ArtifactFactory(Project project, final Module module, FactoryPhase phase) {
		this.project = project;
		this.module = module;
		this.phase = phase;
	}

	public void build(FinishCallback callback) {
		final CompilerManager compilerManager = CompilerManager.getInstance(project);
		CompileScope scope = compilerManager.createModulesCompileScope(new Module[]{module}, true);
		if (needsToRebuild()) compilerManager.compile(scope, processArtifact(callback));
		else compilerManager.make(scope, processArtifact(callback));
	}

	private CompileStatusNotification processArtifact(FinishCallback callback) {
		return (aborted, errors, warnings, compileContext) -> {
			if (!aborted && errors == 0) process(callback);
		};
	}

	private void process(FinishCallback callback) {
		saveAll();
		withTask(new Task.Backgroundable(project, firstUpperCase(phase.gerund()) + " Artifact", true, PerformInBackgroundOption.ALWAYS_BACKGROUND) {
			@Override
			public void run(@NotNull ProgressIndicator indicator) {
				if (!(configurationOf(module) instanceof LegioConfiguration)) return;
				AbstractArtifactFactory.ProcessResult result = process(module, phase, indicator);
				if (callback != null) ApplicationManager.getApplication().invokeLater(() -> callback.onFinish(result));
				if (!result.equals(ProcessResult.Retry)) {
					ApplicationManager.getApplication().invokeLater(() -> {
						reloadProject();
						if (!errorMessages.isEmpty())
							Bus.notify(new Notification("Tara Language", MessageProvider.message("error.occurred", phase.gerund().toLowerCase()), errorMessages.get(0), NotificationType.ERROR), project);
						else processSuccessMessages();
					});
				}
			}
		});
	}

	private boolean needsToRebuild() {
		Configuration configuration = configurationOf(module);
		Configuration.Artifact.Model model = safe(() -> configuration.artifact().model());
		if (model == null) return false;
		File languageFile = LanguageManager.getLanguageFile(model.outLanguage(), configuration.artifact().version());
		return shouldDistributeLanguage(module, phase) && !languageFile.exists();
	}

	private String firstUpperCase(String input) {
		return input.substring(0, 1).toUpperCase() + input.substring(1);
	}

	private void withTask(Task.Backgroundable runnable) {
		ProgressManager.getInstance().runProcessWithProgressAsynchronously(runnable, new BackgroundableProcessIndicator(runnable));
	}

	private void saveAll() {
		Application manager = ApplicationManager.getApplication();
		if (manager.isWriteAccessAllowed()) FileDocumentManager.getInstance().saveAllDocuments();
		StoreReloadManager.getInstance().blockReloadingProjectOnExternalChanges();
	}

	private void reloadProject() {
		SaveAndSyncHandler.getInstance().refreshOpenFiles();
		VirtualFileManager.getInstance().refreshWithoutFileWatcher(false);
		StoreReloadManager.getInstance().unblockReloadingProjectOnExternalChanges();
	}

	private void processSuccessMessages() {
		StringBuilder messageBuilder = new StringBuilder();
		for (String message : successMessages) {
			if (messageBuilder.length() != 0) messageBuilder.append('\n');
			messageBuilder.append(message);
		}
		final String message = messageBuilder.toString();
		if (message.isEmpty()) return;
		notify(module.getProject(), module.getName(), MessageProvider.message("success.publish.message", phase.participle()));
	}

	private void notify(Project project, String title, String body) {
		Bus.notify(new Notification("Tara Language", title, body, NotificationType.INFORMATION), project);
	}

	private String extractDSL(Module module) {
		final Configuration conf = configurationOf(module);
		return safe(() -> conf.artifact().model()) == null ? "" : conf.artifact().model().outLanguage();
	}


	public interface FinishCallback {
		void onFinish(ProcessResult result);
	}
}