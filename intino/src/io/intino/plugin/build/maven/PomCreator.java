package io.intino.plugin.build.maven;

import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleTypeWithWebFeatures;
import com.intellij.openapi.roots.CompilerModuleExtension;
import com.intellij.openapi.roots.CompilerProjectExtension;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.vfs.VirtualFile;
import io.intino.Configuration;
import io.intino.Configuration.Artifact;
import io.intino.Configuration.Artifact.Dependency;
import io.intino.Configuration.Artifact.Package.Mode;
import io.intino.Configuration.Repository;
import io.intino.itrules.Frame;
import io.intino.itrules.FrameBuilder;
import io.intino.itrules.Template;
import io.intino.plugin.IntinoException;
import io.intino.plugin.build.FactoryPhase;
import io.intino.plugin.dependencyresolution.LanguageResolver;
import io.intino.plugin.lang.psi.impl.TaraUtil;
import io.intino.plugin.project.LegioConfiguration;
import io.intino.plugin.project.configuration.Version;
import io.intino.plugin.project.configuration.model.LegioArtifact;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.jps.model.java.JpsJavaSdkType;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import static com.intellij.openapi.module.EffectiveLanguageLevelUtil.getEffectiveLanguageLevel;
import static com.intellij.openapi.roots.ModuleRootManager.getInstance;
import static io.intino.Configuration.Artifact.Package.Mode.LibrariesLinkedByManifest;
import static io.intino.Configuration.Artifact.Package.Mode.ModulesAndLibrariesLinkedByManifest;
import static io.intino.plugin.dependencyresolution.LanguageResolver.languageId;
import static io.intino.plugin.project.Safe.safe;
import static io.intino.plugin.project.Safe.safeList;
import static java.io.File.separator;
import static org.jetbrains.jps.model.java.JavaResourceRootType.RESOURCE;
import static org.jetbrains.jps.model.java.JavaResourceRootType.TEST_RESOURCE;
import static org.jetbrains.jps.model.java.JavaSourceRootType.SOURCE;
import static org.jetbrains.jps.model.java.JavaSourceRootType.TEST_SOURCE;


class PomCreator {
	private static final Logger LOG = Logger.getInstance(PomCreator.class.getName());
	private final Module module;
	private final LegioConfiguration configuration;
	private final Mode packageType;
	private Set<Integer> randomGeneration = new HashSet<>();

	PomCreator(Module module) {
		this.module = module;
		this.configuration = (LegioConfiguration) TaraUtil.configurationOf(module);
		packageType = safe(() -> configuration.artifact().packageConfiguration()) == null || configuration.artifact() == null ? null : configuration.artifact().packageConfiguration().mode();
	}

	File frameworkPom(FactoryPhase phase) {
		return ModuleTypeWithWebFeatures.isAvailable(module) ? webPom(pomFile(), phase) : frameworkPom(pomFile());
	}

	private File webPom(File pom, FactoryPhase phase) {
		FrameBuilder builder = new FrameBuilder();
		fillMavenId(builder);
		final String compilerOutputUrl = CompilerProjectExtension.getInstance(module.getProject()).getCompilerOutputUrl();
		builder.add("buildDirectory", relativeToModulePath(pathOf(compilerOutputUrl)) + separator + "build" + separator);
		builder.add("outDirectory", relativeToModulePath(pathOf(compilerOutputUrl)) + separator + "production" + separator);
		builder.add("build", new FrameBuilder(phase.name().toLowerCase()).add("nodeInstalled", nodeInstalled()).toFrame());
		addRepositories(builder);
		writePom(pom, builder.toFrame(), new UIAccessorPomTemplate());
		return pom;
	}

	private File frameworkPom(File pom) {
		Artifact.Package build = safe(() -> configuration.artifact().packageConfiguration());
		FrameBuilder builder = new FrameBuilder();
		fillMavenId(builder);
		final String[] languageLevel = {"1.8"};
		final Application application = ApplicationManager.getApplication();
		if (application.isReadAccessAllowed())
			languageLevel[0] = JpsJavaSdkType.complianceOption(getEffectiveLanguageLevel(module).toJavaVersion());
		else application.runReadAction((Computable<String>) () ->
				languageLevel[0] = JpsJavaSdkType.complianceOption(getEffectiveLanguageLevel(module).toJavaVersion()));
		builder.add("sdk", languageLevel[0]);
		fillFramework(build, builder);
		writePom(pom, builder.toFrame(), new PomTemplate());
		return pom;
	}

	@NotNull
	private File pomFile() {
		return new File(moduleDirectory(), "pom.xml");
	}

