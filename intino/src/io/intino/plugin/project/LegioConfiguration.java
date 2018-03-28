package io.intino.plugin.project;

import com.intellij.execution.RunManager;
import com.intellij.execution.application.ApplicationConfiguration;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.Result;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.WebModuleType;
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
import io.intino.legio.graph.*;
import io.intino.legio.graph.Repository.Release;
import io.intino.legio.graph.level.LevelArtifact.Model;
import io.intino.plugin.dependencyresolution.*;
import io.intino.plugin.file.legio.LegioFileType;
import io.intino.plugin.project.builders.InterfaceBuilderManager;
import io.intino.plugin.project.builders.ModelBuilderManager;
import io.intino.plugin.project.run.IntinoRunConfiguration;
import io.intino.tara.StashBuilder;
import io.intino.tara.compiler.shared.Configuration;
import io.intino.tara.io.Stash;
import io.intino.tara.io.StashDeserializer;
import io.intino.tara.lang.model.Node;
import io.intino.tara.lang.model.Parameter;
import io.intino.tara.plugin.lang.LanguageManager;
import io.intino.tara.plugin.lang.psi.TaraModel;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.stream.Collectors;

import static io.intino.plugin.project.LibraryConflictResolver.libraryOf;
import static io.intino.plugin.project.LibraryConflictResolver.mustAdd;
import static io.intino.tara.compiler.shared.TaraBuildConstants.WORKING_PACKAGE;
import static java.util.stream.Collectors.toMap;

public class LegioConfiguration implements Configuration {
	private static final Logger LOG = Logger.getInstance(LegioConfiguration.class.getName());
	private final Module module;
	private VirtualFile legioFile;
	private LegioGraph legio;

	public LegioConfiguration(Module module) {
		this.module = module;
	}

	@Override
	public Configuration init() {
		this.legioFile = new LegioFileCreator(module).getOrCreate();
		initConfiguration();
		return this;
	}

	public boolean inited() {
		return legio != null;
	}

	private void initConfiguration() {
		File stashFile = stashFile();
		this.legio = (!stashFile.exists()) ? newGraphFromLegio() : GraphLoader.loadGraph(StashDeserializer.stashFrom(stashFile), stashFile());
		if (legio == null && stashFile.exists()) stashFile.delete();
		reloadInterfaceBuilder();
		resolveLanguages();
		reloadArtifactoriesMetaData();
		if (WebModuleType.isWebModule(module) && this.legio != null)
			new GulpExecutor(this.module, legio.artifact()).startGulpDev();
		if (legio != null && legio.artifact() != null) legio.artifact().save$();
	}

	@Override
	public boolean isSuitable() {
		return new File(new File(module.getModuleFilePath()).getParentFile(), LegioFileType.LEGIO_FILE).exists();
	}

	public void purgeAndReload() {
		final Application application = ApplicationManager.getApplication();
		if (application.isWriteAccessAllowed())
			application.runWriteAction(() -> FileDocumentManager.getInstance().saveAllDocuments());
		withTask(new Task.Backgroundable(module.getProject(), module.getName() + ": Purging and loading Configuration", false, PerformInBackgroundOption.ALWAYS_BACKGROUND) {
					 @Override
					 public void run(@NotNull ProgressIndicator indicator) {
						 if (legioFile == null) legioFile = new LegioFileCreator(module).getOrCreate();
						 legio = newGraphFromLegio();
						 new DependencyPurger(module, LegioConfiguration.this).execute();
						 reloadInterfaceBuilder();
						 reloadDependencies();
						 if (legio != null && legio.artifact() != null) legio.artifact().save$();
					 }
				 }
		);
	}

	@Override
	public void reload() {
		final Application application = ApplicationManager.getApplication();
		if (application.isWriteAccessAllowed())
			application.runWriteAction(() -> FileDocumentManager.getInstance().saveAllDocuments());
		if (module.isDisposed() || module.getProject().isDisposed()) return;
		withTask(new Task.Backgroundable(module.getProject(), module.getName() + ": Reloading Artifact", false, PerformInBackgroundOption.ALWAYS_BACKGROUND) {
					 @Override
					 public void run(@NotNull ProgressIndicator indicator) {
						 if (legioFile == null) legioFile = new LegioFileCreator(module).getOrCreate();
						 legio = newGraphFromLegio();
						 reloadInterfaceBuilder();
						 reloadDependencies();
						 reloadArtifactoriesMetaData();
						 reloadRunConfigurations();
						 if (legio != null && legio.artifact() != null) legio.artifact().save$();
					 }
				 }
		);
	}

