package io.intino.plugin.build;

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
import com.intellij.openapi.module.ModuleTypeWithWebFeatures;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.progress.impl.BackgroundableProcessIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.refactoring.ui.InfoDialog;
import git4idea.GitBranch;
import git4idea.commands.GitCommandResult;
import io.intino.Configuration;
import io.intino.plugin.IntinoException;
import io.intino.plugin.MessageProvider;
import io.intino.plugin.build.git.GitUtil;
import io.intino.plugin.lang.LanguageManager;
import io.intino.plugin.lang.file.TaraFileType;
import io.intino.plugin.lang.psi.impl.IntinoUtil;
import io.intino.plugin.project.configuration.ArtifactLegioConfiguration;
import io.intino.plugin.project.configuration.Version;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import static com.intellij.configurationStore.StoreReloadManager.Companion;
import static io.intino.plugin.TerminalWindow.runCommand;
import static io.intino.plugin.build.FactoryPhaseChecker.collectModuleDependencies;
import static io.intino.plugin.build.git.GitUtil.currentBranch;

public class ArtifactFactory extends AbstractArtifactFactory {
	private String stash;

	public ArtifactFactory(Module module, FactoryPhase phase) {
		super(module, phase);
	}

	public void build(FinishCallback callback) {
		if (configuration.artifact() == null || configuration.artifact().name() == null) return;
		boolean distributed = isDistributed(configuration.artifact());
		if (configuration.isReloading()) {
			errorMessages.add("Artifact cannot be " + phase.participle() + " during reloading");
			notifyErrors();
			return;
		}
		if (includeDistribution(phase) && !distributed && !isSnapshot()) {
			if (hasChanges()) {
				errorMessages.add("Module has changes. Please commit them and retry.");
				notifyErrors();
				return;
			}
//			if (hasSnapshotDependencies()) {
//				errorMessages.add("A release version must not have SNAPSHOT dependencies.");
//				notifyErrors();
//				return;
//			}
			try {
				checker.check(phase, configuration);
				if (startingBranch != null && !isSupportBranch() && !isHotFixBranch() && !isMasterBranch() && !askForReleaseDistribute())
					return;
				if (startingBranch != null && !isSupportBranch() && !isHotFixBranch() && !isMasterBranch())
					checkoutMainAndMerge();
			} catch (IntinoException e) {
				errorMessages.add(e.getMessage());
			}
		}
		if (!errorMessages.isEmpty()) {
			notifyErrors();
			return;
		}
		if (phase == FactoryPhase.DEPLOY && !isSnapshot() && distributed)
			process(callback, !isSnapshot() && distributed);
		else {
			final CompilerManager compilerManager = CompilerManager.getInstance(project);
			CompileScope scope = compilerManager.createModulesCompileScope(new Module[]{module}, true);
			if (needsToRebuild()) compilerManager.compile(scope, processArtifact(callback));
			else compilerManager.make(scope, processArtifact(callback));
		}
		if (isHotFixBranch() || isSupportBranch()) noticeUserAboutChanges();
	}

	private void noticeUserAboutChanges() {
		new InfoDialog("You are in " + startingBranch + " branch. Consider rolling these changes into the development branch and the master branch.", project).show();

	}

	private void compileUI() {
		webDependencies(module).forEach(m -> runCommand(module.getProject(), m, "Building UI Components", "npm run build"));
	}

	public List<String> webDependencies(Module module) {
		return collectModuleDependencies(module, new HashSet<>()).stream().filter(ModuleTypeWithWebFeatures::isAvailable).map(ModuleUtil::getModuleDirPath).toList();
	}

