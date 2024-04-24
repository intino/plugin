package io.intino.plugin.build;

import com.intellij.compiler.CompilerConfiguration;
import com.intellij.compiler.CompilerConfigurationImpl;
import com.intellij.configurationStore.StoreReloadManager;
import com.intellij.ide.SaveAndSyncHandler;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
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
import io.intino.Configuration;
import io.intino.builder.BuildConstants;
import io.intino.plugin.build.postcompileactions.PostCompileActionFactory;
import io.intino.plugin.lang.LanguageManager;
import io.intino.plugin.lang.psi.impl.IntinoUtil;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static io.intino.builder.BuildConstants.REFRESH_MESSAGE_SEPARATOR;
import static io.intino.builder.BuildConstants.SEPARATOR;
import static io.intino.plugin.build.PostCompileAction.FinishStatus.RequiresReload;
import static io.intino.plugin.project.Safe.safe;

public class DslCompilerListener implements com.intellij.compiler.server.CustomBuilderMessageHandler {
	private static final Logger LOG = Logger.getInstance(DslCompilerListener.class.getName());
	private static final String TARA_PATTERN = "!?*.tara";
	private static final String TARAC = "tarac";
	private final Project project;

	public DslCompilerListener(Project project) {
		this.project = project;
		try {
			fillResourcePatterns((CompilerConfigurationImpl) CompilerConfiguration.getInstance(project));
		} catch (Throwable e) {
			LOG.error(e);
		}
	}

	private void fillResourcePatterns(CompilerConfigurationImpl configuration) {
		final List<String> patterns = Arrays.asList(configuration.getResourceFilePatterns());
		if (!patterns.contains(TARA_PATTERN)) configuration.addResourceFilePattern(TARA_PATTERN);
		configuration.convertPatterns();
	}

	@Override
	public void messageReceived(String builderId, String messageType, String messageText) {
		if(!TARAC.equalsIgnoreCase(builderId)) return;
		if (BuildConstants.ACTION_MESSAGE.equals(messageType)) {
			final List<String> messages = Arrays.asList(messageText.split(BuildConstants.MESSAGE_ACTION_START));
			final Module[] module = {null};
			List<PostCompileAction.FinishStatus> finishStatus = messages.stream()
					.skip(1)
					.map(m -> createCompileAction(module, m))
					.map(a -> a != null ? a.execute() : null)
					.toList();
			if (finishStatus.contains(RequiresReload)) IntinoUtil.configurationOf(module[0]).reload();
		} else if (BuildConstants.REFRESH_MESSAGE.equals(messageType)) {
			final String[] parameters = messageText.split(REFRESH_MESSAGE_SEPARATOR);
			refreshLanguage(parameters[0], parameters[1]);
			File directory = new File(parameters[parameters.length - 1]);
			refreshOut(directory);
			refreshDirectory(new File(directory.getParentFile(), "test-res"));
			refreshDirectory(new File(directory.getParentFile(), "res"));
			refreshDirectory(new File(directory.getParentFile(), "src"));
			refreshDirectory(new File(directory.getParentFile(), "test"));
		}
	}

	@Nullable
	private PostCompileAction createCompileAction(Module[] module, String m) {
		m = m.replace(BuildConstants.MESSAGE_ACTION_END, "");
		List<String> split = List.of(m.split(SEPARATOR));
		return PostCompileActionFactory.get(module(module, split.get(0)), split.get(1), split.subList(2, split.size()));
	}

	private Module module(Module[] module, String name) {
		name = name.contains("#") ? name.substring(0, name.indexOf("#")) : name;
		return module[0] = findModule(name);
	}


	private void refreshLanguage(String moduleName, String dslName) {
		Module module = ApplicationManager.getApplication().runReadAction((Computable<Module>) () -> ModuleManager.getInstance(project).findModuleByName(moduleName));
		final Configuration configuration = IntinoUtil.configurationOf(module);
		Configuration.Artifact.Dsl dsl = safe(() -> configuration.artifact().dsl(dslName));
		if (configuration != null && dsl != null) {
			LanguageManager.reloadLanguage(project, dsl.outputDsl().name(), dsl.outputDsl().version());
		}
	}

	private void refreshOut(File file) {
		VirtualFile outDir = VfsUtil.findFileByIoFile(file, true);
		if (outDir == null || !outDir.isValid()) return;
		outDir.refresh(true, true/*,
			() -> reformatGeneratedCode(VfsUtil.findFileByIoFile(new File(file, outDSLFromInput.toLowerCase() + File.separator + "natives"), true))*/);
	}

	private Module findModule(String module) {
		return Arrays.stream(ModuleManager.getInstance(project).getModules()).filter(m -> m.getName().equals(module)).findFirst().orElse(null);
	}

	private void refreshDirectory(File res) {
		VirtualFile resDir = VfsUtil.findFileByIoFile(res, true);
		if (resDir == null || !resDir.isValid()) return;
		resDir.refresh(true, true);
	}

	private void reformatGeneratedCode(VirtualFile outDir) {
		if (outDir == null || !outDir.isValid()) return;
		FileDocumentManager.getInstance().saveAllDocuments();
		StoreReloadManager.Companion.getInstance(project).blockReloadingProjectOnExternalChanges();
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
		StoreReloadManager.Companion.getInstance(project).unblockReloadingProjectOnExternalChanges();
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

