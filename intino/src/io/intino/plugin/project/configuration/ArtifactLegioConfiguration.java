package io.intino.plugin.project.configuration;

import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.module.WebModuleType;
import com.intellij.openapi.progress.PerformInBackgroundOption;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.progress.impl.BackgroundableProcessIndicator;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.util.FileContentUtil;
import io.intino.Configuration;
import io.intino.plugin.IntinoException;
import io.intino.plugin.build.FactoryPhase;
import io.intino.plugin.build.maven.PomCreator;
import io.intino.plugin.cesar.CesarServerInfoDownloader;
import io.intino.plugin.file.LegioFileType;
import io.intino.plugin.lang.psi.TaraModel;
import io.intino.plugin.lang.psi.TaraMogram;
import io.intino.plugin.lang.psi.impl.IntinoUtil;
import io.intino.plugin.lang.psi.impl.TaraMogramImpl;
import io.intino.plugin.lang.psi.impl.TaraPsiUtil;
import io.intino.plugin.project.IntinoDirectory;
import io.intino.plugin.project.configuration.model.LegioArtifact;
import io.intino.plugin.project.configuration.model.LegioRepository;
import io.intino.plugin.project.configuration.model.LegioRunConfiguration;
import io.intino.plugin.project.configuration.model.LegioServer;
import io.intino.tara.Resolver;
import io.intino.tara.language.model.Mogram;
import io.intino.tara.language.model.MogramContainer;
import org.jetbrains.annotations.NotNull;
import tara.dsl.Legio;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static io.intino.plugin.lang.psi.impl.TaraPsiUtil.componentsOfType;
import static io.intino.plugin.project.Safe.safe;
import static io.intino.plugin.project.Safe.safeList;
import static java.util.Collections.singleton;
import static org.apache.maven.artifact.repository.ArtifactRepositoryPolicy.UPDATE_POLICY_ALWAYS;
import static org.apache.maven.artifact.repository.ArtifactRepositoryPolicy.UPDATE_POLICY_DAILY;


public class ArtifactLegioConfiguration implements Configuration {
	private static final Logger LOG = Logger.getInstance(ArtifactLegioConfiguration.class.getName());
	private final Module module;
	private final Resolver resolver;
	private final AtomicBoolean reloading = new AtomicBoolean(false);
	private TaraModel legioFile;
	private VirtualFile vFile;
	private boolean ignited = false;

	public ArtifactLegioConfiguration(Module module) {
		this.module = module;
		this.resolver = new Resolver(new Legio());
	}

	public Configuration init() {
		try {
			withTask(new Task.Backgroundable(module.getProject(), module.getName() + ": Reloading Artifact", false, PerformInBackgroundOption.ALWAYS_BACKGROUND) {
				@Override
				public void run(@NotNull ProgressIndicator indicator) {
					vFile = new LegioFileCreator(module, Collections.emptyList()).getArtifact();
					legioFile = legioFile();
					ApplicationManager.getApplication().runReadAction(() -> legioFile.components().forEach(resolver::resolve));
					final ConfigurationReloader reloader = reloader(indicator, UPDATE_POLICY_DAILY);
					indicator.setText("Resolving box builder...");
					reloader.reloadInterfaceBuilder();
					indicator.setText("Reloading language...");
					reloader.reloadLanguage();
					reloader.reloadArtifactoriesMetaData();
					loadRemoteProcessesInfo();
					if (Boolean.TRUE.equals(safe(() -> artifact().packageConfiguration().createMavenPom())))
						createPom();
					ignited = true;
					save();
				}
			});
		} catch (Throwable ignored) {

		}
		return this;
	}

	public void loadRemoteProcessesInfo() {
		new CesarServerInfoDownloader().download(module.getProject(), this.servers());
	}

	public Module module() {
		return module;
	}

