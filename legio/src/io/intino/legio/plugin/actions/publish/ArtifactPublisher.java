package io.intino.legio.plugin.actions.publish;

import com.intellij.ide.SaveAndSyncHandlerImpl;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.compiler.CompileStatusNotification;
import com.intellij.openapi.compiler.CompilerManager;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ex.ProjectManagerEx;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.util.ui.ConfirmationDialog;
import io.intino.legio.plugin.dependencyresolution.ArtifactoryConnector;
import tara.compiler.shared.Configuration;
import tara.intellij.lang.TaraIcons;
import tara.intellij.lang.psi.impl.TaraUtil;
import tara.intellij.settings.TaraSettings;

import java.io.IOException;
import java.util.List;

import static com.intellij.openapi.vcs.VcsShowConfirmationOption.STATIC_SHOW_CONFIRMATION;
import static io.intino.legio.plugin.MessageProvider.message;

public class ArtifactPublisher extends AbstractArtifactPublisher {
	private final Project project;
	private List<Module> modules;
	private Actions actions;

	public enum Actions {
		PACKAGE("clean", "package"), INSTALL("clean", "package", "install"), DISTRIBUTE("clean", "package", "install", "deploy"), PREDEPLOY, DEPLOY;

		private final String[] actions;

		public String[] actions() {
			return actions;
		}

		Actions(String... actions) {
			this.actions = actions;
		}
	}

	public ArtifactPublisher(Project project, final List<Module> modules, Actions action) {
		this.project = project;
		this.modules = modules;
		this.actions = action;
	}

	public void publish() {
		final CompilerManager compilerManager = CompilerManager.getInstance(project);
		compilerManager.make(compilerManager.createModulesCompileScope(modules.toArray(new Module[modules.size()]), true), publishArtifact());
	}

	private CompileStatusNotification publishArtifact() {
		return (aborted, errors, warnings, compileContext) -> {
			if (!aborted && errors == 0) doPublish();
		};
	}

	private void doPublish() {
		ApplicationManager.getApplication().invokeLater(() -> {
			publishLanguageAndFramework();
			if (!errorMessages.isEmpty())
				Notifications.Bus.notify(new Notification("Tara Language", message("error.occurred"), errorMessages.iterator().next(), NotificationType.ERROR), project);
			else processMessages(successMessages, modules);
		});
	}

	private void publishLanguageAndFramework() {
		saveAll(project);
		for (Module module : modules)
			if (checkOverrideVersion(module, extractDSL(module)) && !publish(module, actions)) return;
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
			return new ArtifactoryConnector(configuration.releaseRepositories(), configuration.snapshotRepository(), configuration.languageRepository()).versions(dsl).contains(version);
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

	private void processMessages(List<String> successMessages, List<Module> modules) {
		StringBuilder messageBuf = new StringBuilder();
		for (String message : successMessages) {
			if (messageBuf.length() != 0) messageBuf.append('\n');
			messageBuf.append(message);
		}
		final Module first = modules.get(0);
		notify(first.getProject(), messageBuf.toString(), modules.size() == 1 ?
				message("success.publish.message", first.getName()) : message("success.language.publishing.message"));
	}

	private void notify(Project project, String title, String body) {
		Notifications.Bus.notify(new Notification("Tara Language", title, body, NotificationType.INFORMATION), project);
	}

	private String extractDSL(Module module) {
		final Configuration conf = TaraUtil.configurationOf(module);
		return conf == null ? "" : conf.outDSL();
	}

}
