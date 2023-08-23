package io.intino.plugin.actions.itrules;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManagerListener;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import io.intino.plugin.file.ItrulesFileType;
import org.jetbrains.annotations.NotNull;


public class ItrFileDocumentManagerListener implements FileDocumentManagerListener {

	private final Project project;
	private final TemplateGeneration generator;

	public ItrFileDocumentManagerListener(Project project) {
		this.project = project;
		generator = new TemplateGeneration();
	}

	@Override
	public void beforeDocumentSaving(@NotNull Document document) {
		if (!project.isInitialized()) return;
		final PsiFile psiFile = PsiDocumentManager.getInstance(project).getPsiFile(document);
		if (psiFile != null && psiFile.getModificationStamp() != 0 && psiFile.getFileType().equals(ItrulesFileType.instance()))
			ApplicationManager.getApplication().invokeLater(() -> generator.createTemplate(project, psiFile.getVirtualFile()), ModalityState.nonModal());
	}

}
