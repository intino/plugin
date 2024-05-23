package io.intino.plugin.project.configuration;

import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.progress.impl.BackgroundableProcessIndicator;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.util.FileContentUtil;
import io.intino.ProjectConfiguration;
import io.intino.plugin.cesar.CesarServerInfoDownloader;
import io.intino.plugin.file.LegioFileType;
import io.intino.plugin.lang.psi.TaraModel;
import io.intino.plugin.lang.psi.TaraMogram;
import io.intino.plugin.lang.psi.impl.IntinoUtil;
import io.intino.plugin.lang.psi.impl.TaraPsiUtil;
import io.intino.plugin.project.configuration.model.LegioProject;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.util.Collections.singleton;


public class ProjectLegioConfiguration implements ProjectConfiguration {
	private static final Logger LOG = Logger.getInstance(ProjectLegioConfiguration.class.getName());
	private final AtomicBoolean reloading = new AtomicBoolean(false);
	private final com.intellij.openapi.project.Project ijProject;
	private VirtualFile vFile;
	private TaraModel legioFile;
	private boolean ignited = false;

	public ProjectLegioConfiguration(com.intellij.openapi.project.Project project) {
		this.ijProject = project;
	}

	public ProjectConfiguration init() {
		try {
			withTask(new Task.Backgroundable(ijProject, ijProject.getName() + ": Reloading", false) {
				@Override
				public void run(@NotNull ProgressIndicator indicator) {
					vFile = new LegioFileCreator(null, Collections.emptyList()).getProject(myProject);
					legioFile = legioFile();
					loadRemoteProcessesInfo();
				}
			});
		} catch (Throwable ignored) {
		}
		return this;
	}

	public com.intellij.openapi.project.Project ijProject() {
		return ijProject;
	}

	public boolean isSuitable() {
		File dir = IntinoUtil.projectRoot(ijProject);
		return new File(dir, LegioFileType.ARTIFACT_LEGIO).exists();
	}

	public void loadRemoteProcessesInfo() {
		new CesarServerInfoDownloader().download(ijProject, project().servers());
	}

	public boolean isReloading() {
		return reloading.get();
	}

	public void refresh() {
		final Application application = ApplicationManager.getApplication();
		if (application.isWriteAccessAllowed())
			application.runWriteAction(() -> {
				if (legioFile == null) return;
				FileDocumentManager instance = FileDocumentManager.getInstance();
				Document document = instance.getDocument(legioFile.getVirtualFile());
				if (document != null) instance.saveDocument(document);
				else instance.saveAllDocuments();
			});
	}

	public void reload() {
		if (reloading.get()) return;
		synchronized (reloading) {
			reloading.set(true);
			refresh();
			if (ijProject.isDisposed()) return;
			try {
				withTask(new Task.Backgroundable(ijProject, ijProject + ": Reloading project...", false) {
							 @Override
							 public void run(@NotNull ProgressIndicator indicator) {
								 try {
									 reloading.set(true);
									 if (legioFile == null) legioFile = legioFile();
									 refresh();
									 restartCodeAnalyzer();
									 FileContentUtil.reparseFiles(ijProject, singleton(legioFile.getVirtualFile()), true);
									 reloading.set(false);
								 } catch (Throwable ignored) {
									 reloading.set(false);
								 }
							 }
						 }
				);
			} catch (Throwable e) {
				LOG.error(e);
			}
			reloading.set(false);
		}
	}


	@Override
	@NotNull
	public LegioProject project() {
		return new LegioProject(this, (TaraMogram) TaraPsiUtil.componentOfType(legioFile, "Project"));
	}

	private void restartCodeAnalyzer() {
		Application application = ApplicationManager.getApplication();
		DaemonCodeAnalyzer codeAnalyzer = DaemonCodeAnalyzer.getInstance(ijProject);
		if (application.isReadAccessAllowed()) codeAnalyzer.restart(legioFile);
		else application.runReadAction(() -> codeAnalyzer.restart(legioFile));
	}

	public TaraModel legioFile() {
		if (vFile == null) return null;
		Application application = ApplicationManager.getApplication();
		if (application.isReadAccessAllowed())
			return (TaraModel) PsiManager.getInstance(ijProject).findFile(vFile);
		return (TaraModel) application.runReadAction((Computable<PsiFile>) () -> PsiManager.getInstance(ijProject).findFile(vFile));
	}

	public boolean inited() {
		return ignited;
	}

	private void withTask(Task.Backgroundable runnable) {
		ProgressManager.getInstance().runProcessWithProgressAsynchronously(runnable, new BackgroundableProcessIndicator(runnable));
	}

}
