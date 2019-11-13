package io.intino.plugin.itrules.project;

import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.*;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiManager;
import com.intellij.refactoring.move.moveClassesOrPackages.JavaMoveClassesOrPackagesHandler;
import com.intellij.refactoring.openapi.impl.JavaRenameRefactoringImpl;
import io.intino.plugin.itrules.lang.file.ItrulesFileType;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

public class FileListener implements com.intellij.openapi.components.BaseComponent {
	private static final Logger logger = Logger.getInstance(FileListener.class);

	@Override
	public void initComponent() {
		VirtualFileManager.getInstance().addVirtualFileListener(new VirtualFileListener() {
			@Override
			public void propertyChanged(@NotNull VirtualFilePropertyEvent event) {
				final VirtualFile file = event.getFile();
				if (ItrulesFileType.INSTANCE.getDefaultExtension().equals(file.getExtension()) && VirtualFile.PROP_NAME.equals(event.getPropertyName())) {
					VirtualFile template = findOldJava(event.getOldValue().toString(), event.getParent());
					final Project project = project();
					if (template != null && project != null) {
						final PsiJavaFile psiFile = (PsiJavaFile) PsiManager.getInstance(project).findFile(template);
						final String newName = event.getNewValue().toString().replace(".itr", "Template");
						JavaRenameRefactoringImpl renamer = new JavaRenameRefactoringImpl(project, psiFile.getClasses()[0], newName, false, false);
						renamer.doRefactoring(renamer.findUsages());
					}
				}
			}

			@Override
			public void fileDeleted(@NotNull VirtualFileEvent event) {
				final VirtualFile file = event.getFile();
				if (ItrulesFileType.INSTANCE.getDefaultExtension().equals(file.getExtension())) {
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
				if (!ItrulesFileType.INSTANCE.getDefaultExtension().equals(file.getExtension())) return;
				final VirtualFile oldJavaTemplate = findOldJava(file, event.getOldParent());
				if (oldJavaTemplate == null) return;
				final Project project = project();
				if (project == null) return;
				final VirtualFile newParent = event.getNewParent();
				final PsiManager manager = PsiManager.getInstance(project);
				final PsiJavaFile psiFile = (PsiJavaFile) manager.findFile(oldJavaTemplate);
				final PsiDirectory destiny = manager.findDirectory(newParent);
				ApplicationManager.getApplication().invokeAndWait(() -> {
					JavaMoveClassesOrPackagesHandler handler = new JavaMoveClassesOrPackagesHandler();
					handler.doMove(project, new PsiElement[]{psiFile}, handler.adjustTargetForMove(dataContext(), destiny), null);
				});

			}

			private VirtualFile findOldJava(String file, VirtualFile parent) {
				return parent != null ? parent.findChild(file.replace(ItrulesFileType.INSTANCE.getDefaultExtension(), "Template.java")) : null;
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
					logger.error(e);
					return null;
				}
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
		});
	}

	@Override
	public void disposeComponent() {

	}

	@NotNull
	@Override
	public String getComponentName() {
		return "Itrules File Listener";
	}
}