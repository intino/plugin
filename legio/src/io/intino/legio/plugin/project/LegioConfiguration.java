package io.intino.legio.plugin.project;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationGroup;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.Result;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.progress.PerformInBackgroundOption;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.progress.impl.BackgroundableProcessIndicator;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import io.intino.legio.LegioApplication;
import io.intino.legio.Project;
import io.intino.legio.Project.Repositories.Language;
import io.intino.legio.Project.Repositories.Release;
import io.intino.legio.Project.Repositories.Repository;
import io.intino.legio.Project.Repositories.Snapshot;
import io.intino.legio.plugin.dependencyresolution.DependencyResolver;
import io.intino.legio.plugin.dependencyresolution.LanguageResolver;
import io.intino.legio.plugin.dependencyresolution.LegioUtil;
import io.intino.legio.plugin.dependencyresolution.LibraryManager;
import org.jetbrains.annotations.NotNull;
import tara.StashBuilder;
import tara.compiler.shared.Configuration;
import tara.dsl.Legio;
import tara.intellij.lang.LanguageManager;
import tara.intellij.lang.psi.TaraModel;
import tara.io.Stash;
import tara.io.StashSerializer;
import tara.lang.model.Node;
import tara.lang.model.Parameter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class LegioConfiguration implements Configuration {

	private static final String CONFIGURATION_LEGIO = "configuration.legio";
	private final Module module;
	private VirtualFile legioFile;
	private LegioApplication legio;

	public LegioConfiguration(Module module) {
		this.module = module;
	}

	@Override
	public Configuration init() {
		legioFile = new LegioFileCreator(module).create();
		reloadGraph();
		reloadDependencies();
		return this;
	}

	@Override
	public boolean isSuitable() {
		return new File(new File(module.getModuleFilePath()).getParentFile(), CONFIGURATION_LEGIO).exists();
	}

	@Override
	public void reload() {
		final NotificationGroup balloon = NotificationGroup.toolWindowGroup("Tara Language", "Balloon");
		balloon.createNotification("Configuration of " + module.getName() + " has changed", "<a href=\"#\">Reload Configuration</a>",
				NotificationType.INFORMATION, (n, e) -> reloadInfo(n)).setImportant(true).notify(module.getProject());
	}

	public void reloadInfo(Notification notification) {
		hide(notification);
		FileDocumentManager.getInstance().saveAllDocuments();
		withTask(new Task.Backgroundable(module.getProject(), "Reloading Configuration", false, PerformInBackgroundOption.ALWAYS_BACKGROUND) {
					 @Override
					 public void run(@NotNull ProgressIndicator indicator) {
						 reloadGraph();
						 ApplicationManager.getApplication().invokeLater(() ->
								 ApplicationManager.getApplication().runWriteAction(() -> reloadDependencies()));
					 }
				 }
		);
	}

	private void hide(Notification notification) {
		if (notification == null) return;
		notification.expire();
		notification.hideBalloon();
	}

	private void withTask(Task.Backgroundable runnable) {
		ProgressManager.getInstance().runProcessWithProgressAsynchronously(runnable, new BackgroundableProcessIndicator(runnable));
	}

	private void reloadGraph() {
		Stash legioStash = loadNewConfiguration();
		if (legioStash == null) return;
		saveStash(legioStash);
		this.legio = GraphLoader.loadGraph(legioStash);
	}

	private Stash loadNewConfiguration() {
		return new StashBuilder(new File(legioFile.getPath()), new Legio(), module.getName()).build();
	}

	private void saveStash(Stash legioStash) {
		try {
			File file = new File(LanguageManager.getMiscDirectory(module.getProject()).getPath(), module.getName() + ".conf");
			file.getParentFile().mkdirs();
			file.createNewFile();
			Files.write(file.toPath(), StashSerializer.serialize(legioStash));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void reloadDependencies() {
		if (legio == null || legio.project() == null) return;
		final List<Library> newLibraries = new DependencyResolver(module, legio.project().repositories(), legio.project().dependencies()).resolve();
		if (legio.project().factory() != null)
			newLibraries.addAll(new LanguageResolver(module, legio.project().repositories().repositoryList(), legio.project().factory(), dslEffectiveVersion()).resolve());
		LibraryManager.removeOldLibraries(module, newLibraries);
	}

	@Override
	public Level level() {
		if (legio == null || legio.project() == null) return null;
		final Project.Factory factory = legio.project().factory();
		if (factory == null) return null;
		final String level = factory.node().conceptList().stream().filter(c -> c.id().contains("#")).map(c -> c.id().split("#")[0]).findFirst().orElse("Platform");
		return Level.valueOf(level);
	}

	@Override
	public String artifactId() {
		return safe(() -> legio.project().name());
	}

	@Override
	public String groupId() {
		return safe(() -> legio.project().groupId());
	}

	@Override
	public String workingPackage() {
		final String workingPackage = safe(() -> legio.project().factory().generationPackage(), dsl());
		return workingPackage.isEmpty() ? outDSL() : workingPackage;
	}

	@Override
	public List<String> repositories() {
		return legio.project().repositories().repositoryList().stream().
				map(Repository::url).collect(Collectors.toList());
	}

	public List<String> releaseRepositories() {
		return legio.project().repositories().releaseList().stream().
				map(Release::url).collect(Collectors.toList());
	}

	@Override
	public List<String> snapshotRepositories() {
		return legio.project().repositories().snapshotList().stream().
				map(Snapshot::url).collect(Collectors.toList());
	}

	@Override
	public String languageRepository() {
		return legio.project().repositories().languageList().stream().
				map(Language::url).findFirst().orElse(null);
	}

	public String languageRepositoryId() {
		return legio.project().repositories().languageList().stream().
				map(Language::mavenId).findFirst().orElse(null);
	}

	@Override
	public String dsl() {
		return safe(() -> legio.project().factory().modeling().language());
	}

	@Override
	public String outDSL() {
		return safe(() -> legio.project().name());
	}

	@Override
	public String dslVersion() {
		return safe(() -> legio.project().factory().modeling().version());
	}


	public String dslEffectiveVersion() {
		return LegioUtil.effectiveVersionOf(dsl(), dslVersion(), this);
	}

	@Override
	public void dslVersion(String version) {
		final Application application = ApplicationManager.getApplication();
		TaraModel psiFile = !application.isReadAccessAllowed() ?
				(TaraModel) application.runReadAction((Computable<PsiFile>) () -> PsiManager.getInstance(module.getProject()).findFile(legioFile)) :
				(TaraModel) PsiManager.getInstance(module.getProject()).findFile(legioFile);
		new WriteCommandAction(module.getProject(), psiFile) {
			@Override
			protected void run(@NotNull Result result) throws Throwable {
				legio.project().factory().modeling().version(version);
				final Node factory = psiFile.components().get(0).components().stream().filter(f -> f.type().equals("Project.Factory")).findFirst().orElse(null);
				if (factory == null) return;
				final Node modeling = factory.components().stream().filter(f -> f.type().equals(f.type())).findFirst().orElse(null);
				if (modeling == null) return;
				final Parameter versionParameter = modeling.parameters().stream().filter(p -> p.name().equals("version")).findFirst().orElse(null);
				versionParameter.substituteValues(Collections.singletonList(version));
			}
		}.execute();
		reload();
	}

	public Project.Build build() {
		return legio.project().build();
	}

	@Override
	public String modelVersion() {
		return safe(() -> legio.project().version());
	}

	@Override
	public void modelVersion(String version) {
		//TODO
		reload();
	}

	@Override
	public int refactorId() {
		return 0;
	}

	@Override
	public void refactorId(int i) {
		reload();
	}

	@Override
	public boolean isPersistent() {
		return safe(() -> legio.project().factory().persistent());
	}

	private String safe(StringWrapper wrapper) {
		return safe(wrapper, "");
	}

	private boolean safe(BooleanWrapper wrapper) {
		try {
			return wrapper.value();
		} catch (NullPointerException e) {
			return false;
		}
	}

	private String safe(StringWrapper wrapper, String defaultValue) {
		try {
			return wrapper.value();
		} catch (NullPointerException e) {
			return defaultValue;
		}
	}

	public List<Project.Dependencies.Compile> dependencies() {
		return legio.project().dependencies().compileList();
	}

	public List<Repository> legioRepositories() {
		return legio.project().repositories().repositoryList();
	}

	private interface StringWrapper {
		String value();
	}

	private interface BooleanWrapper {
		boolean value();
	}
}