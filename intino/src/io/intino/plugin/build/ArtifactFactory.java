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
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.vfs.VirtualFileManager;
import git4idea.commands.GitCommandResult;
import io.intino.Configuration;
import io.intino.plugin.IntinoException;
import io.intino.plugin.MessageProvider;
import io.intino.plugin.build.git.GitUtil;
import io.intino.plugin.lang.LanguageManager;
import io.intino.plugin.project.LegioConfiguration;
import io.intino.plugin.project.configuration.Version;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.time.Instant;
import java.util.Arrays;

import static io.intino.plugin.build.git.GitUtil.currentBranch;
import static io.intino.plugin.project.Safe.safe;

public class ArtifactFactory extends AbstractArtifactFactory {
	private String stash;

	public ArtifactFactory(Module module, FactoryPhase phase) {
		super(module, phase);
	}

	public void build(FinishCallback callback) {
		boolean distributed = isDistributed(configuration.artifact());
		if (includeDistribution(phase) && !distributed && !isSnapshot()) {
			boolean hasChanges = task(() -> Arrays.stream(ModuleRootManager.getInstance(module).getContentRoots()).anyMatch(vf -> GitUtil.isModified(module, vf)));
			if (hasChanges) {
				errorMessages.add("Module has changes. Please commit them and retry.");
				notifyErrors();
				return;
			}

			if (hasSnapshotDependencies()) {
				errorMessages.add("A release version must not have SNAPSHOT dependencies.");
				notifyErrors();
				return;
			}
			try {
				checker.check(phase, module, configuration);
				if (!isInMasterBranch() && !askForReleaseDistribute()) return;
				if (!isInMasterBranch()) checkoutMasterAndMerge();
			} catch (IntinoException e) {
				errorMessages.add(e.getMessage());
			}
		}
		if (!errorMessages.isEmpty()) {
			notifyErrors();
			return;
		}
		if (phase == FactoryPhase.DEPLOY && !isSnapshot() && distributed) process(callback);
		else {
			final CompilerManager compilerManager = CompilerManager.getInstance(project);
			CompileScope scope = compilerManager.createModulesCompileScope(new Module[]{module}, true);
			if (needsToRebuild()) compilerManager.compile(scope, processArtifact(callback));
			else compilerManager.make(scope, processArtifact(callback));
		}
	}

	private boolean hasSnapshotDependencies() {
		try {
			if (configuration.artifact().datahub() != null && new Version(configuration.artifact().datahub().version()).isSnapshot())
				return true;
		} catch (IntinoException e) {
			return false;
		}

		return configuration.artifact().dependencies().stream().anyMatch(d -> {
			try {
				return new Version(d.version()).isSnapshot();
			} catch (IntinoException e) {
				return false;
			}
		});
	}

	private void checkoutMasterAndMerge() {
		withSyncTask("Checking out to Master and merging", () -> {
			stash = "intino:" + module.getName() + ":" + Instant.now().toString();
			GitUtil.stashChanges(module, stash);
			GitCommandResult result = GitUtil.checkoutTo(module, "master");
			if (!result.success()) {
				errorMessages.add("git error:\n" + String.join("\n", result.getErrorOutput()));
				return;
			}
			result = GitUtil.pull(module, "master");
			if (!result.success()) {
				errorMessages.add("git error:\n" + String.join("\n", result.getErrorOutput()));
				return;
			}
			result = GitUtil.mergeBranchIntoCurrent(module, startingBranch);
			if (!result.success()) errorMessages.add("git error:\n" + String.join("\n", result.getErrorOutput()));
		});
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
				if (!(configuration instanceof LegioConfiguration)) return;
				AbstractArtifactFactory.ProcessResult result = process(indicator);
				if (indicator.isCanceled()) return;
				if (callback != null) ApplicationManager.getApplication().invokeLater(() -> callback.onFinish(result));
				if (!result.equals(ProcessResult.Retry)) {
					task(() -> {
						if ("master".equals(currentBranch(module))) {
							GitUtil.checkoutTo(module, startingBranch);
							GitUtil.popStash(module, stash);
						}
					});
					ApplicationManager.getApplication().invokeAndWait(() -> {
						if (!errorMessages.isEmpty()) notifyErrors();
						else if (result.equals(ProcessResult.Done)) processSuccessMessages();
						reloadProject();
					});
				}
			}
		});
	}

	private void task(Runnable runnable) {
		ProgressManager.getInstance().runProcess(runnable, null);
	}

	private <T> T task(Computable<T> runnable) {
		return ProgressManager.getInstance().runProcess(runnable, null);
	}

	private boolean withSyncTask(String title, Runnable runnable) {
		return ProgressManager.getInstance().runProcessWithProgressSynchronously(runnable, title, false, this.project);
	}

	private void notifyErrors() {
		Bus.notify(new Notification("Tara Language", MessageProvider.message("error.occurred", phase.gerund().toLowerCase()), errorMessages.get(0), NotificationType.ERROR), project);
	}

	private boolean needsToRebuild() {
		Configuration.Artifact.Model model = safe(() -> configuration.artifact().model());
		if (model == null) return false;
		File languageFile = LanguageManager.getLanguageFile(model.outLanguage(), configuration.artifact().version());
		return checker.shouldDistributeLanguage(module, phase) && !languageFile.exists();
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
		final String message = String.join("\n", successMessages);
		notify(module.getProject(), module.getName(), message);
		String notificationMessage = MessageProvider.message(checker.shouldDistributeLanguage(module, phase) ? "success.language.publish.message" : "success.publish.message",configuration.artifact().name(), phase.participle());
		if (phase.equals(FactoryPhase.DEPLOY))
			notificationMessage = "Deployment of " + configuration.artifact().name() + " requested";
		notify(module.getProject(), module.getName(), notificationMessage);
	}

	private void notify(Project project, String title, String body) {
		Bus.notify(new Notification("Tara Language", title, body, NotificationType.INFORMATION), project);
	}

	public interface FinishCallback {
		void onFinish(ProcessResult result);
	}
}