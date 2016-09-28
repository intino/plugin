package org.siani.legio.plugin.project;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManagerListener;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.siani.legio.plugin.file.LegioFileType;
import tara.intellij.project.configuration.Configuration;
import tara.intellij.project.configuration.ConfigurationManager;

import static tara.intellij.project.module.ModuleProvider.moduleOf;

public class ConfigurationListener implements FileDocumentManagerListener {

	@Override
	public void beforeAllDocumentsSaving() {
	}

	@Override
	public void beforeDocumentSaving(@NotNull Document document) {
		final Project[] openProjects = ProjectManager.getInstance().getOpenProjects();
		for (Project project : openProjects) {
			if (!project.isInitialized()) continue;
			final PsiFile psiFile = PsiDocumentManager.getInstance(project).getPsiFile(document);
			if (psiFile != null && psiFile.getModificationStamp() != 0 && psiFile.getFileType().equals(LegioFileType.INSTANCE)) {
				final Module module = moduleOf(psiFile);
				if	(module == null) return;
				ApplicationManager.getApplication().invokeLater(() -> reloadConfiguration(module), ModalityState.NON_MODAL);
			}
		}
	}

	private void reloadConfiguration(Module module) {
		final Configuration configuration = ConfigurationManager.configurationOf(module);
		if (configuration instanceof LegioConfiguration) configuration.reload();
	}

	@Override
	public void beforeFileContentReload(VirtualFile file, @NotNull Document document) {

	}

	@Override
	public void fileWithNoDocumentChanged(@NotNull VirtualFile file) {

	}

	@Override
	public void fileContentReloaded(@NotNull VirtualFile file, @NotNull Document document) {

	}

	@Override
	public void fileContentLoaded(@NotNull VirtualFile file, @NotNull Document document) {

	}

	@Override
	public void unsavedDocumentsDropped() {

	}
}
