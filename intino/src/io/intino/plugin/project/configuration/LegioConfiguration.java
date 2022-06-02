package io.intino.plugin.project.configuration;

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
import io.intino.Configuration;
import io.intino.magritte.Resolver;
import io.intino.magritte.lang.model.Node;
import io.intino.magritte.lang.model.NodeContainer;
import io.intino.plugin.cesar.CesarServerInfoDownloader;
import io.intino.plugin.dependencyresolution.DependencyAuditor;
import io.intino.plugin.file.LegioFileType;
import io.intino.plugin.lang.psi.TaraModel;
import io.intino.plugin.lang.psi.TaraNode;
import io.intino.plugin.lang.psi.impl.IntinoUtil;
import io.intino.plugin.lang.psi.impl.TaraPsiUtil;
import io.intino.plugin.project.IntinoDirectory;
import io.intino.plugin.project.configuration.model.LegioArtifact;
import io.intino.plugin.project.configuration.model.LegioRepository;
import io.intino.plugin.project.configuration.model.LegioRunConfiguration;
import io.intino.plugin.project.configuration.model.LegioServer;
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
import static org.apache.maven.artifact.repository.ArtifactRepositoryPolicy.UPDATE_POLICY_ALWAYS;
import static org.apache.maven.artifact.repository.ArtifactRepositoryPolicy.UPDATE_POLICY_DAILY;


public class LegioConfiguration implements Configuration {
	private static final Logger LOG = Logger.getInstance(LegioConfiguration.class.getName());
	private final Module module;
	private final Resolver resolver;
	private final AtomicBoolean reloading = new AtomicBoolean(false);
	private TaraModel legioFile;
	private VirtualFile vFile;
	private boolean inited = false;
	private DependencyAuditor dependencyAuditor;

	public LegioConfiguration(Module module) {
		this.module = module;
		this.resolver = new Resolver(new Legio());
	}

	public Configuration init() {
		this.dependencyAuditor = new DependencyAuditor(module);
		try {
			withTask(new Task.Backgroundable(module.getProject(), module.getName() + ": Reloading Artifact", false, PerformInBackgroundOption.ALWAYS_BACKGROUND) {
				@Override
				public void run(@NotNull ProgressIndicator indicator) {
					vFile = new LegioFileCreator(module, Collections.emptyList()).get();
					legioFile = legioFile();
					legioFile.components().forEach(resolver::resolve);
					final ConfigurationReloader reloader = reloader(UPDATE_POLICY_DAILY);
					reloader.reloadInterfaceBuilder();
					reloader.reloadLanguage();
					reloader.reloadArtifactoriesMetaData();
					loadRemoteProcessesInfo();
					inited = true;
					save();
				}
			});
		} catch (Throwable ignored) {

		}
		return this;
	}

	public void loadRemoteProcessesInfo() {
		new CesarServerInfoDownloader().download(module);
	}

	public Module module() {
		return module;
	}

	public boolean isSuitable() {
		File parent = IntinoUtil.moduleRoot(module);
		return parent != null && new File(parent, LegioFileType.LEGIO_FILE).exists();
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
		synchronized (reloading) {
			if (reloading.get()) return;
			reloading.set(true);
			refresh();
			if (module.isDisposed() || module.getProject().isDisposed()) return;
			try {
				withTask(new Task.Backgroundable(module.getProject(), module.getName() + ": Reloading Artifact", false, PerformInBackgroundOption.ALWAYS_BACKGROUND) {
							 @Override
							 public void run(@NotNull ProgressIndicator indicator) {
								 try {
									 reloading.set(true);
									 if (legioFile == null) legioFile = legioFile();
									 final ConfigurationReloader reloader = reloader(UPDATE_POLICY_DAILY);
									 reloader.reloadInterfaceBuilder();
									 reloader.reloadDependencies();
									 reloader.reloadRunConfigurations();
									 save();
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

	public void reloadDependencies() {
		reloader(UPDATE_POLICY_ALWAYS).reloadDependencies();
	}

	@NotNull
	private ConfigurationReloader reloader(String updatePolicyDaily) {
		return new ConfigurationReloader(module, dependencyAuditor, LegioConfiguration.this, updatePolicyDaily);
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
												  final ConfigurationReloader reloader = reloader(UPDATE_POLICY_ALWAYS);
												  reloader.reloadInterfaceBuilder();
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
		return new LegioArtifact(this, dependencyAuditor, (TaraNode) TaraPsiUtil.componentOfType(legioFile, "Artifact"));
	}

	@Override
	@NotNull
	public List<Server> servers() {
		return componentsOfType(legioFile, "Server").stream().map(n -> new LegioServer(this, (TaraNode) n)).collect(Collectors.toList());
	}

	public List<RunConfiguration> runConfigurations() {
		List<Node> runConfiguration = componentsOfType(legioFile, "RunConfiguration");
		return runConfiguration.stream().map(r -> new LegioRunConfiguration(artifact(), r)).collect(Collectors.toList());
	}

	@Override
	@NotNull
	public List<Repository> repositories() {
		return componentsOfType(legioFile, "Repository").stream().
				map(NodeContainer::components).
				flatMap(Collection::stream).
				map(this::repository).
				collect(Collectors.toList());
	}

	private Repository repository(Node r) {
		if (((TaraNode) r).simpleType().equals("Release"))
			return new LegioRepository.LegioReleaseRepository(this, (TaraNode) r);
		if (((TaraNode) r).simpleType().equals("Snapshot"))
			return new LegioRepository.LegioSnapshotRepository(this, (TaraNode) r);
		return null;
	}

	public TaraModel legioFile() {
		Application application = ApplicationManager.getApplication();
		if (application.isReadAccessAllowed())
			return (TaraModel) PsiManager.getInstance(module.getProject()).findFile(vFile);
		return (TaraModel) application.runReadAction((Computable<PsiFile>) () -> PsiManager.getInstance(module.getProject()).findFile(vFile));
	}

	public boolean inited() {
		return inited;
	}

	public DependencyAuditor dependencyAuditor() {
		return dependencyAuditor;
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
		this.dependencyAuditor.save();
	}

	@NotNull
	private File confFile() {
		File file = IntinoDirectory.artifactsDirectory(module.getProject());
		file.mkdirs();
		return new File(file.getPath(), module.getName() + ".conf");
	}
}
