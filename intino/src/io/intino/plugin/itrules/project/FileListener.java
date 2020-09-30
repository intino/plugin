package io.intino.plugin.itrules.project;

import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.newvfs.BulkFileListener;
import com.intellij.openapi.vfs.newvfs.events.VFileDeleteEvent;
import com.intellij.openapi.vfs.newvfs.events.VFileEvent;
import com.intellij.openapi.vfs.newvfs.events.VFileMoveEvent;
import com.intellij.openapi.vfs.newvfs.events.VFilePropertyChangeEvent;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiManager;
import com.intellij.refactoring.openapi.impl.JavaRenameRefactoringImpl;
import io.intino.plugin.itrules.lang.file.ItrulesFileType;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

public class FileListener implements BulkFileListener {
	@Override
	public void after(@NotNull List<? extends VFileEvent> events) {
		for (VFileEvent event : events) {
			if (event instanceof VFilePropertyChangeEvent) propertyChanged((VFilePropertyChangeEvent) event);
			else if (event instanceof VFileDeleteEvent) fileDeleted((VFileDeleteEvent) event);
			else if (event instanceof VFileMoveEvent) fileMoved((VFileMoveEvent) event);
		}
	}

	public void propertyChanged(@NotNull VFilePropertyChangeEvent event) {
		final VirtualFile file = event.getFile();
		if (ItrulesFileType.instance().getDefaultExtension().equals(file.getExtension()) && VirtualFile.PROP_NAME.equals(event.getPropertyName())) {
			VirtualFile template = findOldJava(event.getOldValue().toString(), event.getFile().getParent());
			final Project project = project();
			if (template != null && project != null) {
				final PsiJavaFile psiFile = (PsiJavaFile) PsiManager.getInstance(project).findFile(template);
				final String newName = event.getNewValue().toString().replace(".itr", "Template");
				JavaRenameRefactoringImpl renamer = new JavaRenameRefactoringImpl(project, psiFile.getClasses()[0], newName, false, false);
				renamer.doRefactoring(renamer.findUsages());
			}
		}
	}

	public void fileDeleted(@NotNull VFileDeleteEvent event) {
		final VirtualFile file = event.getFile();
		if (ItrulesFileType.instance().getDefaultExtension().equals(file.getExtension())) {
			VirtualFile template = findOldJava(file, event.getFile().getParent());
			if (template != null) try {
				template.delete(event.getRequestor());
			} catch (IOException ignored) {
			}
		}
	}

	public void fileMoved(@NotNull VFileMoveEvent event) {
		final VirtualFile file = event.getFile();
		if (!ItrulesFileType.instance().getDefaultExtension().equals(file.getExtension())) return;
		final VirtualFile oldJavaTemplate = findOldJava(file, event.getOldParent());
		if (oldJavaTemplate == null) return;
		final Project project = project();
		if (project == null) return;
		final VirtualFile newParent = event.getNewParent();
		final PsiManager manager = PsiManager.getInstance(project);
		final PsiJavaFile psiFile = (PsiJavaFile) manager.findFile(oldJavaTemplate);
		final PsiDirectory destiny = manager.findDirectory(newParent);
//				ApplicationManager.getApplication().invokeAndWait(() -> {
//					JavaMoveClassesOrPackagesHandler handler = new JavaMoveClassesOrPackagesHandler();
//					handler.doMove(project, new PsiElement[]{psiFile}, handler.adjustTargetForMove(dataContext(), destiny), null);
//				});
		//TODO Si se está moviendo un paquete que contiene la plantilla, esto hace petar.

	}

	private VirtualFile findOldJava(String file, VirtualFile parent) {
		return parent != null ? parent.findChild(file.replace(ItrulesFileType.instance().getDefaultExtension(), "Template.java")) : null;
	}

	private VirtualFile findOldJava(VirtualFile file, VirtualFile parent) {
		return parent != null ? parent.findChild(file.getNameWithoutExtension() + "Template.java") : null;
	}

	private Project project() {
		final DataContext result = dataContext();
		return result != null ? (Project) result.getData(PlatformDataKeys.PROJECT.getName()) : null;
	}

	private DataContext dataContext() {
		try {
			return DataManager.getInstance().getDataContextFromFocusAsync().blockingGet(10);
		} catch (TimeoutException | ExecutionException e) {
			return null;
		}
	}

}