	private String moduleDirectory() {
		return new File(module.getModuleFilePath()).getParent();
	}

	private void fillMavenId(FrameBuilder builder) {
		LegioArtifact artifact = configuration.artifact();
		builder.add("pom").add("groupId", artifact.groupId()).add("artifactId", artifact.name()).add("version", artifact.version());
	}

	private void fillFramework(Artifact.Package pack, FrameBuilder builder) {
		if (ApplicationManager.getApplication().isReadAccessAllowed()) fillDirectories(builder);
		else ApplicationManager.getApplication().runReadAction(() -> fillDirectories(builder));
		final CompilerModuleExtension extension = CompilerModuleExtension.getInstance(module);
		if (extension != null) {
			builder.add("outDirectory", relativeToModulePath(outDirectory(extension)));
			builder.add("testOutDirectory", relativeToModulePath(testOutDirectory(extension)));
			builder.add("buildDirectory", relativeToModulePath(buildDirectory()) + "/");
		}
		if (pack != null) configureBuild(builder, configuration.artifact().licence(), pack);
		addDependencies(builder);
		addRepositories(builder);
	}

	@NotNull
	private String buildDirectory() {
		return projectOutDirectory() + separator + "build" + separator;
	}

	private String outDirectory(CompilerModuleExtension extension) {
		return pathOf(extension.getCompilerOutputUrl());
	}

	private String testOutDirectory(CompilerModuleExtension extension) {
		return pathOf(extension.getCompilerOutputUrlForTests());
	}

	private String projectOutDirectory() {
		return pathOf(CompilerProjectExtension.getInstance(module.getProject()).getCompilerOutputUrl());
	}

	private String relativeToModulePath(String path) {
		Path other = Paths.get(path);
		Path modulePath = Paths.get(moduleDirectory()).toAbsolutePath();
		try {
			return modulePath.relativize(other.toAbsolutePath()).toFile().getPath();
		} catch (IllegalArgumentException e) {
			return path;
		}
	}

	private void addDependencies(FrameBuilder builder) {
		if (!packageType.equals(ModulesAndLibrariesLinkedByManifest)) addDependantModuleAsSources(builder, module);
		else builder.add("compile", " ");
		Set<String> dependencies = new HashSet<>();
		for (Dependency dependency : collectDependencies()) {
			if (dependency.toModule() && !packageType.equals(ModulesAndLibrariesLinkedByManifest)) continue;
			if (dependencies.add(dependency.identifier()))
				builder.add("dependency", createDependencyFrame(dependency));
		}
		if (!packageType.equals(ModulesAndLibrariesLinkedByManifest))
			addDependantModuleLibraries(builder, dependencies);
		addLevelDependency(builder);
	}

	@NotNull
	private List<Dependency> collectDependencies() {
		List<Dependency> deps = new ArrayList<>(safeList(() -> configuration.artifact().dependencies())).stream().filter(d -> !(d instanceof Dependency.Web)).collect(Collectors.toList());
		Dependency.DataHub datahub = configuration.artifact().datahub();
		if (datahub != null) deps.add(datahub);
		return deps;
	}

	private void addRepositories(FrameBuilder builder) {
		configuration.repositories().stream().filter(r -> !(r instanceof Repository.Language)).forEach(r ->
				builder.add("repository", createRepositoryFrame(r)));
		Repository releaseRepository = repository();
		if (releaseRepository != null)
			builder.add("repository", createDistributionRepositoryFrame(releaseRepository, "release"));
	}

	private Configuration.Repository repository() {
		try {
			Version version = new Version(configuration.artifact().version());
			if (version.isSnapshot()) return safe(() -> configuration.artifact().distribution().snapshot());
			return safe(() -> configuration.artifact().distribution().release());
		} catch (IntinoException e) {
			LOG.error(e);
			return null;
		}
	}

	private void addLevelDependency(FrameBuilder builder) {
		Artifact.Model.Language language = safe(() -> configuration.artifact().model().language());
		if (language != null) {
			final String languageId = findLanguageId(language);
			if (!languageId.isEmpty()) builder.add("dependency", createDependencyFrame(languageId.split(":")));
		}
	}

	private void addDependantModuleLibraries(FrameBuilder builder, Set<String> dependencies) {
		for (Module dependantModule : getModuleDependencies()) {
			final Configuration configuration = TaraUtil.configurationOf(dependantModule);
			safeList(() -> configuration.artifact().dependencies()).stream().
					filter(d -> !d.toModule() && dependencies.add(d.identifier())).
					forEach(d -> builder.add("dependency", createDependencyFrame(d)));
			if (safe(() -> configuration.artifact().model()) == null) continue;
			Artifact.Model.Language language = configuration.artifact().model().language();
			if (language != null) {
				final String languageID = languageId(language.name(), language.version());
				if (languageID == null || languageID.isEmpty()) return;
				builder.add("dependency", createDependencyFrame(languageID.split(":")));
			}
		}
	}

