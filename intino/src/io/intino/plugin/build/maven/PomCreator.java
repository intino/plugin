package io.intino.plugin.build.maven;

import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.LanguageLevelUtil;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleType;
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
import io.intino.itrules.Engine;
import io.intino.itrules.Frame;
import io.intino.itrules.FrameBuilder;
import io.intino.itrules.template.Template;
import io.intino.plugin.IntinoException;
import io.intino.plugin.build.FactoryPhase;
import io.intino.plugin.dependencyresolution.LanguageResolver;
import io.intino.plugin.lang.psi.impl.IntinoUtil;
import io.intino.plugin.project.configuration.ArtifactLegioConfiguration;
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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.intellij.openapi.module.WebModuleTypeBase.WEB_MODULE;
import static com.intellij.openapi.roots.ModuleRootManager.getInstance;
import static io.intino.Configuration.Artifact.Package.Mode.LibrariesLinkedByManifest;
import static io.intino.Configuration.Artifact.Package.Mode.ModulesAndLibrariesLinkedByManifest;
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
	private final ArtifactLegioConfiguration configuration;
	private final Mode packageType;

	public PomCreator(Module module) {
		this.module = module;
		this.configuration = (ArtifactLegioConfiguration) IntinoUtil.configurationOf(module);
		this.packageType = safe(() -> configuration.artifact().packageConfiguration()) == null ? null : configuration.artifact().packageConfiguration().mode();
	}

	public File artifactPom(FactoryPhase phase) throws IntinoException {
		return ModuleTypeWithWebFeatures.isAvailable(module) ? webPom(pomFile(), phase) : artifactPom(pomFile(), phase);
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

	private File artifactPom(File pom, FactoryPhase phase) {
		Artifact.Package build = safe(() -> configuration.artifact().packageConfiguration());
		FrameBuilder builder = new FrameBuilder();
		fillMavenId(builder);
		builder.add("sdk", langaugeLevel());
		fillArtifactPom(build, builder);
//		if (phase.ordinal() > INSTALL.ordinal() && !version().isSnapshot())
//			builder.add("dependencyCheck", "dependencyCheck");
		writePom(pom, builder.toFrame(), new PomTemplate());
		return pom;
	}

	private String langaugeLevel() {
		final String[] languageLevel = {"11"};
		final Application application = ApplicationManager.getApplication();
		if (application.isReadAccessAllowed()) languageLevel[0] = languageLevel();
		else application.runReadAction((Computable<String>) () -> languageLevel[0] = languageLevel());
		return languageLevel[0];
	}

	@NotNull
	private Version version() throws IntinoException {
		return new Version(configuration.artifact().version());
	}

	@NotNull
	private String languageLevel() {
		return JpsJavaSdkType.complianceOption(LanguageLevelUtil.getEffectiveLanguageLevel(module).toJavaVersion());
	}

	@NotNull
	private File pomFile() {
		return new File(moduleDirectory(), "pom.xml");
	}

	private File moduleDirectory() {
		return IntinoUtil.moduleRoot(module);
	}

	private void fillMavenId(FrameBuilder builder) {
		LegioArtifact artifact = configuration.artifact();
		builder.add("pom").add("groupId", artifact.groupId()).add("artifactId", artifact.name()).add("version", artifact.version());
	}

	private void fillArtifactPom(Artifact.Package pack, FrameBuilder builder) {
		if (ApplicationManager.getApplication().isReadAccessAllowed()) fillDirectories(builder);
		else ApplicationManager.getApplication().runReadAction(() -> fillDirectories(builder));
		final CompilerModuleExtension extension = CompilerModuleExtension.getInstance(module);
		if (extension != null) {
			builder.add("outDirectory", relativeToModulePath(outDirectory(extension)));
			builder.add("testOutDirectory", relativeToModulePath(testOutDirectory(extension)));
			builder.add("buildDirectory", relativeToModulePath(buildDirectory()) + "/");
		}
		if (pack != null) configureBuild(builder, configuration.artifact(), pack);
		addPlugins(builder);
		addDependencies(builder);
		if (ApplicationManager.getApplication().isReadAccessAllowed()) addRepositories(builder);
		else ApplicationManager.getApplication().runReadAction(() -> addRepositories(builder));
	}

	private void addPlugins(FrameBuilder builder) {
		configuration.artifact().packageConfiguration().mavenPlugins().forEach(mp -> builder.add("mavenPlugin", mp));
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
		Path modulePath = moduleDirectory().toPath();
		try {
			return modulePath.relativize(other.toAbsolutePath()).toFile().getPath();
		} catch (IllegalArgumentException e) {
			return path;
		}
	}

	private void addDependencies(FrameBuilder builder) {
		Set<Module> dependantModules = collectDependantModules(module, new HashSet<>(), false);
		if (!allModulesSeparated()) addDependantModuleAsSources(builder, dependantModules);
		else builder.add("compile", " ");
		Set<String> dependencies = new HashSet<>(dslDependencies(builder));
		List<Dependency> moduleDependencies = collectDependencies();
		for (Dependency dependency : moduleDependencies.stream().filter(d -> !d.scope().equalsIgnoreCase("test")).toList()) {
			if (dependency.toModule() && isRegistered(dependantModules, dependency) && !allModulesSeparated())
				addDependantModuleLibraries(builder, dependency, dependencies);
			else if (dependencies.add(dependency.identifier()))
				builder.add("dependency", createDependencyFrame(dependency));
		}
		for (Dependency dependency : moduleDependencies.stream().filter(d -> d.scope().equalsIgnoreCase("test")).toList())
			if ((!dependency.toModule() || (dependency.toModule() && allModulesSeparated())) && dependencies.add(dependency.identifier()))
				builder.add("dependency", createDependencyFrame(dependency));
		if (!allModulesSeparated())
			addDependantModuleTestLibraries(builder, dependencies);
	}

	private boolean isRegistered(Set<Module> dependantModules, Dependency dependency) {
		return dependantModules.stream().anyMatch(dm -> {
			Artifact artifact = IntinoUtil.configurationOf(dm).artifact();
			return artifact.name().equals(dependency.artifactId()) && artifact.groupId().equals(dependency.groupId()) &&
				   artifact.version().equals(dependency.version());
		});
	}

	private boolean allModulesSeparated() {
		return packageType.equals(ModulesAndLibrariesLinkedByManifest);
	}

	@NotNull
	private List<Dependency> collectDependencies() {
		List<Dependency> deps = new ArrayList<>(safeList(() -> configuration.artifact().dependencies())).stream().filter(d -> !(d instanceof Dependency.Web)).collect(Collectors.toList());
		Dependency datahub = configuration.artifact().datahub();
		if (datahub != null) deps.add(0, datahub);
		Dependency archetype = configuration.artifact().archetype();
		if (archetype != null) deps.add(0, archetype);
		return deps;
	}

	private void addRepositories(FrameBuilder builder) {
		configuration.repositories().forEach(r -> builder.add("repository", createRepositoryFrame(r)));
		Repository repository = repository();
		if (repository != null)
			builder.add("repository", createDistributionRepositoryFrame(repository, "release"));
	}

	private Configuration.Repository repository() {
		try {
			Version version = version();
			if (version.isSnapshot()) return safe(() -> configuration.artifact().distribution().snapshot());
			return safe(() -> configuration.artifact().distribution().release());
		} catch (IntinoException e) {
			LOG.error(e);
			return null;
		}
	}

	private List<String> dslDependencies(FrameBuilder builder) {
		List<String> dependencies = new ArrayList<>();
		for (Artifact.Dsl dsl : configuration.artifact().dsls()) {
			if (dsl != null) {
				final String[] runtimeCoors = runtimeCoors(dsl);
				if (runtimeCoors != null) {
					dependencies.add(String.join(":", runtimeCoors));
					builder.add("dependency", createDependencyFrame(runtimeCoors));
				}
			}
		}
		return dependencies;
	}

	private void addDependantModuleLibraries(FrameBuilder builder, Dependency dependency, Set<String> dependencies) {
		Module dependantModule = findModuleOf(dependency, dependency.scope().equalsIgnoreCase("test"));
		if (dependantModule == null) return;
		final Configuration configuration = IntinoUtil.configurationOf(dependantModule);
		if (WEB_MODULE.equals(ModuleType.get(module).getId()) && configuration instanceof ArtifactLegioConfiguration)
			((ArtifactLegioConfiguration) configuration).reloadDependencies();
		safeList(() -> configuration.artifact().dependencies()).stream().
				filter(d -> (!d.toModule()) && !d.scope().equalsIgnoreCase("test") && dependencies.add(d.identifier())).
				forEach(d -> builder.add("dependency", createDependencyFrame(d)));
		safeList(() -> configuration.artifact().dependencies()).stream().
				filter(d -> (d.toModule()) && !d.scope().equalsIgnoreCase("test") && dependencies.add(d.identifier())).
				forEach(d -> addDependantModuleLibraries(builder, d, dependencies));
		configuration.artifact().dsls().stream()
				.map(d -> LanguageResolver.runtimeCoors(d.name(), d.version()))
				.filter(id -> id != null && !id.isEmpty())
				.forEach(id -> builder.add("dependency", createDependencyFrame(id.split(":"))));
	}

	private Module findModuleOf(Dependency dependency, boolean includeTests) {
		return getModuleDependencies(includeTests).stream().filter(m -> {
			Configuration configuration = IntinoUtil.configurationOf(m);
			Artifact artifact = configuration.artifact();
			return artifact.groupId().equalsIgnoreCase(dependency.groupId()) &&
				   artifact.name().equalsIgnoreCase(dependency.artifactId()) &&
				   artifact.version().equalsIgnoreCase(dependency.version());
		}).findFirst().orElse(null);
	}

	private void addDependantModuleTestLibraries(FrameBuilder builder, Set<String> dependencies) {
		for (Module dependantModule : getModuleDependencies(true)) {
			final Configuration configuration = IntinoUtil.configurationOf(dependantModule);
			safeList(() -> configuration.artifact().dependencies()).stream().
					filter(d -> (d.scope().equalsIgnoreCase("test")) && dependencies.add(d.identifier())).
					forEach(d -> builder.add("dependency", createDependencyFrame(d)));
		}
	}

	private void fillDirectories(FrameBuilder builder) {
		builder.add("sourceDirectory", srcDirectories(module));
		builder.add("resourceDirectory", resourceDirectories(module).stream().map(this::relativeToModulePath).toArray(String[]::new));
		final List<String> resTest = resourceTestDirectories(module);
		builder.add("resourceTestDirectory", resTest.stream().map(this::relativeToModulePath).toArray(String[]::new));
	}

	@NotNull
	private List<Module> getModuleDependencies(boolean includeTests) {
		return new ArrayList<>(collectDependantModules(this.module, new HashSet<>(), includeTests));
	}

	private Set<Module> collectDependantModules(Module module, Set<Module> collection, boolean includeTests) {
		for (Module dependant : getInstance(module).getModuleDependencies(includeTests)) {
			if (!collection.contains(dependant))
				collection.addAll(collectDependantModules(dependant, collection, includeTests));
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
		for (Module dependency : collectDependantModules(module, new HashSet<>(), false)) {
			if (ModuleTypeWithWebFeatures.isAvailable(dependency)) {
				final CompilerModuleExtension extension = CompilerModuleExtension.getInstance(dependency);
				if (extension != null && extension.getCompilerOutputUrl() != null)
					resources.add(outDirectory(extension));
			} else if (!allModulesSeparated()) {
				List<VirtualFile> roots = getInstance(dependency).getSourceRoots(RESOURCE);
				resources.addAll(roots.stream().map(VirtualFile::getPath).toList());
			}
		}
		return resources;
	}

	private List<String> resourceTestDirectories(Module module) {
		List<VirtualFile> sourceRoots = getInstance(module).getSourceRoots(TEST_RESOURCE);
		List<String> list = sourceRoots.stream().map(VirtualFile::getPath).collect(Collectors.toList());
		for (Module dep : getModuleDependencies(true))
			list.addAll(getInstance(dep).getSourceRoots(TEST_RESOURCE).stream().map(VirtualFile::getPath).toList());
		return list;
	}

	private void configureBuild(FrameBuilder builder, Artifact artifact, Artifact.Package aPackage) {
		if (artifact.url() != null) builder.add("url", artifact.url());
		if (artifact.description() != null) builder.add("description", artifact.description());
		if (aPackage.attachSources()) builder.add("attachSources", " ");
		if (aPackage.signArtifactWithGpg()) builder.add("gpgSign", " ");
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
		builder.add("developer", artifact.developers().stream().map(d -> new FrameBuilder("developer").
				add("name", d.name()).
				add("email", d.email()).
				add("organization", d.organization()).
				add("organizationUrl", d.organizationUrl()).
				toFrame()).toArray(Frame[]::new));
		if (artifact.license() != null)
			builder.add("license", new FrameBuilder("license", artifact.license().type().name()).toFrame());
		if (artifact.license() != null) {
			Artifact.Scm scm = artifact.scm();
			builder.add("scm", new FrameBuilder("scm").add("url", scm.url()).
					add("connection", scm.connection()).
					add("developerConnection", scm.developerConnection()).
					add("tag", scm.tag()).toFrame());
		}
	}

	private void addMVOptions(FrameBuilder frame, String jvmOptions) {
		frame.add("vmOptions", jvmOptions);
	}

	private void addParameter(FrameBuilder frame, Configuration.Parameter parameter) {
		final FrameBuilder pFrame = new FrameBuilder("parameter").add("name", parameter.name());
		if (parameter.value() != null) pFrame.add("value", parameter.value());
		pFrame.add("required", parameter.value() == null);
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

	private void addDependantModuleAsSources(FrameBuilder builder, Set<Module> dependantModules) {
		dependantModules.forEach(dependency -> addModuleAsSources(builder, dependency));
	}

	private void addModuleAsSources(FrameBuilder builder, Module dependency) {
		ApplicationManager.getApplication().runReadAction(() -> {
			for (VirtualFile sourceRoot : getInstance(dependency).getModifiableModel().getSourceRoots(SOURCE))
				if (sourceRoot != null) builder.add("moduleDependency", relativeToModulePath(sourceRoot.getPath()));
			for (VirtualFile testRoot : getInstance(dependency).getModifiableModel().getSourceRoots(TEST_SOURCE))
				if (testRoot != null) builder.add("testModuleDependency", relativeToModulePath(testRoot.getPath()));
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

	private String[] runtimeCoors(Artifact.Dsl dsl) {
		Artifact.Dsl.Runtime runtime = dsl.runtime();
		if (runtime == null || runtime.groupId() == null) return null;
		return new String[]{runtime.groupId(), runtime.artifactId(), runtime.version()};
	}

	private Frame createDependencyFrame(Dependency d) {
		final FrameBuilder builder = new FrameBuilder("dependency").
				add("scope", d.scope()).
				add("groupId", d.groupId()).
				add("artifactId", d.artifactId()).
				add("version", d.version());
		if (!d.excludes().isEmpty()) for (Dependency.Exclude exclude : d.excludes())
			builder.add("exclusion", new FrameBuilder("exclusion")
					.add("groupId", exclude.groupId())
					.add("artifactId", exclude.artifactId()).toFrame());
		return builder.toFrame();
	}

	private Frame createDependencyFrame(String[] id) {
		return new FrameBuilder("dependency")
				.add("scope", "compile")
				.add("groupId", id[0].toLowerCase())
				.add("artifactId", id[1].toLowerCase())
				.add("version", id[2]).toFrame();
	}

	private Frame createRepositoryFrame(Repository repo) {
		return new FrameBuilder("repository", repo.getClass().getSimpleName()).
				add("name", repo.identifier()).
				add("url", repo.url()).
				add("type", (repo instanceof Repository.Snapshot) ? "-snapshot" : "").
				add("snapshot", repo instanceof Repository.Snapshot).toFrame();
	}

	private Frame createDistributionRepositoryFrame(Repository repo, String type) {
		return new FrameBuilder("repository", "distribution", type).
				add("url", repo.url()).
				add("name", repo.identifier()).
				add("type", "release").toFrame();
	}

	private void writePom(File pom, Frame frame, Template template) {
		try {
			Files.writeString(pom.toPath(), new Engine(template).render(frame));
		} catch (IOException e) {
			LOG.error("Error creating pomFile to publish action: " + e.getMessage());
		}
	}

	private boolean nodeInstalled() {
		return new File(System.getProperty("user.home"), "/node/node").exists() || new File(System.getProperty("user.home"), "/node/node.exe").exists();
	}

}
