package io.intino.plugin.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.util.messages.MessageBus;
import io.intino.plugin.IntinoIcons;
import io.intino.plugin.toolwindows.output.IntinoTopics;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

public class IntinoGenerationAction extends IntinoAction {
	private static final char NL = '\n';
	private static final String ENCODING = "UTF-8";
	private boolean isConnected = false;
	private Set<PsiFile> pendingFiles = new HashSet<>();

	@Override
	public void actionPerformed(AnActionEvent e) {
		Module module = e.getData(LangDataKeys.MODULE);
		execute(module);
	}

	@Override
	public void execute(Module module) {
		if (module == null) return;
		pendingFiles.clear();
	}

	public void force(Module module) {
		if (module == null) return;
//		model(module);
	}

	@Override
	public void update(AnActionEvent e) {
		super.update(e);
		final Project project = e.getProject();
		if (!isConnected && project != null) {
			final MessageBus messageBus = project.getMessageBus();
			messageBus.connect().subscribe(IntinoTopics.FILE_MODIFICATION, file -> {
				final VirtualFile vFile = VfsUtil.findFileByIoFile(new File(file), true);
				if (vFile == null) return;
				pendingFiles.add(PsiManager.getInstance(project).findFile(vFile));
			});
			isConnected = true;
		}
		e.getPresentation().setVisible(!pendingFiles.isEmpty());
		e.getPresentation().setIcon(IntinoIcons.GENARATION_16);
		e.getPresentation().setText("Generate intino code");
	}
}
