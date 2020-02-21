package io.intino.plugin.build;

import com.intellij.compiler.CompilerConfigurationImpl;
import com.intellij.compiler.server.CustomBuilderMessageHandler;
import com.intellij.configurationStore.StoreReloadManager;
import com.intellij.ide.SaveAndSyncHandler;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.refactoring.util.CommonRefactoringUtil;
import com.intellij.util.messages.MessageBusConnection;
import io.intino.Configuration;
import io.intino.plugin.lang.LanguageManager;
import io.intino.plugin.lang.psi.impl.TaraUtil;
import io.intino.tara.compiler.shared.TaraBuildConstants;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class TaraCompilerListener implements ProjectComponent {
	private static final Logger LOG = Logger.getInstance(TaraCompilerListener.class.getName());
	private static final String TARA_PATTERN = "!?*.tara";
	private final Project project;

	private MessageBusConnection messageBusConnection;

	public TaraCompilerListener(Project project) {
		this.project = project;
	}

	@Override
	public void initComponent() {
		messageBusConnection = project.getMessageBus().connect();
		messageBusConnection.subscribe(CustomBuilderMessageHandler.TOPIC, new FileInvalidationListener());
		fillResourcePatterns(new CompilerConfigurationImpl(project));
	}

	private void fillResourcePatterns(CompilerConfigurationImpl configuration) {
		final List<String> patterns = Arrays.asList(configuration.getResourceFilePatterns());
		if (!patterns.contains(TARA_PATTERN)) configuration.addResourceFilePattern(TARA_PATTERN);
		configuration.convertPatterns();
	}

	@Override
	public void disposeComponent() {
		messageBusConnection.disconnect();
	}

	private class FileInvalidationListener implements CustomBuilderMessageHandler {
		@Override
		public void messageReceived(String builderId, String messageType, String messageText) {
			if (TaraBuildConstants.TARAC.equals(builderId) && TaraBuildConstants.REFRESH_BUILDER_MESSAGE.equals(messageType)) {
				final String[] parameters = messageText.split(TaraBuildConstants.REFRESH_BUILDER_MESSAGE_SEPARATOR);
				refreshLanguage(parameters[0]);
				File directory = new File(parameters[parameters.length - 1]);
				refreshOut(parameters[0], directory);
				refreshDirectory(new File(directory.getParentFile(), "test-res"));
				refreshDirectory(new File(directory.getParentFile(), "res"));
				refreshDirectory(new File(directory.getParentFile(), "src"));
				refreshDirectory(new File(directory.getParentFile(), "test"));
			}
		}

		private void refreshLanguage(String moduleName) {
			Module module = ApplicationManager.getApplication().runReadAction((Computable<Module>) () -> ModuleManager.getInstance(project).findModuleByName(moduleName));
			final Configuration configuration = TaraUtil.configurationOf(module);
			if (configuration != null && configuration.artifact().model() != null) {
				LanguageManager.reloadLanguage(project, configuration.artifact().model().outLanguage(), configuration.artifact().model().outLanguageVersion());
			}
		}

		private void refreshOut(String outDsl, File file) {
			VirtualFile outDir = VfsUtil.findFileByIoFile(file, true);
			if (outDir == null || !outDir.isValid()) return;
			outDir.refresh(true, true/*,
			() -> reformatGeneratedCode(VfsUtil.findFileByIoFile(new File(file, outDSLFromInput.toLowerCase() + File.separator + "natives"), true))*/);
		}

		private void refreshDirectory(File res) {
			VirtualFile resDir = VfsUtil.findFileByIoFile(res, true);
			if (resDir == null || !resDir.isValid()) return;
			resDir.refresh(true, true);
		}

		private void reformatGeneratedCode(VirtualFile outDir) {
			if (outDir == null || !outDir.isValid()) return;
			FileDocumentManager.getInstance().saveAllDocuments();
			StoreReloadManager.getInstance().blockReloadingProjectOnExternalChanges();
			final DataContext result = io.intino.plugin.project.DataContext.getContext();
			Project project = result != null ? (Project) result.getData(PlatformDataKeys.PROJECT.getName()) : null;
			if (project == null) return;
			final PsiDirectory[] psiOutDirectory = new PsiDirectory[1];
			ApplicationManager.getApplication().runReadAction(() -> {
				psiOutDirectory[0] = PsiManager.getInstance(project).findDirectory(outDir);
			});
			if (psiOutDirectory[0] == null || !psiOutDirectory[0].isDirectory()) return;
			project.save();
			reformatAllFiles(project, psiOutDirectory[0]);
			reloadProject(project);
		}

		private void reformatAllFiles(Project project, PsiDirectory directory) {
			List<PsiElement> psiFiles = new ArrayList<>();
			try {
				ApplicationManager.getApplication().runReadAction(() -> {
					if (directory.getChildren().length != 0)
						Collections.addAll(psiFiles, directory.getChildren());
				});
				for (PsiElement file : psiFiles) {
					if (file instanceof PsiFile) reformat(project, (PsiFile) file);
					else if (file instanceof PsiDirectory) reformatAllFiles(project, (PsiDirectory) file);
				}
			} catch (Exception e) {
				LOG.error(e.getMessage(), e);
			}
		}

		private void reloadProject(Project project) {
			SaveAndSyncHandler.getInstance().refreshOpenFiles();
			VirtualFileManager.getInstance().refreshWithoutFileWatcher(false);
			StoreReloadManager.getInstance().unblockReloadingProjectOnExternalChanges();
			refreshFiles(project);
		}

		private void refreshFiles(Project project) {
			for (VirtualFile file : FileEditorManager.getInstance(project).getOpenFiles()) file.refresh(true, false);
		}

		private void reformat(final Project project, final PsiFile file) {
			WriteCommandAction.writeCommandAction(project, file).run(() -> {
				assert CommonRefactoringUtil.checkReadOnlyStatus(project, file);
				CodeStyleManager.getInstance(project).reformat(file, true);
			});
		}

	}
}

