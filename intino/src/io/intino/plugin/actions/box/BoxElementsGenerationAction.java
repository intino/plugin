package io.intino.plugin.actions.box;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationGroup;
import com.intellij.notification.NotificationGroupManager;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.progress.PerformInBackgroundOption;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.progress.impl.BackgroundableProcessIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;
import io.intino.Configuration;
import io.intino.builder.BuildConstants;
import io.intino.plugin.IntinoException;
import io.intino.plugin.IntinoIcons;
import io.intino.plugin.actions.IntinoAction;
import io.intino.plugin.lang.psi.impl.IntinoUtil;
import io.intino.plugin.project.configuration.ArtifactLegioConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import static com.intellij.notification.NotificationType.ERROR;
import static io.intino.plugin.project.Safe.safe;

public class BoxElementsGenerationAction extends IntinoAction {
	private static final Logger Logger = com.intellij.openapi.diagnostic.Logger.getInstance(BoxElementsGenerationAction.class);

	@Override
	public void update(AnActionEvent e) {
		super.update(e);
		final Project project = e.getProject();
		Module module = e.getData(LangDataKeys.MODULE);
		boolean enable = project != null && module != null;
		Configuration configuration = IntinoUtil.configurationOf(module);
		if (!(configuration instanceof ArtifactLegioConfiguration)) enable = false;
		else if (safe(() -> configuration.artifact().dsls().isEmpty(), false)) enable = false;
		e.getPresentation().setVisible(enable);
		e.getPresentation().setEnabled(enable);
		e.getPresentation().setIcon(IntinoIcons.GENARATION_16);
		e.getPresentation().setText("Generate Web Elements Code");
	}

	@Override
	public void actionPerformed(AnActionEvent e) {
		execute(e.getData(LangDataKeys.MODULE));
	}

	@Override
	public void execute(Module module) {
		final Configuration configuration = IntinoUtil.configurationOf(module);
		if (!(configuration instanceof ArtifactLegioConfiguration) || safe(() -> configuration.artifact().dsls().isEmpty(), false))
			return;
		withTask(new Task.Backgroundable(module.getProject(), module.getName() + ": Reloading box elements", false, PerformInBackgroundOption.ALWAYS_BACKGROUND) {
			@Override
			public void run(@NotNull ProgressIndicator indicator) {
				doExecute(module, (ArtifactLegioConfiguration) configuration);
			}
		});
	}

	private void doExecute(Module module, ArtifactLegioConfiguration configuration) {
		try {
			ApplicationManager.getApplication().invokeAndWait(() -> FileDocumentManager.getInstance().saveAllDocuments());
			Configuration.Artifact.Dsl dsl = safe(() -> configuration.artifact().dsl("Konos"));//TODO remove
			if (dsl == null) return;
			DslExportRunner runner = new DslExportRunner(module, configuration, dsl, BuildConstants.Mode.OnlyElements, null);
			runner.runExport();
			notify(module);
		} catch (IOException e) {
			Logger.error(e);
		} catch (IntinoException e) {
			notifyError(e.getMessage(), module);
		}
	}

	private void withTask(Task.Backgroundable runnable) {
		ProgressManager.getInstance().runProcessWithProgressAsynchronously(runnable, new BackgroundableProcessIndicator(runnable));
	}

	private void notify(Module module) {
		NotificationGroup balloon = NotificationGroup.findRegisteredGroup("Intino");
		if (balloon == null) balloon = NotificationGroupManager.getInstance().getNotificationGroup("Intino");
		balloon.createNotification(module.getName() + " Web elements " + "reloaded", MessageType.INFO).setImportant(false).notify(module.getProject());
	}


	private void notifyError(String message, Module module) {
		Notifications.Bus.notify(new Notification("Intino", "Elements cannot be generated. ", message, ERROR), module.getProject());
	}

}
