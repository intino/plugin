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
import io.intino.legio.LegioApplication;
import io.intino.legio.LifeCycle;
import io.intino.legio.Project;
import io.intino.legio.Project.Dependencies.Dependency;
import io.intino.legio.Project.Factory;
import io.intino.legio.Project.Repositories.Repository;
import io.intino.plugin.dependencyresolution.JavaDependencyResolver;
import io.intino.plugin.dependencyresolution.LanguageResolver;
import io.intino.plugin.dependencyresolution.LibraryManager;
import io.intino.plugin.dependencyresolution.WebDependencyResolver;
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
import tara.dsl.Legio;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.stream.Collectors;

public class LegioConfiguration implements Configuration {
	private static final Logger LOG = Logger.getInstance(GulpExecutor.class.getName());

	private static final String CONFIGURATION_LEGIO = "configuration.legio";
	private final Module module;
	private VirtualFile legioFile;
	private LegioApplication legio;

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
		reloadInterfaceBuilder();
		resolveLanguages();
		if (WebModuleType.isWebModule(module) && this.legio != null) new GulpExecutor(this.module, legio.project()).startGulpDev();
	}

	@Override
	public boolean isSuitable() {
		return new File(new File(module.getModuleFilePath()).getParentFile(), CONFIGURATION_LEGIO).exists();
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
						 if (legio != null && legio.project() != null) legio.project().save();
					 }
				 }
		);
	}

	private void reloadInterfaceBuilder() {
		final Factory.Interface interfaceNode = safe(() -> legio.project().factory().interface$());
		if (interfaceNode != null) new InterfaceBuilderManager().reload(interfaceNode.version());
	}

	private LegioApplication newGraphFromLegio() {
		Stash legioStash = loadNewConfiguration();
		return legioStash == null ? null : GraphLoader.loadGraph(legioStash, stashFile());
	}

	private Stash loadNewConfiguration() {
		try {
			return new StashBuilder(new File(legioFile.getPath()), new Legio(), module.getName()).build();
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
		if (legio == null || legio.project() == null) return;
		resolveJavaDependencies();
		if (WebModuleType.isWebModule(module) && legio.project().webDependencies() != null) resolveWebDependencies();
	}

	private void resolveJavaDependencies() {
		if (dependencies() == null) return;
		final JavaDependencyResolver resolver = new JavaDependencyResolver(module, legio.project().repositories(), dependencies());
		final List<Library> newLibraries = resolver.resolve();
		newLibraries.addAll(resolveLanguages());
		LibraryManager.removeOldLibraries(module, newLibraries);
	}

	private List<Library> resolveLanguages() {
		List<Library> libraries = new ArrayList<>();
		for (LanguageLibrary language : languages()) {
			final String effectiveVersion = language.effectiveVersion();
			libraries.addAll(new LanguageResolver(module, legio.project().repositories().repositoryList(), legio.project().factory().languageList(d -> d.name().equals(language.name())).get(0), effectiveVersion == null || effectiveVersion.isEmpty() ? language.version() : effectiveVersion).resolve());
		}
		return libraries;
	}

	private void resolveWebDependencies() {
		new WebDependencyResolver(module, legio.project(), legio.project().webDependencies()).resolve();
	}

	@Override
	public Level level() {
		if (legio == null || legio.project() == null) return null;
		final Factory factory = legio.project().factory();
		if (factory == null) return null;
		final String level = factory.node().conceptList().stream().filter(c -> c.id().contains("#")).map(c -> c.id().split("#")[0]).findFirst().orElse(null);
		return level == null ? null : Level.valueOf(level);
	}

	@Override
	public String artifactId() {
		return safe(() -> legio.project().name());
	}

	@Override
	public String groupId() {
		return safe(() -> legio.project().groupId());
	}

	public String version() {
		return safe(() -> legio.project().version());
	}

	public void version(String version) {
		reload();//TODO
	}

	@Override
	public String workingPackage() {
		return safe(() -> legio.project().factory().inPackage(), outDSL());
	}

	@Override
	public List<? extends LanguageLibrary> languages() {
		final List<Factory.Language> list = safe(() -> legio.project().factory().asLevel().languageList());
		if (list == null) return Collections.emptyList();
		List<LanguageLibrary> languages = new ArrayList<>();
		for (Factory.Language language : list) {
			languages.add(new LanguageLibrary() {
				@Override
				public String name() {
					return language.name();
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
							final Node factory = psiFile.components().get(0).components().stream().filter(f -> f.type().equals("Project.Factory")).findFirst().orElse(null);
							if (factory == null) return;
							final Node language = factory.components().stream().filter(f -> f.type().equals("Project.Factory.Language")).findFirst().orElse(null);
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
	public List<String> repositories() {
		return safe(() -> legio.project().repositories().repositoryList().stream().
				map(Repository::url).collect(Collectors.toList()));
	}

	public Map<String, String> releaseRepositories() {
		return safe(() -> legio.project().repositories().releaseList().stream().
				collect(Collectors.toMap(Repository::url, Repository::mavenId)));
	}

	@Override
	public String snapshotRepository() {
		return safe(() -> legio.project().repositories().snapshot().url());
	}

	@Override
	public AbstractMap.SimpleEntry<String, String> distributionLanguageRepository() {
		return safe(() -> new AbstractMap.SimpleEntry<String, String>
				(legio.lifeCycle().distribution().language().url(), legio.lifeCycle().distribution().language().mavenId()));
	}

	@Override
	public AbstractMap.SimpleEntry<String, String> distributionReleaseRepository() {
		return safe(() -> new AbstractMap.SimpleEntry<String, String>
				(legio.lifeCycle().distribution().release().url(), legio.lifeCycle().distribution().release().mavenId()));
	}

	@Override
	public AbstractMap.SimpleEntry<String, String> distributionSnapshotRepository() {
		return safe(() -> new AbstractMap.SimpleEntry<String, String>
				(legio.lifeCycle().distribution().snapshot().url(), legio.lifeCycle().distribution().snapshot().mavenId()));
	}

	@Override
	public String languageRepository() {
		return safe(() -> legio.project().repositories().language().url());
	}

	public String languageRepositoryId() {
		return safe(() -> legio.project().repositories().language().mavenId());
	}

	@Override
	public String outDSL() {
		return safe(() -> legio.project().name());
	}

	public Factory factory() {
		return safe(() -> legio.project().factory());
	}

	public LifeCycle lifeCycle() {
		return legio.lifeCycle();
	}

	public LifeCycle.Package build() {
		return safe(() -> legio.lifeCycle().package$());
	}

	public LifeCycle.QualityAnalytics qualityAnalytics() {
		return legio.lifeCycle().qualityAnalytics();
	}

	@Override
	public String interfaceVersion() {
		return safe(() -> legio.project().factory().interface$().version());
	}

	private String safe(StringWrapper wrapper) {
		return safe(wrapper, "");
	}

	private String safe(StringWrapper wrapper, String defaultValue) {
		try {
			return wrapper.value();
		} catch (NullPointerException e) {
			return defaultValue;
		}
	}

	public static <T> T safe(Wrapper<T> wrapper) {
		try {
			return wrapper.value();
		} catch (NullPointerException e) {
			return null;
		}
	}

	private <T> List<T> safeList(ListWrapper<T> wrapper) {
		try {
			return wrapper.value();
		} catch (NullPointerException e) {
			return Collections.emptyList();
		}
	}

	public List<Dependency> dependencies() {
		return safeList(() -> legio.project().dependencies().dependencyList());
	}

	public List<Repository> legioRepositories() {
		return safeList(() -> legio.project().repositories().repositoryList());
	}

	public Project.License licence() {
		return legio.project().license();
	}

	public Project project() {
		return safe(() -> legio.project());
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