	private void fillDirectories(FrameBuilder builder) {
		builder.add("sourceDirectory", srcDirectories(module));
		builder.add("resourceDirectory", resourceDirectories(module).stream().map(this::relativeToModulePath).toArray(String[]::new));
		final List<String> resTest = resourceTestDirectories(module);
		builder.add("resourceTestDirectory", resTest.stream().map(this::relativeToModulePath).toArray(String[]::new));
	}

	@NotNull
	private List<Module> getModuleDependencies() {
		return collectModuleDependencies(this.module);
	}

	private List<Module> collectModuleDependencies(Module module) {
		return new ArrayList<>(collectModuleDependencies(module, new HashSet<>()));
	}

	private Set<Module> collectModuleDependencies(Module module, Set<Module> collection) {
		for (Module dependant : getInstance(module).getModuleDependencies()) {
			if (!collection.contains(dependant)) collection.addAll(collectModuleDependencies(dependant, collection));
			collection.add(dependant);
		}
		return collection;
	}

	private String[] srcDirectories(Module module) {
		final ModuleRootManager manager = getInstance(module);
		final List<VirtualFile> sourceRoots = manager.getModifiableModel().getSourceRoots(SOURCE);
		return sourceRoots.stream().map(VirtualFile::getName).toArray(String[]::new);
	}

	private List<String> resourceDirectories(Module module) {
		final List<VirtualFile> sourceRoots = getInstance(module).getSourceRoots(RESOURCE);
		final List<String> resources = sourceRoots.stream().map(VirtualFile::getPath).collect(Collectors.toList());
		for (Module dependency : collectModuleDependencies(module, new HashSet<>())) {
			if (ModuleTypeWithWebFeatures.isAvailable(dependency)) {
				final CompilerModuleExtension extension = CompilerModuleExtension.getInstance(dependency);
				if (extension != null && extension.getCompilerOutputUrl() != null)
					resources.add(outDirectory(extension));
			} else if (!packageType.equals(ModulesAndLibrariesLinkedByManifest)) {
				List<VirtualFile> roots = getInstance(dependency).getSourceRoots(RESOURCE);
				resources.addAll(roots.stream().map(VirtualFile::getPath).collect(Collectors.toList()));
			}
		}
		return resources;
	}

	private List<String> resourceTestDirectories(Module module) {
		List<VirtualFile> sourceRoots = getInstance(module).getSourceRoots(TEST_RESOURCE);
		List<String> list = sourceRoots.stream().map(VirtualFile::getPath).collect(Collectors.toList());
		for (Module dep : getModuleDependencies())
			list.addAll(getInstance(dep).getSourceRoots(TEST_RESOURCE).stream().map(VirtualFile::getPath).collect(Collectors.toList()));
		return list;
	}

	private void configureBuild(FrameBuilder builder, Artifact.Licence license, Artifact.Package aPackage) {
		if (aPackage.attachSources()) builder.add("attachSources", " ");
		if (aPackage.attachDoc()) builder.add("attachJavaDoc", " ");
		if (aPackage.isRunnable()) {
			if (aPackage.macOsConfiguration() != null) builder.add("osx", osx(aPackage));
			if (aPackage.windowsConfiguration() != null) builder.add("windows", windows(aPackage));
		}
		final Artifact.Package.Mode type = aPackage.mode();
		if (type.equals(LibrariesLinkedByManifest) || type.equals(ModulesAndLibrariesLinkedByManifest)) {
			builder.add("linkLibraries", "true");
			FrameBuilder copyDependencies = new FrameBuilder("copyDependencies");
			builder.add("copyDependencies", copyDependencies.toFrame());
			if (aPackage.classpathPrefix() != null)
				copyDependencies.add("classpathPrefix", aPackage.classpathPrefix());
		} else builder.add("linkLibraries", "false").add("extractedLibraries", " ");
		if (aPackage.isRunnable()) builder.add("mainClass", aPackage.mainClass());
		configuration.artifact().parameters().forEach(parameter -> addParameter(builder, parameter));
		if (aPackage.defaultJVMOptions() != null && !aPackage.defaultJVMOptions().isEmpty())
			addMVOptions(builder, aPackage.defaultJVMOptions());
		if (aPackage.finalName() != null && !aPackage.finalName().isEmpty())
			builder.add("finalName", aPackage.finalName());
		if (license != null) builder.add("license", new FrameBuilder("license", license.type().name()).toFrame());
	}