	private void reloadArtifactoriesMetaData() {
		new ArtifactorySensor(repositoryTypes()).update();
	}

	private void reloadInterfaceBuilder() {
		final Artifact.Box boxing = safe(() -> legio.artifact().box());
		if (boxing != null) new InterfaceBuilderManager().reload(module.getProject(), boxing.sdk());
	}

	private void reloadRunConfigurations() {
		final List<RunConfiguration> runConfigurations = safeList(() -> legio.runConfigurationList());
		for (RunConfiguration runConfiguration : runConfigurations) {
			ApplicationConfiguration configuration = findRunConfiguration(runConfiguration.name$());
			if (configuration != null) configuration.setProgramParameters(parametersOf(runConfiguration));
		}
	}

	public static String parametersOf(io.intino.legio.graph.RunConfiguration runConfiguration) {
		StringBuilder builder = new StringBuilder();
		for (Argument argument : runConfiguration.argumentList())
			builder.append("\"").append(argument.name()).append("=").append(argument.value()).append("\" ");
		return builder.toString();
	}

	private ApplicationConfiguration findRunConfiguration(String name) {
		final List<com.intellij.execution.configurations.RunConfiguration> list = RunManager.getInstance(module.getProject()).
				getAllConfigurationsList().stream().filter(r -> r instanceof IntinoRunConfiguration).collect(Collectors.toList());
		return (ApplicationConfiguration) list.stream().filter(r -> r.getName().equals(name)).findFirst().orElse(null);
	}

	private LegioGraph newGraphFromLegio() {
		Stash legioStash = loadNewConfiguration();
		return legioStash == null ? null : GraphLoader.loadGraph(legioStash, stashFile());
	}

	private Stash loadNewConfiguration() {
		try {
			return new StashBuilder(Collections.singletonList(new File(legioFile.getPath())), new tara.dsl.Legio(), module.getName()).build();
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
			return null;
		}
	}

	@NotNull
	private File stashFile() {
		return new File(LanguageManager.getMiscDirectory(module.getProject()).getPath(), module.getName() + ".conf");
	}

	private void reloadDependencies() {
		if (legio == null || legio.artifact() == null) return;
		resolveJavaDependencies();
		if (WebModuleType.isWebModule(module) && legio.artifact().webImports() != null) resolveWebDependencies();
	}

	private void resolveJavaDependencies() {
		if (dependencies() == null) return;
		final JavaDependencyResolver resolver = new JavaDependencyResolver(module, repositoryTypes(), dependencies());
		final List<Library> newLibraries = resolver.resolve();
		final List<Library> languageLibraries = resolveLanguages();
		for (Library languageLibrary : languageLibraries) {
			if (mustAdd(languageLibrary, newLibraries)) {
				newLibraries.remove(libraryOf(newLibraries, languageLibrary.getName()));
				newLibraries.add(languageLibrary);
			}
		}
		LibraryManager.clean(module, newLibraries);
	}

	private List<Library> resolveLanguages() {
		List<Library> libraries = new ArrayList<>();
		Model model = model();
		if (model == null) return libraries;
		for (LanguageLibrary language : languages()) {
			final String effectiveVersion = language.effectiveVersion();
			String version = effectiveVersion == null || effectiveVersion.isEmpty() ? language.version() : effectiveVersion;
			libraries.addAll(new LanguageResolver(module, repositoryTypes(), model, version).resolve());
		}
		return libraries;
	}

	public List<Repository.Type> repositoryTypes() {
		List<Repository.Type> repos = new ArrayList<>();
		for (Repository r : safeList(() -> legio.repositoryList())) repos.addAll(r.typeList());
		return repos;
	}

	public List<String> taraCompilerClasspath() {
		Model modeling = model();
		if (modeling == null) return Collections.emptyList();
		return new ModelBuilderManager(this.module.getProject(), model()).resolveBuilder();
	}