	public boolean isSuitable() {
		File parent = IntinoUtil.moduleRoot(module);
		return parent != null && new File(parent, LegioFileType.ARTIFACT_LEGIO).exists();
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
			if (module.isDisposed() || module.getProject().isDisposed()) return;
			try {
				withTask(new Task.Backgroundable(module.getProject(), module.getName() + ": Reloading Artifact...", false, PerformInBackgroundOption.ALWAYS_BACKGROUND) {
							 @Override
							 public void run(@NotNull ProgressIndicator indicator) {
								 try {
									 reloading.set(true);
									 if (legioFile == null) legioFile = legioFile();
									 final ConfigurationReloader reloader = reloader(indicator, UPDATE_POLICY_DAILY);
									 indicator.setText("Reloading box builder...");
									 reloader.reloadInterfaceBuilder();
									 indicator.setText("Resolving imports...");
									 reloader.reloadDependencies();
									 reloader.reloadRunConfigurations();
									 if (safe(() -> artifact().packageConfiguration().createMavenPom())) createPom();
									 save();
									 refresh();
									 restartCodeAnalyzer();
									 FileContentUtil.reparseFiles(module.getProject(), singleton(legioFile.getVirtualFile()), true);
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


	private void createPom() {
		if (ModuleType.get(module) instanceof WebModuleType) return;
		try {
			new PomCreator(module).frameworkPom(FactoryPhase.PACKAGE);
		} catch (IntinoException e) {
			LOG.error(e);
		}
	}

	public void reloadDependencies() {
		reloader(UPDATE_POLICY_ALWAYS).reloadDependencies();
	}

	@NotNull
	private ConfigurationReloader reloader(ProgressIndicator indicator, String policy) {
		return new ConfigurationReloader(module, ArtifactLegioConfiguration.this, policy, indicator);
	}

	@NotNull
	private ConfigurationReloader reloader(String updatePolicyDaily) {
		return new ConfigurationReloader(module, ArtifactLegioConfiguration.this, updatePolicyDaily);
	}

	public void purgeAndReload() {
		if (reloading.get()) return;
		reloading.set(true);
		final Application application = ApplicationManager.getApplication();
		if (application.isWriteAccessAllowed())
			application.runWriteAction(() -> FileDocumentManager.getInstance().saveAllDocuments());
		ProgressManager.getInstance().run(new Task.Backgroundable(module.getProject(), module.getName() + ": Purging and loading Configuration", false, PerformInBackgroundOption.ALWAYS_BACKGROUND) {
											  @Override
											  public void run(@NotNull ProgressIndicator indicator) {
												  reloading.set(true);
												  if (legioFile == null) legioFile = legioFile();
												  final ConfigurationReloader reloader = reloader(indicator, UPDATE_POLICY_ALWAYS);
												  indicator.setText("Reloading box builder...");
												  reloader.reloadInterfaceBuilder();
												  indicator.setText("Resolving imports...");
												  reloader.reloadDependencies();
												  save();
												  reloading.set(false);
											  }
										  }
		);
		reloading.set(false);
	}


	@Override
	@NotNull
	public LegioArtifact artifact() {
		return new LegioArtifact(this, (TaraMogram) TaraPsiUtil.componentOfType(legioFile, "Artifact"));
	}

	@Override
	@NotNull
	public List<Server> servers() {
		List<Server> servers = componentsOfType(legioFile, "Server").stream().map(n -> new LegioServer((TaraMogram) n)).collect(Collectors.toList());
		servers.addAll(safeList(() -> ConfigurationManager.projectConfigurationOf(module).project().servers()));
		return servers;
	}

	public List<RunConfiguration> runConfigurations() {
		List<Mogram> runConfiguration = componentsOfType(legioFile, "RunConfiguration");
		return runConfiguration.stream().map(r -> new LegioRunConfiguration(artifact(), r)).collect(Collectors.toList());
	}

	@Override
	@NotNull
	public List<Repository> repositories() {
		List<Repository> repositories = componentsOfType(legioFile, "Repository").stream().
				map(MogramContainer::components).
				flatMap(Collection::stream).
				map(this::repository).
				collect(Collectors.toList());
		repositories.addAll(safeList(() -> ConfigurationManager.projectConfigurationOf(module).project().repositories()));

		return repositories;
	}

	private Repository repository(Mogram r) {
		if (((TaraMogramImpl) r).simpleType().equals("Release"))
			return new LegioRepository.LegioReleaseRepository(this, (TaraMogram) r);
		if (((TaraMogramImpl) r).simpleType().equals("Snapshot"))
			return new LegioRepository.LegioSnapshotRepository(this, (TaraMogram) r);
		return null;
	}


	private void restartCodeAnalyzer() {
		Application application = ApplicationManager.getApplication();
		DaemonCodeAnalyzer codeAnalyzer = DaemonCodeAnalyzer.getInstance(module.getProject());
		if (application.isReadAccessAllowed()) codeAnalyzer.restart(legioFile);
		else application.runReadAction(() -> codeAnalyzer.restart(legioFile));
	}

	public VirtualFile legiovFile() {
		return vFile;
	}

	public TaraModel legioFile() {
		Application application = ApplicationManager.getApplication();
		if (application.isReadAccessAllowed())
			return (TaraModel) PsiManager.getInstance(module.getProject()).findFile(vFile);
		return (TaraModel) application.runReadAction((Computable<PsiFile>) () -> PsiManager.getInstance(module.getProject()).findFile(vFile));
	}

	public boolean inited() {
		return ignited;
	}

	private void withTask(Task.Backgroundable runnable) {
		ProgressManager.getInstance().runProcessWithProgressAsynchronously(runnable, new BackgroundableProcessIndicator(runnable));
	}

	public void save() {
		LegioArtifact artifact = artifact();
		if (module == null || ModuleType.get(module) instanceof WebModuleType) return;
		if (artifact.model() != null || artifact.box() != null) try {
			Files.write(confFile().toPath(), artifact.serialize());
		} catch (IOException e) {
			LOG.error(e);
		}
	}

	@NotNull
	private File confFile() {
		File file = IntinoDirectory.artifactsDirectory(module.getProject());
		file.mkdirs();
		return new File(file.getPath(), module.getName() + ".conf");
	}
}
