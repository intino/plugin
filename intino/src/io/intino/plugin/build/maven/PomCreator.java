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
import io.intino.itrules.Frame;
import io.intino.itrules.FrameBuilder;
import io.intino.itrules.Template;
import io.intino.legio.graph.Artifact;
import io.intino.legio.graph.Artifact.Imports.Dependency;
import io.intino.legio.graph.Parameter;
import io.intino.legio.graph.Repository;
import io.intino.plugin.dependencyresolution.LanguageResolver;
import io.intino.plugin.project.LegioConfiguration;
import io.intino.tara.compiler.shared.Configuration;
import io.intino.tara.plugin.lang.psi.impl.TaraUtil;
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
import static io.intino.legio.graph.Artifact.Package.Mode;
import static io.intino.legio.graph.Artifact.Package.Mode.LibrariesLinkedByManifest;
import static io.intino.legio.graph.Artifact.Package.Mode.ModulesAndLibrariesLinkedByManifest;
import static io.intino.plugin.dependencyresolution.LanguageResolver.languageID;
import static io.intino.plugin.project.Safe.safe;
import static io.intino.plugin.project.Safe.safeList;
import static java.io.File.separator;
import static org.jetbrains.jps.model.java.JavaResourceRootType.RESOURCE;
import static org.jetbrains.jps.model.java.JavaResourceRootType.TEST_RESOURCE;
import static org.jetbrains.jps.model.java.JavaSourceRootType.SOURCE;
import static org.jetbrains.jps.model.java.JavaSourceRootType.TEST_SOURCE;


public class PomCreator {
	private static final Logger LOG = Logger.getInstance(PomCreator.class.getName());
	private final Module module;
	private final LegioConfiguration configuration;
	private final Mode packageType;
	private Set<Integer> randomGeneration = new HashSet<>();

	public PomCreator(Module module) {
		this.module = module;
		this.configuration = (LegioConfiguration) TaraUtil.configurationOf(module);
		packageType = safe(() -> configuration.graph().artifact().package$()) == null || configuration.graph().artifact() == null ? null : configuration.graph().artifact().package$().mode();
	}

	public File frameworkPom() {
		return ModuleTypeWithWebFeatures.isAvailable(module) ? webPom(pomFile()) : frameworkPom(pomFile());
	}

	private File webPom(File pom) {
		FrameBuilder builder = new FrameBuilder();
		fillMavenId(builder);
		final String compilerOutputUrl = CompilerProjectExtension.getInstance(module.getProject()).getCompilerOutputUrl();
		builder.add("buildDirectory", relativeToModulePath(pathOf(compilerOutputUrl)) + separator + "build" + separator);
		builder.add("outDirectory", relativeToModulePath(pathOf(compilerOutputUrl)) + separator + "production" + separator);
		addRepositories(builder);
		writePom(pom, builder.toFrame(), new ActivityPomTemplate());
		return pom;
	}

	private String relativeToModulePath(String path) {
		Path other = Paths.get(path);
		if (other == null) return moduleDirectory();
		return Paths.get(moduleDirectory()).toAbsolutePath().relativize(other.toAbsolutePath()).toFile().getPath();
	}

