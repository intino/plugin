package io.intino.plugin.actions;

import com.intellij.notification.NotificationGroup;
import com.intellij.notification.NotificationGroupManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.module.WebModuleType;
import com.intellij.openapi.module.WebModuleTypeBase;
import com.intellij.openapi.progress.PerformInBackgroundOption;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.progress.impl.BackgroundableProcessIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.psi.PsiFile;
import io.intino.Configuration;
import io.intino.plugin.archetype.ArchetypeRenderer;
import io.intino.plugin.lang.psi.impl.IntinoUtil;
import io.intino.plugin.project.configuration.LegioConfiguration;
import org.jetbrains.annotations.NotNull;

import static com.intellij.openapi.actionSystem.CommonDataKeys.PSI_FILE;

public class ArchetypeGeneration extends AnAction {
	public void actionPerformed(AnActionEvent e) {
		PsiFile data = e.getData(PSI_FILE);
		if (data == null) return;
		this.withTask(new Task.Backgroundable(e.getProject(), e.getProject().getName() + ": Generating archetypes...", false, PerformInBackgroundOption.ALWAYS_BACKGROUND) {
			@Override
			public void run(@NotNull ProgressIndicator progressIndicator) {
				for (Module module : ModuleManager.getInstance(e.getProject()).getModules()) {
					if (WebModuleType.is(module, WebModuleTypeBase.getInstance())) continue;
					Configuration cnf = IntinoUtil.configurationOf(module);
					if (cnf != null && cnf.artifact().box() != null && cnf.artifact().packageConfiguration().isRunnable())
						new ArchetypeRenderer(module, (LegioConfiguration) cnf).render(VfsUtil.virtualToIoFile(data.getVirtualFile()));
				}
				notifySuccess(e.getProject());
			}
		});
	}

	private void notifySuccess(Project project) {
		NotificationGroup balloon = NotificationGroup.findRegisteredGroup("Intino");
		if (balloon == null) balloon = NotificationGroupManager.getInstance().getNotificationGroup("Intino");
		balloon.createNotification("Archetype reloaded", MessageType.INFO).setImportant(false).notify(project);
	}

	private void withTask(Task.Backgroundable runnable) {
		ProgressManager.getInstance().runProcessWithProgressAsynchronously(runnable, new BackgroundableProcessIndicator(runnable));
	}

	@Override
	public void update(@NotNull AnActionEvent e) {
		super.update(e);
		final Project project = e.getProject();
		PsiFile fileContext = e.getData(PSI_FILE);
		boolean visible = project != null && fileContext != null && fileContext.getName().equalsIgnoreCase(".archetype");
		e.getPresentation().setVisible(visible);
		e.getPresentation().setEnabled(visible);
	}
}