	public Model model() {
		return safe(() -> legio.artifact().asLevel().model());
	}

	private void resolveWebDependencies() {
		new WebDependencyResolver(module, legio.artifact(), repositoryTypes()).resolve();
	}

	@Override
	public Level level() {
		if (legio == null || legio.artifact() == null) return null;
		final Artifact artifact = artifact();
		if (artifact == null) return null;
		final String level = artifact.core$().conceptList().stream().filter(c -> c.id().contains("#")).map(c -> c.id().split("#")[0]).findFirst().orElse(null);
		return level == null ? null : Level.valueOf(level);
	}

	@Override
	public String artifactId() {
		return safe(() -> legio.artifact().name$());
	}

	@Override
	public String groupId() {
		return safe(() -> legio.artifact().groupId());
	}

	public String version() {
		return safe(() -> legio.artifact().version());
	}

	public void version(String version) {
		reload();//TODO
	}

	@Override
	public String workingPackage() {
		return safe(() -> legio.artifact().code().targetPackage(), groupId() + "." + artifactId());
	}

	public String nativeLanguage() {
		return "java";
	}

	@Override
	public List<? extends LanguageLibrary> languages() {
		Model model = safe(() -> legio.artifact().asLevel().model());
		if (model == null) return Collections.emptyList();
		return Collections.singletonList(new LanguageLibrary() {
			@Override
			public String name() {
				return model.language();
			}

			@Override
			public String version() {
				return model.version();
			}

			@Override
			public String effectiveVersion() {
				return model.effectiveVersion();
			}

			@Override
			public void version(String version) {
				final Application application = ApplicationManager.getApplication();
				TaraModel psiFile = !application.isReadAccessAllowed() ?
						(TaraModel) application.runReadAction((Computable<PsiFile>) () -> PsiManager.getInstance(module.getProject()).findFile(legioFile)) :
						(TaraModel) PsiManager.getInstance(module.getProject()).findFile(legioFile);
				new WriteCommandAction(module.getProject(), psiFile) {
					@Override
					protected void run(@NotNull Result result) throws Throwable {
						model.version(version);
						final Node model = psiFile.components().get(0).components().stream().filter(f -> f.type().equals("LevelArtifact.Model")).findFirst().orElse(null);
						if (model == null) return;
						final Parameter versionParameter = model.parameters().stream().filter(p -> p.name().equals("version")).findFirst().orElse(null);
						versionParameter.substituteValues(Collections.singletonList(version));
					}
				}.execute();
				reload();
			}

			@Override
			public String generationPackage() {
				Attributes attributes = languageParameters();
				return attributes == null ? null : attributes.getValue(WORKING_PACKAGE.replace(".", "-"));
			}
		});
	}

	public Attributes languageParameters() {
		final Model model = safe(() -> legio.artifact().asLevel().model());
		if (model == null) return null;
		final File languageFile = LanguageManager.getLanguageFile(model.language(), model.effectiveVersion());
		if (!languageFile.exists()) return null;
		try {
			Manifest manifest = new JarFile(languageFile).getManifest();
			return manifest == null ? null : manifest.getAttributes("tara");
		} catch (IOException e) {
			return null;
		}

	}

	public Map<String, String> repositories() {
		Map<String, String> repositories = new HashMap<>();
		legio.repositoryList().forEach(r -> repositories.putAll(r.typeList().stream().collect(toMap(Repository.Type::url, Repository.Type::mavenID))));
		return repositories;
	}

	public Map<String, String> releaseRepositories() {
		Map<String, String> repositories = new HashMap<>();
		safeList(() -> legio.repositoryList()).forEach(r -> repositories.putAll(r.typeList(t -> t.i$(Release.class)).stream().collect(toMap(Repository.Type::url, Repository.Type::mavenID))));
		return repositories;
	}

	@Override
	public String snapshotRepository() {
		return safe(() -> legio.repositoryList().get(0).snapshot().url());
	}

	@Override
	public AbstractMap.SimpleEntry<String, String> distributionLanguageRepository() {
		return safe(() -> new AbstractMap.SimpleEntry<String, String>
				(legio.artifact().distribution().language().url(), legio.artifact().distribution().language().mavenID()));
	}