	private void addMVOptions(FrameBuilder frame, String jvmOptions) {
		frame.add("vmOptions", jvmOptions);
	}

	private void addParameter(FrameBuilder frame, Configuration.Parameter parameter) {
		final FrameBuilder pFrame = new FrameBuilder("parameter").add("key", parameter.name());
		if (parameter.value() != null) pFrame.add("value", parameter.value());
		if (parameter.description() != null && !parameter.description().isEmpty())
			pFrame.add("description", parameter.description());
		frame.add("parameter", pFrame.toFrame());
	}

	private Frame osx(Artifact.Package build) {
		final FrameBuilder builder = new FrameBuilder().add("mainClass", build.mainClass());
		String icon = safe(() -> build.macOsConfiguration().icon());
		if (icon != null && !icon.isEmpty())
			builder.add("icon", icon);
		return builder.toFrame();
	}

	private Frame windows(Artifact.Package build) {
		final FrameBuilder builder = new FrameBuilder().add("mainClass", build.mainClass());
		builder.add("icon", build.windowsConfiguration().icon());
		final Artifact artifact = configuration.artifact();
		builder.add("name", artifact.name()).add("out", buildDirectory()).add("version", artifact.version());
		builder.add("prefix", build.classpathPrefix() != null ? build.classpathPrefix() : "dependency");
		return builder.toFrame();
	}

	private void addDependantModuleAsSources(FrameBuilder builder, Module module) {
		for (Module dependency : collectModuleDependencies(module))
			ApplicationManager.getApplication().runReadAction(() -> {
				for (VirtualFile sourceRoot : getInstance(dependency).getModifiableModel().getSourceRoots(SOURCE))
					if (sourceRoot != null) builder.add("moduleDependency", sourceRoot.getPath());
				for (VirtualFile testRoot : getInstance(dependency).getModifiableModel().getSourceRoots(TEST_SOURCE))
					if (testRoot != null) builder.add("testModuleDependency", testRoot.getPath());
			});
	}

	private String pathOf(String path) {
		if (path.startsWith("file://")) return path.substring("file://".length());
		try {
			return new URL(path).getFile();
		} catch (MalformedURLException e) {
			return path;
		}
	}

	private String findLanguageId(Artifact.Model.Language language) {
		if (packageType.equals(ModulesAndLibrariesLinkedByManifest))
			return languageId(language.name(), language.version());
		return LanguageResolver.moduleDependencyOf(module, language.name(), language.version()) != null ? "" : languageId(language.name(), language.version());
	}

	private Frame createDependencyFrame(Dependency d) {
		final FrameBuilder builder = new FrameBuilder("dependency").add("groupId", d.groupId()).
				add("scope", d.scope()).
				add("artifactId", d.artifactId()).
				add("version", d.effectiveVersion().isEmpty() ? d.version() : d.effectiveVersion());
		if (!d.excludes().isEmpty()) for (Dependency.Exclude exclude : d.excludes())
			builder.add("exclusion", new FrameBuilder("exclusion").add("groupId", exclude.groupId()).add("artifactId", exclude.artifactId()).toFrame());
		return builder.toFrame();
	}

	private Frame createDependencyFrame(String[] id) {
		return new FrameBuilder("dependency").add("groupId", id[0].toLowerCase()).add("scope", "compile").add("artifactId", id[1].toLowerCase()).add("version", id[2]).toFrame();
	}

	private Frame createRepositoryFrame(Repository repo) {
		return new FrameBuilder("repository", repo.getClass().getSimpleName()).
				add("name", repo.identifier()).
				add("url", repo.url()).
				add("random", generateRandom()).
				add("type", (repo instanceof Repository.Snapshot) ? "snapshot" : "release").toFrame();
	}

	private int generateRandom() {
		int random = new Random().nextInt(10);
		while (!randomGeneration.add(random)) random = new Random().nextInt(10);
		return random;
	}

	private Frame createDistributionRepositoryFrame(Repository repo, String type) {
		return new FrameBuilder("repository", "distribution", type).
				add("url", repo.url()).
				add("name", repo.identifier()).
				add("type", "release").toFrame();
	}

	private void writePom(File pom, Frame frame, Template template) {
		try {
			Files.write(pom.toPath(), template.render(frame).getBytes());
		} catch (IOException e) {
			LOG.error("Error creating pomFile to publish action: " + e.getMessage());
		}
	}

	private boolean nodeInstalled() {
		return new File(System.getProperty("user.home"), "/node/node").exists() || new File(System.getProperty("user.home"), "/node/node.exe").exists();
	}

}
