package io.intino.plugin.project;

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
import io.intino.legio.Artifact;
import io.intino.legio.Artifact.Modeling;
import io.intino.legio.Legio;
import io.intino.legio.Repository;
import io.intino.legio.Repository.Release;
import io.intino.plugin.dependencyresolution.*;
import io.intino.plugin.file.legio.LegioFileType;
import io.intino.plugin.project.builders.InterfaceBuilderManager;
import io.intino.tara.StashBuilder;
import io.intino.tara.compiler.shared.Configuration;
import io.intino.tara.compiler.shared.TaraBuildConstants;
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

import static java.util.stream.Collectors.toMap;

public class LegioConfiguration implements Configuration {
	private static final Logger LOG = Logger.getInstance(GulpExecutor.class.getName());

	private final Module module;
	private VirtualFile legioFile;
	private Legio legio;

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
		if (WebModuleType.isWebModule(module) && this.legio != null)
			new GulpExecutor(this.module, legio.artifact()).startGulpDev();
		if (legio != null && legio.artifact() != null) legio.artifact().save();
	}

	@Override
	public boolean isSuitable() {
		return new File(new File(module.getModuleFilePath()).getParentFile(), LegioFileType.LEGIO_FILE).exists();
	}

	public void purgeAndReload() {
		final Application application = ApplicationManager.getApplication();
		if (application.isWriteAccessAllowed())
			application.runWriteAction(() -> FileDocumentManager.getInstance().saveAllDocuments());
		withTask(new Task.Backgroundable(module.getProject(), "Purge Configuration", false, PerformInBackgroundOption.ALWAYS_BACKGROUND) {
					 @Override
					 public void run(@NotNull ProgressIndicator indicator) {
						 if (legioFile == null) legioFile = new LegioFileCreator(module).getOrCreate();
						 legio = newGraphFromLegio();
						 new DependencyPurger(module).execute();
						 reloadInterfaceBuilder();
						 reloadDependencies();
						 if (legio != null && legio.artifact() != null) legio.artifact().save();
					 }
				 }
		);
	}

	@Override
	public void reload() {
		final Application application = ApplicationManager.getApplication();
		if (application.isWriteAccessAllowed())
			application.runWriteAction(() -> FileDocumentManager.getInstance().saveAllDocuments());
		withTask(new Task.Backgroundable(module.getProject(), "Reloading Configuration", false, PerformInBackgroundOption.ALWAYS_BACKGROUND) {
					 @Override
					 public void run(@NotNull ProgressIndicator indicator) {
						 if (legioFile == null) legioFile = new LegioFileCreator(module).getOrCreate();
						 legio = newGraphFromLegio();
						 reloadInterfaceBuilder();
						 reloadDependencies();
						 if (legio != null && legio.artifact() != null) legio.artifact().save();
					 }
				 }
		);
	}

	private void reloadInterfaceBuilder() {
		final Artifact.Boxing boxing = safe(() -> legio.artifact().boxing());
		if (boxing != null) new InterfaceBuilderManager().reload(module.getProject(), boxing.version());
	}

	private Legio newGraphFromLegio() {
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
		newLibraries.addAll(resolveLanguages());
		LibraryManager.clean(module, newLibraries);
	}

	private List<Library> resolveLanguages() {
		List<Library> libraries = new ArrayList<>();
		Modeling modeling = safe(() -> legio.artifact().modeling());
		if (modeling == null) return libraries;
		for (LanguageLibrary language : languages()) {
			final String effectiveVersion = language.effectiveVersion();
			String version = effectiveVersion == null || effectiveVersion.isEmpty() ? language.version() : effectiveVersion;
			libraries.addAll(new LanguageResolver(module, repositoryTypes(),
					modeling.languageList(d -> d.name$().equals(language.name())).get(0), version).resolve());
		}
		return libraries;
	}

	public List<Repository.Type> repositoryTypes() {
		List<Repository.Type> repos = new ArrayList<>();
		for (Repository r : safeList(() -> legio.repositoryList())) repos.addAll(r.typeList());
		return repos;
	}

	public List<String> taraCompilerClasspath() {
		Artifact.Generation modeling = generation();
		if (modeling == null) return Collections.emptyList();
		return new TaraBuilderResolver(this.module.getProject(), generation()).resolveBuilder();
	}

	private void resolveWebDependencies() {
		new WebDependencyResolver(module, legio.artifact(), repositoryTypes(), legio.artifact().webImports()).resolve();
	}

	@Override
	public Level level() {
		if (legio == null || legio.artifact() == null) return null;
		final Artifact.Generation generation = legio.artifact().generation();
		if (generation == null) return null;
		final String level = generation.node().conceptList().stream().filter(c -> c.id().contains("#")).map(c -> c.id().split("#")[0]).findFirst().orElse(null);
		return level == null ? null : Level.valueOf(level);
	}

	@Override
	public String artifactId() {
		return safe(() -> legio.artifact().name());
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
		return safe(() -> legio.artifact().generation().inPackage(), outDSL());
	}

	public String nativeLanguage() {
		return "java";
	}

	@Override
	public List<? extends LanguageLibrary> languages() {
		final List<Modeling.Language> list = safe(() -> legio.artifact().modeling().languageList());
		if (list == null) return Collections.emptyList();
		List<LanguageLibrary> languages = new ArrayList<>();
		for (Modeling.Language language : list) {
			languages.add(new LanguageLibrary() {
				@Override
				public String name() {
					return language.name$();
				}

				@Override
				public String version() {
					return language.version();
				}

				@Override
				public String effectiveVersion() {
					return language.effectiveVersion();
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
							language.version(version);
							final Node modeling = psiFile.components().get(0).components().stream().filter(f -> f.type().equals("Project.Modeling")).findFirst().orElse(null);
							if (modeling == null) return;
							final Node language = modeling.components().stream().filter(f -> f.type().equals("Project.Modeling.Language")).findFirst().orElse(null);
							if (language == null) return;
							final Parameter versionParameter = language.parameters().stream().filter(p -> p.name().equals("version")).findFirst().orElse(null);
							versionParameter.substituteValues(Collections.singletonList(version));
						}
					}.execute();
					reload();
				}

				@Override
				public String generationPackage() {
					try {
						final File languageFile = LanguageManager.getLanguageFile(name(), effectiveVersion());
						if (!languageFile.exists()) return null;
						Manifest manifest = new JarFile(languageFile).getManifest();
						final Attributes tara = manifest.getAttributes("tara");
						if (tara == null) return null;
						return tara.getValue(TaraBuildConstants.WORKING_PACKAGE.replace(".", "-"));
					} catch (IOException e) {
						LOG.error(e.getMessage(), e);
						return null;
					}
				}
			});
		}
		return languages;

	}

	@Override
	public Map<String, String> repositories() {
		Map<String, String> repositories = new HashMap<>();
		legio.repositoryList().forEach(r -> repositories.putAll(r.typeList().stream().collect(toMap(Repository.Type::url, Repository.Type::mavenID))));
		return repositories;
	}

	public Map<String, String> releaseRepositories() {
		Map<String, String> repositories = new HashMap<>();
		legio.repositoryList().forEach(r -> repositories.putAll(r.typeList(t -> t.is(Release.class)).stream().collect(toMap(Repository.Type::url, Repository.Type::mavenID))));
		return repositories;
	}

	@Override
	public String snapshotRepository() {
		return safe(() -> legio.repositoryList().get(0).snapshot().url());
	}

	@Override
	public AbstractMap.SimpleEntry<String, String> distributionLanguageRepository() {
		return safe(() -> new AbstractMap.SimpleEntry<String, String>
				(legio.artifact().distribution().language().url(), legio.artifact().distribution().mavenId()));
	}

	@Override
	public AbstractMap.SimpleEntry<String, String> distributionReleaseRepository() {
		return safe(() -> new AbstractMap.SimpleEntry<String, String>
				(legio.artifact().distribution().release().url(), legio.artifact().distribution().mavenId()));
	}

	@Override
	public AbstractMap.SimpleEntry<String, String> distributionSnapshotRepository() {
		return safe(() -> new AbstractMap.SimpleEntry<String, String>
				(legio.artifact().distribution().snapshot().url(), legio.artifact().distribution().mavenId()));
	}


	@Override
	public Map<String, String> languageRepositories() {
		Map<String, String> repositories = new HashMap<>();
		legio.repositoryList().forEach(r -> repositories.putAll(r.typeList(t -> t.is(Repository.Language.class)).stream().collect(toMap(Repository.Type::url, Repository.Type::mavenID))));
		return repositories;
	}

	@Override
	public String outDSL() {
		return safe(() -> legio.artifact().name());
	}

	public Artifact.Generation generation() {
		return safe(() -> legio.artifact().generation());
	}


	public List<Artifact.Deployment> deployments() {
		return safe(() -> legio.artifact().deploymentList());
	}

	public Artifact.Pack pack() {
		return safe(() -> legio.artifact().pack());
	}

	public Artifact.QualityAnalytics qualityAnalytics() {
		return legio.artifact().qualityAnalytics();
	}

	public List<RunConfiguration> runConfigurations() {
		final List<Artifact.Deployment> deploys = safeList(() -> deployments());
		if (deploys == null || deploys.isEmpty()) return Collections.emptyList();
		return deploys.stream().map(this::createRunConfiguration).collect(Collectors.toList());
	}

	@NotNull
	private RunConfiguration createRunConfiguration(final Artifact.Deployment deployment) {
		return new RunConfiguration() {
			@Override
			public String name() {
				return deployment.name();
			}

			@Override
			public List<Parameter> parameters() {
				return deployment.configuration().argumentList().stream().map(p -> wrapParameter("", p)).collect(Collectors.toList());
			}

			@Override
			public List<Service> services() {
				return deployment.configuration().serviceList().stream().map(service -> new Service() {
					public String name() {
						return service.name();
					}

					public List<Parameter> parameters() {
						return service.argumentList().stream().map(p -> wrapParameter(service.name(), p)).collect(Collectors.toList());
					}
				}).collect(Collectors.toList());
			}

			@Override
			public String store() {
				return deployment.configuration().store().path();
			}

			@NotNull
			private Parameter wrapParameter(String prefix, final io.intino.legio.Argument p) {
				return new Parameter() {
					@Override
					public String name() {
						return (prefix.isEmpty() ? "" : prefix + ".") + p.name();
					}

					@Override
					public String type() {
						return "String";
					}

					@Override
					public String value() {
						return p.value();
					}
				};
			}
		};
	}

	@Override
	public String interfaceVersion() {
		return safe(() -> legio.artifact().boxing().version());
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

	public static <T> T safe(Wrapper<T> wrapper) {
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