	private File frameworkPom(File pom) {
		Artifact.Package build = safe(() -> configuration.graph().artifact().package$());
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
		builder.add("pom").add("groupId", configuration.groupId()).add("artifactId", configuration.artifactId()).add("version", configuration.version());
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
		if (pack != null) configureBuild(builder, configuration.graph().artifact().license(), pack);
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

	private void addDependencies(FrameBuilder builder) {
		if (!packageType.equals(ModulesAndLibrariesLinkedByManifest)) addDependantModuleAsSources(builder, module);
		else builder.add("compile", " ");
		Set<String> dependencies = new HashSet<>();
		for (Dependency dependency : safeList(() -> configuration.graph().artifact().imports().dependencyList())) {
			if (dependency.toModule() && !packageType.equals(ModulesAndLibrariesLinkedByManifest)) continue;
			if (dependencies.add(dependency.identifier()))
				builder.add("dependency", createDependencyFrame(dependency));
		}
		if (!packageType.equals(ModulesAndLibrariesLinkedByManifest)) addModuleTypeDependencies(builder, dependencies);
		addLevelDependency(builder);
	}

	private void addRepositories(FrameBuilder builder) {
		configuration.repositoryTypes().stream().filter(r -> !r.i$(Repository.Language.class)).forEach(r ->
				builder.add("repository", createRepositoryFrame(r)));
		if (configuration.distributionReleaseRepository() != null)
			builder.add("repository", createDistributionRepositoryFrame(configuration.distributionReleaseRepository(), "release"));
	}

	private void addLevelDependency(FrameBuilder builder) {
		if (configuration.level() == null) return;
		for (Configuration.LanguageLibrary language : configuration.languages()) {
			final String languageId = findLanguageId(language);
			if (!languageId.isEmpty()) builder.add("dependency", createDependencyFrame(languageId.split(":")));
		}
	}

	private void addModuleTypeDependencies(FrameBuilder builder, Set<String> dependencies) {
		for (Module dependantModule : getModuleDependencies()) {
			final Configuration configuration = TaraUtil.configurationOf(dependantModule);
			for (Dependency d : safeList(() -> ((LegioConfiguration) configuration).graph().artifact().imports().dependencyList()))
				if (dependencies.add(d.identifier())) builder.add("dependency", createDependencyFrame(d));
//			if (configuration.level() == null) continue;
			for (Configuration.LanguageLibrary language : configuration.languages()) {
				final String languageID = languageID(language.name(), language.version());
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

	private void configureBuild(FrameBuilder builder, Artifact.License license, Artifact.Package aPackage) {
		if (aPackage.attachSources()) builder.add("attachSources", " ");
		if (aPackage.attachDoc()) builder.add("attachJavaDoc", " ");
		if (aPackage.isMacOS()) builder.add("osx", osx(aPackage));
		if (aPackage.isWindows()) builder.add("windows", windows(aPackage));
		final Artifact.Package.Mode type = aPackage.mode();
		if (type.equals(LibrariesLinkedByManifest) || type.equals(ModulesAndLibrariesLinkedByManifest)) {
			builder.add("linkLibraries", "true");
			FrameBuilder copyDependencies = new FrameBuilder("copyDependencies");
			builder.add("copyDependencies", copyDependencies.toFrame());
			if (aPackage.classpathPrefix() != null)
				copyDependencies.add("classpathPrefix", aPackage.classpathPrefix());
		} else builder.add("linkLibraries", "false").add("extractedLibraries", " ");
		if (aPackage.isRunnable()) builder.add("mainClass", aPackage.asRunnable().mainClass());
		configuration.graph().artifact().parameterList().forEach(parameter -> addParameter(builder, parameter));
		if (aPackage.defaultJVMOptions() != null && !aPackage.defaultJVMOptions().isEmpty())
			addMVOptions(builder, aPackage.defaultJVMOptions());
		if (aPackage.finalName() != null && !aPackage.finalName().isEmpty())
			builder.add("finalName", aPackage.finalName());
		if (license != null) builder.add("license", new FrameBuilder("license", license.type().name()).toFrame());
	}

	private void addMVOptions(FrameBuilder frame, String jvmOptions) {
		frame.add("vmOptions", jvmOptions);
	}

	private void addParameter(FrameBuilder frame, Parameter parameter) {
		final FrameBuilder pFrame = new FrameBuilder("parameter").add("key", parameter.name());
		if (parameter.defaultValue() != null) pFrame.add("value", parameter.defaultValue());
		if (parameter.description() != null && !parameter.description().isEmpty())
			pFrame.add("description", parameter.description());
		frame.add("parameter", pFrame.toFrame());
	}

	private Frame osx(Artifact.Package build) {
		final FrameBuilder builder = new FrameBuilder().add("mainClass", build.asRunnable().mainClass());
		if (build.asMacOS().macIcon() != null && !build.asMacOS().macIcon().isEmpty())
			builder.add("icon", build.asMacOS().macIcon());
		return builder.toFrame();
	}

	private Frame windows(Artifact.Package build) {
		final FrameBuilder builder = new FrameBuilder().add("mainClass", build.asRunnable().mainClass());
		builder.add("icon", build.asWindows().windowsIcon());
		final Artifact artifact = configuration.graph().artifact();
		builder.add("name", artifact.name$()).add("out", buildDirectory()).add("version", artifact.version());
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
		try {
			return new URL(path).getFile();
		} catch (MalformedURLException e) {
			return path;
		}
	}

	private String findLanguageId(Configuration.LanguageLibrary language) {
		if (packageType.equals(ModulesAndLibrariesLinkedByManifest))
			return languageID(language.name(), language.version());
		return LanguageResolver.moduleDependencyOf(module, language.name(), language.version()) != null ? "" : languageID(language.name(), language.version());
	}

	private Frame createDependencyFrame(Dependency d) {
		final FrameBuilder builder = new FrameBuilder("dependency").add("groupId", d.groupId()).
				add("scope", d.core$().graph().concept(d.getClass()).name()).add("artifactId", d.artifactId().toLowerCase()).
				add("version", d.effectiveVersion().isEmpty() ? d.version() : d.effectiveVersion());
		if (!d.excludeList().isEmpty()) for (Dependency.Exclude exclude : d.excludeList())
			builder.add("exclusion", new FrameBuilder("exclusion").add("groupId", exclude.groupId()).add("artifactId", exclude.artifactId()).toFrame());
		return builder.toFrame();
	}

	private Frame createDependencyFrame(String[] id) {
		return new FrameBuilder("dependency").add("groupId", id[0].toLowerCase()).add("scope", "compile").add("artifactId", id[1].toLowerCase()).add("version", id[2]).toFrame();
	}

	private Frame createRepositoryFrame(Repository.Type repo) {
		return new FrameBuilder("repository", repo.getClass().getSimpleName()).
				add("name", repo.mavenID()).
				add("url", repo.url()).
				add("random", generateRandom()).
				add("type", repo.i$(Repository.Snapshot.class) ? "snapshot" : "release").toFrame();
	}

	private int generateRandom() {
		int random = new Random().nextInt(10);
		while (!randomGeneration.add(random)) random = new Random().nextInt(10);
		return random;
	}

	private Frame createDistributionRepositoryFrame(AbstractMap.SimpleEntry<String, String> repo, String type) {
		return new FrameBuilder("repository", "distribution", type).
				add("url", repo.getKey()).
				add("name", repo.getValue()).
				add("type", "release").toFrame();
	}

	private void writePom(File pom, Frame frame, Template template) {
		try {
			Files.write(pom.toPath(), template.render(frame).getBytes());
		} catch (IOException e) {
			LOG.error("Error creating pomFile to publish action: " + e.getMessage());
		}
	}
}
