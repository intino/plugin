package io.intino.plugin.toolwindows.project;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.util.messages.MessageBus;
import com.intellij.util.messages.MessageBusConnection;
import io.intino.plugin.console.IntinoTopics;
import io.intino.plugin.file.konos.KonosFileType;
import io.intino.tara.plugin.lang.file.TaraFileType;
import org.jetbrains.annotations.NotNull;

public class FileDocumentManagerListener implements com.intellij.openapi.fileEditor.FileDocumentManagerListener {
	public void beforeAllDocumentsSaving() {
	}

	public void beforeDocumentSaving(@NotNull Document document) {
	}

	public void beforeFileContentReload(VirtualFile file, @NotNull Document document) {

	}

	public void fileWithNoDocumentChanged(@NotNull VirtualFile file) {
	}

	public void fileContentReloaded(@NotNull VirtualFile file, @NotNull Document document) {
	}

	public void fileContentLoaded(@NotNull VirtualFile file, @NotNull Document document) {
		final Project[] openProjects = ProjectManager.getInstance().getOpenProjects();
		for (Project project : openProjects) {
			if (!project.isInitialized()) continue;
			final PsiFile psiFile = PsiDocumentManager.getInstance(project).getPsiFile(document);
			if (psiFile != null && (TaraFileType.instance().equals(psiFile.getFileType()) || KonosFileType.instance().equals(psiFile.getFileType()))) {
				document.addDocumentListener(new DocumentListener() {
					public void beforeDocumentChange(DocumentEvent event) {
					}
					public void documentChanged(DocumentEvent event) {
						publish(psiFile);
					}
				});
			}
		}
	}

	@Override
	public void unsavedDocumentsDropped() {

	}

	private void publish(PsiFile file) {
		final MessageBus messageBus = file.getProject().getMessageBus();
		final IntinoFileListener legioListener = messageBus.syncPublisher(IntinoTopics.FILE_MODIFICATION);
		legioListener.modified(file.getOriginalFile().getVirtualFile().getPath());
		final MessageBusConnection connect = messageBus.connect();
		connect.deliverImmediately();
		connect.disconnect();
	}
}