	@Override
	public AbstractMap.SimpleEntry<String, String> distributionReleaseRepository() {
		return safe(() -> new AbstractMap.SimpleEntry<String, String>
				(legio.artifact().distribution().release().url(), legio.artifact().distribution().release().mavenID()));
	}

	@Override
	public Map<String, String> languageRepositories() {
		if (legio == null) return Collections.emptyMap();
		Map<String, String> repositories = new HashMap<>();
		for (Repository r : legio.repositoryList()) {
			final List<Repository.Type> types = r.typeList(t -> t != null && t.i$(Repository.Language.class) && t.mavenID() != null && t.url() != null);
			if (types.isEmpty()) continue;
			repositories.putAll(types.stream().collect(toMap(Repository.Type::url, Repository.Type::mavenID)));
		}
		return repositories;
	}

	@Override
	public String outDSL() {
		return safe(() -> legio.artifact().name$());
	}

	@Override
	public String boxPackage() {
		return safe(() -> legio.artifact().box().targetPackage());
	}

	public List<Artifact.Deployment> deployments() {
		return safe(() -> legio.artifact().deploymentList());
	}

	public Artifact.Package pack() {
		return safe(() -> legio.artifact().package$());
	}

	public List<RunConfiguration> runConfigurations() {
		return safeList(() -> legio.runConfigurationList());
	}

	public Artifact.QualityAnalytics qualityAnalytics() {
		return legio.artifact().qualityAnalytics();
	}

	public List<DeployConfiguration> deployConfigurations() {
		final List<Artifact.Deployment> deploys = safeList(this::deployments);
		if (deploys == null || deploys.isEmpty()) return Collections.emptyList();
		return deploys.stream().flatMap(deploy -> deploy.destinations().stream()).map(this::createDeployConfiguration).collect(Collectors.toList());
	}

	@NotNull
	private DeployConfiguration createDeployConfiguration(final Destination deployment) {
		return new DeployConfiguration() {
			public String name() {
				return deployment.name$();
			}

			public boolean pro() {
				return deployment.i$(Artifact.Deployment.Pro.class);
			}

			public List<Parameter> parameters() {
				return deployment.runConfiguration().argumentList().stream().map(this::wrapParameter).collect(Collectors.toList());
			}

			@NotNull
			private Parameter wrapParameter(final Argument p) {
				return new Parameter() {
					public String name() {
						return p.name();
					}

					public String value() {
						return p.value();
					}
				};
			}
		};
	}

	@Override
	public String boxVersion() {
		return safe(() -> legio.artifact().box().sdk());
	}

	private String safe(StringWrapper wrapper) {
		return safe(wrapper, "");
	}

	private String safe(StringWrapper wrapper, String defaultValue) {
		try {
			return wrapper.value();
		} catch (Throwable e) {
			return defaultValue;
		}
	}

	private static <T> T safe(Wrapper<T> wrapper) {
		try {
			return wrapper.value();
		} catch (Throwable e) {
			return null;
		}
	}

	private <T> List<T> safeList(ListWrapper<T> wrapper) {
		try {
			return wrapper.value();
		} catch (Throwable e) {
			return Collections.emptyList();
		}
	}

	public List<Artifact.Imports.Dependency> dependencies() {
		return safeList(() -> legio.artifact().imports().dependencyList());
	}

	public List<Repository> legioRepositories() {
		return safeList(() -> legio.repositoryList());
	}

	public Artifact.License licence() {
		return legio.artifact().license();
	}

	public Artifact artifact() {
		return safe(() -> legio.artifact());
	}

	public PsiFile legioFile() {
		return PsiManager.getInstance(module.getProject()).findFile(legioFile);
	}

	public String runnerClass() {
		return safe(() -> pack().asRunnable().mainClass());
	}

	private interface StringWrapper {

		String value();
	}

	public interface Wrapper<T> {

		T value();
	}

	private interface ListWrapper<T> {
		List<T> value();
	}

	private void withTask(Task.Backgroundable runnable) {
		ProgressManager.getInstance().runProcessWithProgressAsynchronously(runnable, new BackgroundableProcessIndicator(runnable));
	}
}
