package io.intino.plugin.codeinsight.languageinjection;

import com.intellij.codeInsight.actions.ReformatCodeProcessor;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileDocumentManagerListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import org.jetbrains.annotations.NotNull;

public class JavaFragmentDocumentManagerListener implements FileDocumentManagerListener {

	private final Project project;

	public JavaFragmentDocumentManagerListener(Project project) {
		this.project = project;
	}

	@Override
	public void fileContentLoaded(@NotNull VirtualFile file, @NotNull Document document) {
		if (!project.isInitialized()) return;
		if (!file.getName().startsWith("Java Fragment")) return;
		FileViewProvider vp = FileDocumentManager.getInstance().findCachedPsiInAnyProject(file);
		if (vp == null || vp.getManager().getProject() != project) return;
		final PsiFile psiFile = PsiManager.getInstance(project).findFile(file);
		if (psiFile != null && (psiFile.getName().startsWith("Java Fragment")))
			ApplicationManager.getApplication().invokeLater(() -> new ReformatCodeProcessor(project, psiFile, null, false).run());
	}
}
