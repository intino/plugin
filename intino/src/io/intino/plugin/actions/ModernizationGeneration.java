package io.intino.plugin.actions;

import com.intellij.ide.highlighter.XmlFileType;
import com.intellij.notification.NotificationGroup;
import com.intellij.notification.NotificationGroupManager;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.progress.impl.BackgroundableProcessIndicator;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import io.intino.goros.space.Shifter;
import io.intino.plugin.IntinoIcons;
import io.intino.plugin.file.GorosFileType;
import io.intino.plugin.lang.psi.impl.IntinoUtil;
import org.jetbrains.annotations.NotNull;

import java.io.File;

import static com.intellij.openapi.actionSystem.CommonDataKeys.PSI_FILE;

public class ModernizationGeneration extends AnAction {
	public void update(@NotNull AnActionEvent e) {
		e.getPresentation().setIcon(IntinoIcons.GOROS_13);
		PsiFile context = e.getData(PSI_FILE);
		if (context != null && isGorosFile(context))
			enable(e);
		else disable(e);
	}

	@Override
	public @NotNull ActionUpdateThread getActionUpdateThread() {
		return ActionUpdateThread.BGT;
	}

	@Override
	public void actionPerformed(@NotNull AnActionEvent e) {
		Module module = e.getData(LangDataKeys.MODULE);
		PsiFile context = e.getData(PSI_FILE);
		if (context == null) return;
		try {
			withTask(new Task.Backgroundable(module.getProject(), module.getName() + ": Modernizing to Goros", false) {
				@Override
				public void run(@NotNull ProgressIndicator indicator) {
					try {
						Shifter.main(new String[]{context.getVirtualFile().getPath()});
						refreshFiles(IntinoUtil.moduleRoot(module));
					} catch (Exception ex) {
						NotificationGroup balloon = NotificationGroup.findRegisteredGroup("Intino");
						if (balloon == null)
							balloon = NotificationGroupManager.getInstance().getNotificationGroup("Intino");
						balloon.createNotification("Error during modernization: " + ex.getMessage(), MessageType.ERROR).
								setImportant(false).notify(module.getProject());
					}
				}
			});
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

	private boolean isGorosFile(PsiFile context) {
		return context.getFileType().equals(GorosFileType.instance()) || context.getFileType().equals(XmlFileType.INSTANCE) && context.getOriginalFile().getName().endsWith(GorosFileType.INSTANCE.getDefaultExtension());
	}
}
