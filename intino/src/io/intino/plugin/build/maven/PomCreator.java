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
import org.siani.itrules.Template;
import org.siani.itrules.model.Frame;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
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
	private Set<Integer> randomGeneration = new HashSet<>();
	private final Mode packageType;

	public PomCreator(Module module) {
		this.module = module;
		this.configuration = (LegioConfiguration) TaraUtil.configurationOf(module);
		packageType = safe(() -> configuration.graph().artifact().package$()) == null || configuration.graph().artifact() == null ? null : configuration.graph().artifact().package$().mode();
	}

	public File frameworkPom() {
		return ModuleTypeWithWebFeatures.isAvailable(module) ? webPom(pomFile()) : frameworkPom(pomFile());
	}

	private File webPom(File pom) {
		Frame frame = new Frame();
		fillMavenId(frame);
		final String compilerOutputUrl = CompilerProjectExtension.getInstance(module.getProject()).getCompilerOutputUrl();
		frame.addSlot("buildDirectory", pathOf(compilerOutputUrl) + separator + "build" + separator);
		frame.addSlot("outDirectory", pathOf(compilerOutputUrl) + separator + "production" + separator);
		addRepositories(frame);
		writePom(pom, frame, ActivityPomTemplate.create());
		return pom;
	}

	private File frameworkPom(File pom) {
		Artifact.Package build = safe(() -> configuration.graph().artifact().package$());
		Frame frame = new Frame();
		fillMavenId(frame);
		final String[] languageLevel = {"1.8"};
		final Application application = ApplicationManager.getApplication();
		if (application.isReadAccessAllowed())
			languageLevel[0] = JpsJavaSdkType.complianceOption(getEffectiveLanguageLevel(module).toJavaVersion());
		else application.runReadAction((Computable<String>) () ->
				languageLevel[0] = JpsJavaSdkType.complianceOption(getEffectiveLanguageLevel(module).toJavaVersion()));
		frame.addSlot("sdk", languageLevel[0]);
		fillFramework(build, frame);
		writePom(pom, frame, PomTemplate.create());
		return pom;
	}

	@NotNull
	private File pomFile() {
		return new File(moduleDirectory(), "pom2.xml");
	}

	private String moduleDirectory() {
		return new File(module.getModuleFilePath()).getParent();
	}

	private void fillMavenId(Frame frame) {
		frame.addTypes("pom");
		frame.addSlot("groupId", configuration.groupId());
		frame.addSlot("artifactId", configuration.artifactId());
		frame.addSlot("version", configuration.version());
	}

	private void fillFramework(Artifact.Package pack, Frame frame) {
		if (ApplicationManager.getApplication().isReadAccessAllowed()) fillDirectories(frame);
		else ApplicationManager.getApplication().runReadAction(() -> fillDirectories(frame));
		final CompilerModuleExtension extension = CompilerModuleExtension.getInstance(module);
		if (extension != null) {
			frame.addSlot("outDirectory", outDirectory(extension));
			frame.addSlot("testOutDirectory", testOutDirectory(extension));
			frame.addSlot("buildDirectory", buildDirectory());
		}
		if (pack != null) configureBuild(frame, configuration.graph().artifact().license(), pack);
		addDependencies(frame);
		addRepositories(frame);
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

	private void addDependencies(Frame frame) {
		if (!packageType.equals(ModulesAndLibrariesLinkedByManifest)) addDependantModuleAsSources(frame, module);
		else frame.addSlot("compile", "");
		Set<String> dependencies = new HashSet<>();
		for (Dependency dependency : safeList(() -> configuration.graph().artifact().imports().dependencyList())) {
			if (dependency.toModule() && !packageType.equals(ModulesAndLibrariesLinkedByManifest)) continue;
			if (dependencies.add(dependency.identifier()))
				frame.addSlot("dependency", createDependencyFrame(dependency));
		}
		if (!packageType.equals(ModulesAndLibrariesLinkedByManifest)) addModuleTypeDependencies(frame, dependencies);
		addLevelDependency(frame);
	}

	private void addRepositories(Frame frame) {
		configuration.repositoryTypes().stream().filter(r -> !r.i$(Repository.Language.class)).forEach(r ->
				frame.addSlot("repository", createRepositoryFrame(r)));
		if (configuration.distributionReleaseRepository() != null)
			frame.addSlot("repository", createDistributionRepositoryFrame(configuration.distributionReleaseRepository(), "release"));
	}

	private void addLevelDependency(Frame frame) {
		if (configuration.level() == null) return;
		for (Configuration.LanguageLibrary language : configuration.languages()) {
			final String languageId = findLanguageId(language);
			if (!languageId.isEmpty()) frame.addSlot("dependency", createDependencyFrame(languageId.split(":")));
		}
	}

	private void addModuleTypeDependencies(Frame frame, Set<String> dependencies) {
		for (Module dependantModule : getModuleDependencies()) {
			final Configuration configuration = TaraUtil.configurationOf(dependantModule);
			for (Dependency d : safeList(() -> ((LegioConfiguration) configuration).graph().artifact().imports().dependencyList()))
				if (dependencies.add(d.identifier())) frame.addSlot("dependency", createDependencyFrame(d));
			if (configuration.level() == null) continue;
			for (Configuration.LanguageLibrary language : configuration.languages()) {
				final String languageID = languageID(language.name(), language.version());
				if (languageID == null || languageID.isEmpty()) return;
				frame.addSlot("dependency", createDependencyFrame(languageID.split(":")));
			}
		}
	}

	private void fillDirectories(Frame frame) {
		frame.addSlot("sourceDirectory", srcDirectories(module));
		frame.addSlot("resourceDirectory", resourceDirectories(module).toArray(new String[0]));
		final List<String> resTest = resourceTestDirectories(module);
		frame.addSlot("resourceTestDirectory", resTest.toArray(new String[0]));
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

	private void configureBuild(Frame frame, Artifact.License license, Artifact.Package aPackage) {
		if (aPackage.attachSources()) frame.addSlot("attachSources", "");
		if (aPackage.attachDoc()) frame.addSlot("attachJavaDoc", "");
		if (aPackage.isMacOS()) frame.addSlot("osx", osx(aPackage));
		if (aPackage.isWindows()) frame.addSlot("windows", windows(aPackage));
		final Artifact.Package.Mode type = aPackage.mode();
		if (type.equals(LibrariesLinkedByManifest) || type.equals(ModulesAndLibrariesLinkedByManifest))
			frame.addSlot("linkLibraries", "true");
		else frame.addSlot("linkLibraries", "false").addSlot("extractedLibraries", "");
		if (aPackage.isRunnable()) frame.addSlot("mainClass", aPackage.asRunnable().mainClass());
		configuration.graph().artifact().parameterList().forEach(parameter -> addParameter(frame, parameter));
		if (aPackage.defaultJVMOptions() != null && !aPackage.defaultJVMOptions().isEmpty())
			addMVOptions(frame, aPackage.defaultJVMOptions());
		if (aPackage.classpathPrefix() != null) frame.addSlot("classpathPrefix", aPackage.classpathPrefix());
		if (aPackage.finalName() != null && !aPackage.finalName().isEmpty())
			frame.addSlot("finalName", aPackage.finalName());
		if (license != null) frame.addSlot("license", new Frame().addTypes("license", license.type().name()));
	}

	private void addMVOptions(Frame frame, String jvmOptions) {
		frame.addSlot("vmOptions", jvmOptions);
	}

	private void addParameter(Frame frame, Parameter parameter) {
		final Frame pFrame = new Frame().addTypes("parameter").addSlot("key", parameter.name());
		if (parameter.defaultValue() != null) pFrame.addSlot("value", parameter.defaultValue());
		if (parameter.description() != null && !parameter.description().isEmpty())
			pFrame.addSlot("description", parameter.description());
		frame.addSlot("parameter", pFrame);
	}

	private Frame osx(Artifact.Package build) {
		final Frame frame = new Frame().addSlot("mainClass", build.asRunnable().mainClass());
		if (build.asMacOS().macIcon() != null && !build.asMacOS().macIcon().isEmpty())
			frame.addSlot("icon", build.asMacOS().macIcon());
		return frame;
	}

	private Frame windows(Artifact.Package build) {
		final Frame frame = new Frame().addSlot("mainClass", build.asRunnable().mainClass());
		frame.addSlot("icon", build.asWindows().windowsIcon());
		final Artifact artifact = configuration.graph().artifact();
		frame.addSlot("name", artifact.name$()).addSlot("out", buildDirectory()).addSlot("version", artifact.version());
		frame.addSlot("prefix", build.classpathPrefix() != null ? build.classpathPrefix() : "dependency");
		return frame;
	}

	private void addDependantModuleAsSources(Frame frame, Module module) {
		for (Module dependency : collectModuleDependencies(module))
			ApplicationManager.getApplication().runReadAction(() -> {
				for (VirtualFile sourceRoot : getInstance(dependency).getModifiableModel().getSourceRoots(SOURCE))
					if (sourceRoot != null) frame.addSlot("moduleDependency", sourceRoot.getPath());
				for (VirtualFile testRoot : getInstance(dependency).getModifiableModel().getSourceRoots(TEST_SOURCE))
					if (testRoot != null) frame.addSlot("testModuleDependency", testRoot.getPath());
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
		final Frame frame = new Frame().addTypes("dependency").addSlot("groupId", d.groupId()).
				addSlot("scope", d.core$().graph().concept(d.getClass()).name()).addSlot("artifactId", d.artifactId().toLowerCase()).
				addSlot("version", d.effectiveVersion().isEmpty() ? d.version() : d.effectiveVersion());
		if (!d.excludeList().isEmpty()) for (Dependency.Exclude exclude : d.excludeList())
			frame.addSlot("exclusion", new Frame().addTypes("exclusion").
					addSlot("groupId", exclude.groupId()).addSlot("artifactId", exclude.artifactId()));
		return frame;
	}

	private Frame createDependencyFrame(String[] id) {
		return new Frame().addTypes("dependency").addSlot("groupId", id[0].toLowerCase()).addSlot("scope", "compile").addSlot("artifactId", id[1].toLowerCase()).addSlot("version", id[2]);
	}

	private Frame createRepositoryFrame(Repository.Type repo) {
		return new Frame().addTypes("repository", repo.getClass().getSimpleName()).
				addSlot("name", repo.mavenID()).
				addSlot("url", repo.url()).
				addSlot("random", generateRandom()).
				addSlot("type", repo.i$(Repository.Snapshot.class) ? "snapshot" : "release");
	}

	private int generateRandom() {
		int random = new Random().nextInt(10);
		while (!randomGeneration.add(random)) random = new Random().nextInt(10);
		return random;
	}

	private Frame createDistributionRepositoryFrame(AbstractMap.SimpleEntry<String, String> repo, String type) {
		return new Frame().addTypes("repository", "distribution", type).
				addSlot("url", repo.getKey()).
				addSlot("name", repo.getValue()).
				addSlot("type", "release");
	}

	private void writePom(File pom, Frame frame, Template template) {
		try {
			Files.write(pom.toPath(), template.format(frame).getBytes());
		} catch (IOException e) {
			LOG.error("Error creating pomFile to publish action: " + e.getMessage());
		}
	}
}