	private boolean hasChanges() {
		return Arrays.stream(ModuleRootManager.getInstance(module).getContentRoots()).anyMatch(vf -> GitUtil.isModified(module, vf));
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

	private void checkoutMainAndMerge() {
		withSyncTask("Checking out to Main and merging", () -> {
			stash = "intino:" + module.getName() + ":" + Instant.now().toString();
			GitUtil.stashChanges(module, stash);
			GitBranch main = GitUtil.mainBranch(module);
			GitCommandResult result = GitUtil.checkoutTo(module, main.getName());
			if (!result.success()) {
				errorMessages.add("git error:\n" + String.join("\n", result.getErrorOutput()));
				return;
			}
			result = GitUtil.pull(module, main.getName());
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
			if (!aborted && errors == 0) process(callback, false);
		};
	}

	private void process(FinishCallback callback, boolean distributed) {
		saveAll();
		withTask(new Task.Backgroundable(project, firstUpperCase(phase.gerund()) + " Artifact", true) {
			@Override
			public void run(@NotNull ProgressIndicator indicator) {
				if (!distributed) {
					if (!(configuration instanceof ArtifactLegioConfiguration)) return;
					if (!checker.webServiceIsCompiled(module)) {
						indicator.setText("Building UI services");
						compileUI();
					}
					if (!checker.webServiceIsCompiled(module)) {
						errorMessages.add("Impossible to build UI services. Please build them manually");
						notifyErrors();
						return;
					}
				}
				ProcessResult result = process(indicator);
				if (indicator.isCanceled()) return;
				if (callback != null) ApplicationManager.getApplication().invokeLater(() -> callback.onFinish(result));
				if (!result.equals(ProcessResult.Retry)) {
					task(() -> {
						if (startingBranch != null && !startingBranch.equals(currentBranch(module))) {
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

	private void withSyncTask(String title, Runnable runnable) {
		ProgressManager.getInstance().runProcessWithProgressSynchronously(runnable, title, false, this.project);
	}

	private void notifyErrors() {
		Bus.notify(new Notification("Intino", MessageProvider.message("error.occurred", phase.gerund().toLowerCase()), errorMessages.get(0), NotificationType.ERROR), project);
	}

	private boolean needsToRebuild() {
		var dsls = configuration.artifact().dsls();
		if (dsls.isEmpty()) return false;
		for (Configuration.Artifact.Dsl dsl : dsls) {
			Configuration.Artifact.Dsl.OutputDsl outputDsl = dsl.outputDsl();
			if (outputDsl == null) continue;
			File languageFile = LanguageManager.getLanguageFile(outputDsl.name(), outputDsl.version());
			if (checker.shouldDistributeLanguage(phase, module, dsl) && !languageFile.exists() && hasDslFiles(((ArtifactLegioConfiguration) configuration).module()))
				return true;
		}
		return false;
	}

	private boolean hasDslFiles(Module module) {
		VirtualFile srcRoot = IntinoUtil.getSrcRoot(module);
		if (!srcRoot.exists()) return false;
		return !FileUtils.listFiles(srcRoot.toNioPath().toFile(), new String[]{TaraFileType.INSTANCE.getDefaultExtension()}, true).isEmpty();
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
		Companion.getInstance(project).blockReloadingProjectOnExternalChanges();
	}

	private void reloadProject() {
		SaveAndSyncHandler.getInstance().refreshOpenFiles();
		VirtualFileManager.getInstance().refreshWithoutFileWatcher(false);
		Companion.getInstance(project).unblockReloadingProjectOnExternalChanges();
	}

	private void processSuccessMessages() {
		final String message = String.join("\n", successMessages);
		if (!message.isEmpty()) notify(module.getProject(), module.getName(), message);

		String notificationMessage = MessageProvider.message(shouldDistributeAnyDsl() ? "success.language.publish.message" : "success.publish.message", configuration.artifact().name(), phase.participle());
		if (phase.equals(FactoryPhase.DEPLOY))
			notificationMessage = "Deployment of " + configuration.artifact().name() + " requested";
		notify(module.getProject(), module.getName(), notificationMessage);
	}

	private boolean shouldDistributeAnyDsl() {
		return configuration.artifact().dsls().stream().anyMatch(d -> checker.shouldDistributeLanguage(phase, module, d));
	}

	private void notify(Project project, String title, String body) {
		Bus.notify(new Notification("Intino", title, body, NotificationType.INFORMATION), project);
	}

	public interface FinishCallback {
		void onFinish(ProcessResult result);
	}
}