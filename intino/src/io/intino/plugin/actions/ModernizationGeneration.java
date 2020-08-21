package io.intino.plugin.actions;

import com.intellij.notification.NotificationGroup;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.progress.PerformInBackgroundOption;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.progress.impl.BackgroundableProcessIndicator;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import io.intino.plugin.IntinoIcons;
import io.intino.plugin.file.goros.GorosFileType;
import org.jetbrains.annotations.NotNull;

import java.io.File;

import static com.intellij.openapi.actionSystem.CommonDataKeys.PSI_FILE;

public class ModernizationGeneration extends AnAction {
	public void update(@NotNull AnActionEvent e) {
		e.getPresentation().setIcon(IntinoIcons.GOROS_13);
		PsiFile context = e.getData(PSI_FILE);
		if (context != null && context.getFileType().equals(GorosFileType.INSTANCE)) enable(e);
		else disable(e);
	}

	@Override
	public void actionPerformed(@NotNull AnActionEvent e) {
		Module module = e.getData(LangDataKeys.MODULE);
		PsiFile context = e.getData(PSI_FILE);
		if (context == null) return;
		try {
			withTask(new Task.Backgroundable(module.getProject(), module.getName() + ": Modernizing to Goros", false, PerformInBackgroundOption.ALWAYS_BACKGROUND) {
				@Override
				public void run(@NotNull ProgressIndicator indicator) {
					try {
						io.intino.goros.Shifter.main(new String[]{context.getVirtualFile().getPath()});
					} catch (Exception ex) {
						final NotificationGroup balloon = NotificationGroup.balloonGroup("tara");
						balloon.createNotification("Error during modernization: " + ex.getMessage(), MessageType.ERROR).
								setImportant(false).notify(module.getProject());
					}
				}
			});
			refreshFiles(new File(module.getModuleFilePath()).getParentFile());
		} catch (Throwable ignored) {

		}
	}

	private void refreshFiles(File dir) {
		VirtualFile vDir = VfsUtil.findFileByIoFile(dir, true);
		if (vDir == null || !vDir.isValid()) return;
		VfsUtil.markDirtyAndRefresh(true, true, true, vDir);
		vDir.refresh(true, true);
	}


	private void withTask(Task.Backgroundable runnable) {
		ProgressManager.getInstance().runProcessWithProgressAsynchronously(runnable, new BackgroundableProcessIndicator(runnable));
	}


	private static void enable(AnActionEvent e) {
		e.getPresentation().setEnabled(true);
		e.getPresentation().setVisible(true);
	}

	private static void disable(AnActionEvent e) {
		e.getPresentation().setVisible(false);
		e.getPresentation().setEnabled(false);
	}
}
