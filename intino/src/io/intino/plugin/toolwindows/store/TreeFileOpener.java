package io.intino.plugin.toolwindows.store;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import io.intino.alexandria.zim.ZimExtractor;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class TreeFileOpener {


	private Project project;

	public TreeFileOpener(Project project) {
		this.project = project;
	}

	void openFile(TreePath path) {
		if (path == null) return;
		final Connection.File nodeFile = (Connection.File) ((DefaultMutableTreeNode) path.getLastPathComponent()).getUserObject();
		if (nodeFile.name().endsWith(".zim")) open(decompress(nodeFile.content()));
		else if (nodeFile.absolutePath() != null) open(new File(nodeFile.absolutePath()));
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

	private File decompress(InputStream content) {
		try {
			File temp = FileUtil.createTempDirectory("", "zim");
			ZimExtractor.of(content).to(temp);
			return new File(temp, "zim.inl");
		} catch (IOException e) {
			Logger.getInstance(this.getClass()).error(e);
		}

		return null;
	}
}
