package io.intino.plugin.actions.box;

import com.intellij.notification.NotificationGroup;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.progress.PerformInBackgroundOption;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.progress.impl.BackgroundableProcessIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.CompilerModuleExtension;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.indexing.FileBasedIndex;
import com.intellij.util.indexing.ID;
import io.intino.Configuration;
import io.intino.plugin.IntinoIcons;
import io.intino.plugin.actions.IntinoAction;
import io.intino.plugin.file.konos.KonosFileType;
import io.intino.plugin.lang.psi.TaraModel;
import io.intino.plugin.lang.psi.impl.TaraUtil;
import io.intino.plugin.project.IntinoDirectory;
import io.intino.plugin.project.LegioConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static io.intino.konos.compiler.shared.KonosBuildConstants.*;
import static java.nio.charset.StandardCharsets.UTF_8;

public class BoxElementsGenerationAction extends IntinoAction {
	private static final Logger Logger = com.intellij.openapi.diagnostic.Logger.getInstance(BoxElementsGenerationAction.class);

	@Override
	public void update(AnActionEvent e) {
		super.update(e);
		final Project project = e.getProject();
		boolean visible = project != null && e.getData(LangDataKeys.MODULE) != null;
		e.getPresentation().setVisible(visible);
		e.getPresentation().setEnabled(visible);
		e.getPresentation().setIcon(IntinoIcons.GENARATION_16);
		e.getPresentation().setText("Generate Web Elements Code");
	}

	@Override
	public void actionPerformed(AnActionEvent e) {
		execute(e.getData(LangDataKeys.MODULE));
	}

	@Override
	public void execute(Module module) {
		final Configuration configuration = TaraUtil.configurationOf(module);
		if (!(configuration instanceof LegioConfiguration)) return;
		withTask(new Task.Backgroundable(module.getProject(), module.getName() + ": Reloading box elements", false, PerformInBackgroundOption.ALWAYS_BACKGROUND) {
			@Override
			public void run(@NotNull ProgressIndicator indicator) {
				doExecute(module, (LegioConfiguration) configuration);
			}
		});
	}

	private void doExecute(Module module, LegioConfiguration configuration) {
		try {
			ApplicationManager.getApplication().invokeAndWait(() -> FileDocumentManager.getInstance().saveAllDocuments());
			KonosRunner konosRunner = new KonosRunner(module, configuration, sources(module), UTF_8, collectPaths(module));
			konosRunner.runKonosCompiler();
			notify(module);
		} catch (IOException e) {
			Logger.error(e);
		}
	}

	private void withTask(Task.Backgroundable runnable) {
		ProgressManager.getInstance().runProcessWithProgressAsynchronously(runnable, new BackgroundableProcessIndicator(runnable));
	}

	private void notify(Module module) {
		final NotificationGroup balloon = NotificationGroup.findRegisteredGroup("Tara Language");
		if (balloon != null)
			balloon.createNotification(module.getName() + " Web elements " + "reloaded", MessageType.INFO).setImportant(false).notify(module.getProject());
	}

	private Map<String, String> collectPaths(Module module) {
		File projectDirectory = new File(Objects.requireNonNull(module.getProject().getBasePath()));
		Map<String, String> map = new LinkedHashMap<>();
		map.put(PROJECT_PATH, projectDirectory.getAbsolutePath());
		map.put(MODULE_PATH, new File(module.getModuleFilePath()).getParent());
		map.put(RES_PATH, TaraUtil.getResourcesRoot(module, false).getPath());
		List<VirtualFile> sourceRoots = TaraUtil.getSourceRoots(module);
		sourceRoots.stream().filter(f -> new File(f.getPath()).getName().equals("src")).findFirst().ifPresent(src -> map.put(SRC_PATH, src.getPath()));
		sourceRoots.stream().filter(f -> new File(f.getPath()).getName().equals("gen")).findFirst().ifPresent(src -> map.put(OUTPUTPATH, src.getPath()));
		map.put(FINAL_OUTPUTPATH, CompilerModuleExtension.getInstance(module).getCompilerOutputUrl().replace("file://", ""));
		File intinoDirectory = IntinoDirectory.of(module.getProject());
		if (intinoDirectory.exists()) map.put(INTINO_PROJECT_PATH, intinoDirectory.getAbsolutePath());
		return map;
	}


	public List<File> sources(Module module) {
		if (module == null) {
			return Collections.emptyList();
		} else {
			Application application = ApplicationManager.getApplication();
			return application.isReadAccessAllowed() ? konosFiles(module) : application.runReadAction((Computable<List<File>>) () -> konosFiles(module));
		}
	}

	private List<File> konosFiles(Module module) {
		List<File> konosFiles = new ArrayList<>();
		Collection<VirtualFile> files = FileBasedIndex.getInstance().getContainingFiles(ID.create("filetypes"), KonosFileType.instance(), GlobalSearchScope.moduleScope(module));
		files.stream().filter((o) -> o != null && !o.getCanonicalFile().getName().contains("Misc")).forEach((file) -> {
			TaraModel konosFile = (TaraModel) PsiManager.getInstance(module.getProject()).findFile(file);
			if (konosFile != null) konosFiles.add(new File(konosFile.getVirtualFile().getPath()));
		});
		return konosFiles;
	}
}
