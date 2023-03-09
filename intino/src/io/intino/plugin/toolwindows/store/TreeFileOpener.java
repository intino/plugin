package io.intino.plugin.toolwindows.store;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import java.io.File;

class TreeFileOpener {
	private final Project project;

	TreeFileOpener(Project project) {
		this.project = project;
	}

	void openFile(TreePath path) {
		if (path == null) return;
		final Connection.File nodeFile = (Connection.File) ((DefaultMutableTreeNode) path.getLastPathComponent()).getUserObject();
		if (nodeFile.absolutePath() != null) open(new File(nodeFile.absolutePath()));
	}

	private void open(File tempFile) {
		final VirtualFile virtualFile = VfsUtil.findFileByIoFile(tempFile, true);
		if (virtualFile == null) return;
		final PsiFile psiFile = PsiManager.getInstance(this.project).findFile(virtualFile);
		try {
			if (psiFile != null) psiFile.navigate(true);
		} catch (Throwable ignored) {
		}
	}
}