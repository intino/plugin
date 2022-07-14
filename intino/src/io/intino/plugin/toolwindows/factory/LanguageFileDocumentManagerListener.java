package io.intino.plugin.toolwindows.factory;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.fileEditor.FileDocumentManagerListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.impl.PsiManagerEx;
import com.intellij.util.messages.MessageBus;
import com.intellij.util.messages.MessageBusConnection;
import io.intino.plugin.file.KonosFileType;
import io.intino.plugin.lang.file.TaraFileType;
import io.intino.plugin.toolwindows.IntinoTopics;
import org.jetbrains.annotations.NotNull;

public class LanguageFileDocumentManagerListener implements FileDocumentManagerListener {

	private final Project project;

	public LanguageFileDocumentManagerListener(Project project) {
		this.project = project;
	}

	public void fileContentLoaded(@NotNull VirtualFile file, @NotNull Document document) {
		if (!project.isInitialized()) return;
		FileViewProvider vp = PsiManagerEx.getInstanceEx(project).getFileManager().findCachedViewProvider(file);
		if (vp == null || vp.getManager().getProject() != project) return;
		final PsiFile psiFile = PsiDocumentManager.getInstance(project).getPsiFile(document);
		if (psiFile != null && (TaraFileType.instance().equals(psiFile.getFileType()) || KonosFileType.instance().equals(psiFile.getFileType()))) {
			document.addDocumentListener(new DocumentListener() {
				public void beforeDocumentChange(@NotNull DocumentEvent event) {
				}

				public void documentChanged(@NotNull DocumentEvent event) {
					publish(psiFile);
				}
			});
		}
	}

	private void publish(PsiFile file) {
		final Project project = file.getProject();
		if (project.isDisposed()) return;
		final MessageBus messageBus = project.getMessageBus();
		final IntinoFileListener legioListener = messageBus.syncPublisher(IntinoTopics.FILE_MODIFICATION);
		legioListener.modified(file.getOriginalFile().getVirtualFile().getPath());
		final MessageBusConnection connect = messageBus.connect();
		connect.deliverImmediately();
		connect.disconnect();
	}
}
