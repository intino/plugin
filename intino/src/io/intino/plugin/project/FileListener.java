package io.intino.plugin.project;

import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.BaseComponent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.*;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiManager;
import com.intellij.refactoring.move.moveClassesOrPackages.JavaMoveClassesOrPackagesHandler;
import com.intellij.refactoring.openapi.impl.JavaRenameRefactoringImpl;
import io.intino.plugin.lang.file.TaraFileType;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

/**
 * Listen remove, move or rename of tara files to propagate this change to the attached java class
 **/
public class FileListener implements BaseComponent {

	private VirtualFileListener listener;

	@Override
	public void initComponent() {
		listener = new VirtualFileListener() {
			@Override
			public void propertyChanged(@NotNull VirtualFilePropertyEvent event) {
				final VirtualFile file = event.getFile();
				if (TaraFileType.instance().getDefaultExtension().equals(file.getExtension()) && VirtualFile.PROP_NAME.equals(event.getPropertyName())) {
					VirtualFile template = findOldJava(event.getOldValue().toString(), event.getParent());
					final Project project = project();
					if (template != null && project != null) {
						final PsiJavaFile psiFile = (PsiJavaFile) PsiManager.getInstance(project).findFile(template);
						final String newName = event.getNewValue().toString().replace(".tara", "");
						JavaRenameRefactoringImpl renamer = new JavaRenameRefactoringImpl(project, psiFile.getClasses()[0], newName, false, false);
						renamer.doRefactoring(renamer.findUsages());
					}
				}
			}

			@Override
			public void fileDeleted(@NotNull VirtualFileEvent event) {
				final VirtualFile file = event.getFile();
				if (TaraFileType.instance().getDefaultExtension().equals(file.getExtension())) {
					VirtualFile template = findOldJava(file, event.getParent());
					if (template != null) try {
						template.delete(event.getRequestor());
					} catch (IOException ignored) {
					}
				}
			}

			@Override
			public void fileMoved(@NotNull VirtualFileMoveEvent event) {
				final VirtualFile file = event.getFile();
				if (!TaraFileType.instance().getDefaultExtension().equals(file.getExtension())) return;
				final VirtualFile oldJavaTemplate = findOldJava(file, event.getOldParent());
				if (oldJavaTemplate == null) return;
				final Project project = project();
				if (project == null) return;
				final VirtualFile newParent = event.getNewParent();
				final PsiManager manager = PsiManager.getInstance(project);
				final PsiJavaFile psiFile = (PsiJavaFile) manager.findFile(oldJavaTemplate);
				final PsiDirectory destiny = manager.findDirectory(newParent);
				ApplicationManager.getApplication().invokeLater(() -> {
					JavaMoveClassesOrPackagesHandler handler = new JavaMoveClassesOrPackagesHandler();
					handler.doMove(project, new PsiElement[]{psiFile}, handler.adjustTargetForMove(dataContext(), destiny), null);
				});
			}

			private VirtualFile findOldJava(String file, VirtualFile parent) {
				return parent != null ? parent.findChild(file.replace(TaraFileType.instance().getDefaultExtension(), "java")) : null;
			}

			private VirtualFile findOldJava(VirtualFile file, VirtualFile parent) {
				return parent != null ? parent.findChild(file.getNameWithoutExtension() + ".java") : null;
			}

			private Project project() {
				final DataContext result = dataContext();
				return result != null ? (Project) result.getData(PlatformDataKeys.PROJECT.getName()) : null;
			}

			private DataContext dataContext() {
				return io.intino.plugin.project.DataContext.getContext();
			}

			@Override
			public void contentsChanged(@NotNull VirtualFileEvent event) {

			}

			@Override
			public void fileCreated(@NotNull VirtualFileEvent event) {

			}

			@Override
			public void fileCopied(@NotNull VirtualFileCopyEvent event) {

			}

			@Override
			public void beforePropertyChange(@NotNull VirtualFilePropertyEvent event) {
			}

			@Override
			public void beforeContentsChange(@NotNull VirtualFileEvent event) {

			}

			@Override
			public void beforeFileDeletion(@NotNull VirtualFileEvent event) {

			}

			@Override
			public void beforeFileMovement(@NotNull VirtualFileMoveEvent event) {

			}
		};
		VirtualFileManager.getInstance().addVirtualFileListener(listener);
	}

	@Override
	public void disposeComponent() {
		VirtualFileManager.getInstance().removeVirtualFileListener(listener);
	}

	@NotNull
	@Override
	public String getComponentName() {
		return "Tara File Listener";
	}
}
