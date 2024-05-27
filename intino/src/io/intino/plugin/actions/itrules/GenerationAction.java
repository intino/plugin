package io.intino.plugin.actions.itrules;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDocumentManager;
import io.intino.plugin.actions.ActionUtils;
import io.intino.plugin.file.ItrulesFileType;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class GenerationAction extends AnAction implements DumbAware {
	public static final Logger LOG = Logger.getInstance("itrules: Generate");


	protected List<VirtualFile> itrFiles(AnActionEvent e) {
		List<VirtualFile> rulesFiles = ActionUtils.getFilesFromEvent(e, ItrulesFileType.instance().getDefaultExtension());
		LOG.info("actionPerformed " + (rulesFiles.isEmpty() ? "NONE" : rulesFiles));
		if (rulesFiles.isEmpty()) return null;
		return rulesFiles;
	}

	protected List<VirtualFile> javaFiles(AnActionEvent e) {
		return ActionUtils.getFilesFromEvent(e, "java");
	}

	protected boolean projectExists(AnActionEvent e, Project project) {
		if (project == null) {
			LOG.error("actionPerformed: no project for " + e);
			return true;
		}
		return false;
	}

	protected void refreshFiles(File destination) {
		Set<File> generatedFiles = new HashSet<>();
		generatedFiles.add(destination);
		LocalFileSystem.getInstance().refreshIoFiles(generatedFiles, true, false, null);
		final VirtualFile vFile = VfsUtil.findFileByIoFile(destination, true);
		if (vFile != null) VfsUtil.markDirtyAndRefresh(true, false, false, vFile);
	}

	protected boolean checkDocument(Project project, VirtualFile rulesFile) {
		PsiDocumentManager documentManager = PsiDocumentManager.getInstance(project);
		FileDocumentManager fileDocManager = FileDocumentManager.getInstance();
		Document doc = fileDocManager.getDocument(rulesFile);
		if (doc == null) return true;
		if ((!documentManager.isCommitted(doc)) || (fileDocManager.isDocumentUnsaved(doc))) {
			documentManager.commitDocument(doc);
			fileDocManager.saveDocument(doc);
		}
		return false;
	}

	protected Module getModuleOf(Project project, VirtualFile file) {
		return ProjectRootManager.getInstance(project).getFileIndex().getModuleForFile(file);
	}

}
