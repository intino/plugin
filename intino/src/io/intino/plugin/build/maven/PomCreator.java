package io.intino.plugin.build.maven;

import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
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
import org.siani.itrules.Template;
import org.siani.itrules.model.Frame;
<<<<<<< HEAD
import io.intino.tara.compiler.shared.Configuration;
import io.intino.tara.plugin.lang.psi.impl.TaraUtil;
=======
>>>>>>> release/1.2.0

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;

import static com.intellij.openapi.module.EffectiveLanguageLevelUtil.getEffectiveLanguageLevel;
import static com.intellij.openapi.module.WebModuleTypeBase.isWebModule;
import static io.intino.legio.graph.Artifact.Package.Mode;
import static io.intino.legio.graph.Artifact.Package.Mode.*;
import static org.jetbrains.jps.model.java.JavaResourceRootType.RESOURCE;
import static org.jetbrains.jps.model.java.JavaResourceRootType.TEST_RESOURCE;
import static org.jetbrains.jps.model.java.JavaSourceRootType.SOURCE;


public class PomCreator {
	private static final Logger LOG = Logger.getInstance(PomCreator.class.getName());
	private final Module module;
	private final LegioConfiguration configuration;
	private Set<Integer> randomGeneration = new HashSet<>();
	private final Mode packageType;

	public PomCreator(Module module) {
		this.module = module;
		this.configuration = (LegioConfiguration) TaraUtil.configurationOf(module);
		packageType = configuration.pack() == null || configuration.artifact() == null ? null : configuration.artifact().package$().mode();
	}

	public File frameworkPom() throws IOException {
		final File pom = pomFile();
		return isWebModule(module) ? webPom(pom) : frameworkPom(pom);
	}

	private File webPom(File pom) {
		Frame frame = new Frame();
		fillMavenId(frame);
		frame.addSlot("buildDirectory", pathOf(CompilerProjectExtension.getInstance(module.getProject()).getCompilerOutputUrl()) + File.separator + "build" + File.separator);
		frame.addSlot("outDirectory", pathOf(CompilerProjectExtension.getInstance(module.getProject()).getCompilerOutputUrl()) + File.separator + "production" + File.separator);
		addRepositories(frame);
		writePom(pom, frame, ActivityPomTemplate.create());
		return pom;
	}

	private File frameworkPom(File pom) {
		Artifact.Package build = configuration.pack();
		Frame frame = new Frame();
		fillMavenId(frame);
		final String[] languageLevel = {"1.8"};
		final Application application = ApplicationManager.getApplication();
		if (application.isReadAccessAllowed())
			languageLevel[0] = getEffectiveLanguageLevel(module).getCompilerComplianceDefaultOption();
		else application.runReadAction((Computable<String>) () ->
				languageLevel[0] = getEffectiveLanguageLevel(module).getCompilerComplianceDefaultOption());
		frame.addSlot("sdk", languageLevel[0]);
		fillFramework(build, frame);
		writePom(pom, frame, PomTemplate.create());
		return pom;
	}

	@NotNull
	private File pomFile() throws IOException {
		return new File(new File(module.getModuleFilePath()).getParent(), "pom2.xml");
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
			frame.addSlot("outDirectory", pathOf(extension.getCompilerOutputUrl()));
			frame.addSlot("testOutDirectory", pathOf(extension.getCompilerOutputUrlForTests()));
			frame.addSlot("buildDirectory", pathOf(CompilerProjectExtension.getInstance(module.getProject()).getCompilerOutputUrl()) + File.separator + "build" + File.separator);
		}
		if (pack != null) configureBuild(frame, configuration.licence(), pack);
		addDependencies(frame);
		addRepositories(frame);
	}

	private void addDependencies(Frame frame) {
		if (packageType.equals(LibrariesLinkedByManifest) || packageType.equals(ModulesAndLibrariesExtracted))
			addDependantModuleSources(frame, module);
		Set<String> dependencies = new HashSet<>();
		for (Dependency dependency : configuration.dependencies()) {
			if (dependency.toModule() && !packageType.equals(ModulesAndLibrariesLinkedByManifest)) continue;
			if (dependencies.add(dependency.identifier()))
				frame.addSlot("dependency", createDependencyFrame(dependency));
		}
		addModuleTypeDependencies(frame, dependencies);
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
			for (Dependency d : ((LegioConfiguration) configuration).dependencies())
				if (dependencies.add(d.identifier())) frame.addSlot("dependency", createDependencyFrame(d));
			if (configuration.level() == null) continue;
			for (Configuration.LanguageLibrary language : configuration.languages()) {
				final String languageID = LanguageResolver.languageID(language.name(), language.version());
				if (languageID == null || languageID.isEmpty()) return;
				frame.addSlot("dependency", createDependencyFrame(languageID.split(":")));
			}
		}
	}

	private void fillDirectories(Frame frame) {
		frame.addSlot("sourceDirectory", srcDirectories(module));
		final List<String> res = resourceDirectories(module);
		for (Module dep : getModuleDependencies())
			res.addAll(resourceDirectories(dep));
		frame.addSlot("resourceDirectory", res.toArray(new String[res.size()]));
		final List<String> resTest = resourceTestDirectories(module);
		for (Module dep : getModuleDependencies())
			resTest.addAll(resourceTestDirectories(dep));
		frame.addSlot("resourceTestDirectory", resTest.toArray(new String[resTest.size()]));
	}

	@NotNull
	private List<Module> getModuleDependencies() {
		return collectModuleDependencies(this.module);
	}

	private List<Module> collectModuleDependencies(Module module) {
		List<Module> list = new ArrayList<>();
		for (Module dependant : ModuleRootManager.getInstance(module).getModuleDependencies()) {
			list.add(dependant);
			list.addAll(collectModuleDependencies(dependant));
		}
		return list;
	}

	private String[] srcDirectories(Module module) {
		final ModuleRootManager manager = ModuleRootManager.getInstance(module);
		final List<VirtualFile> sourceRoots = manager.getModifiableModel().getSourceRoots(SOURCE);
		return sourceRoots.stream().map(VirtualFile::getName).toArray(String[]::new);
	}

	private List<String> resourceDirectories(Module module) {
		final ModuleRootManager manager = ModuleRootManager.getInstance(module);
		final List<VirtualFile> sourceRoots = manager.getSourceRoots(RESOURCE);
		final List<String> resources = sourceRoots.stream().map(VirtualFile::getPath).collect(Collectors.toList());
		for (Module dependency : collectModuleDependencies(module)) {
			if (isWebModule(dependency)) {
				final CompilerModuleExtension extension = CompilerModuleExtension.getInstance(dependency);
				if (extension != null && extension.getCompilerOutputUrl() != null)
					resources.add(pathOf(extension.getCompilerOutputUrl()));
			}
		}
		return resources;
	}

	private List<String> resourceTestDirectories(Module module) {
		final ModuleRootManager manager = ModuleRootManager.getInstance(module);
		final List<VirtualFile> sourceRoots = manager.getSourceRoots(TEST_RESOURCE);
		return sourceRoots.stream().map(VirtualFile::getPath).collect(Collectors.toList());
	}

	private void configureBuild(Frame frame, Artifact.License license, Artifact.Package build) {
		if (build.attachSources()) frame.addSlot("attachSources", "");
		if (build.attachDoc()) frame.addSlot("attachJavaDoc", "");
		final Artifact.Package.Mode type = build.mode();
		if (type.equals(LibrariesLinkedByManifest) || type.equals(ModulesAndLibrariesLinkedByManifest))
			frame.addSlot("linkLibraries", "true");
		else frame.addSlot("linkLibraries", "false").addSlot("extractedLibraries", "");
		if (build.isRunnable()) frame.addSlot("mainClass", build.asRunnable().mainClass());
		for (Parameter parameter : build.parameterList())
			frame.addSlot("parameter", new Frame().addTypes("parameter").addSlot("key", parameter.name()).addSlot("value", parameter.value()));
		if (build.classpathPrefix() != null) frame.addSlot("classpathPrefix", build.classpathPrefix());
		if (build.finalName() != null && !build.finalName().isEmpty()) frame.addSlot("finalName", build.finalName());
		if (license != null)
			frame.addSlot("license", new Frame().addTypes("license", license.type().name()));
	}

	private void addDependantModuleSources(Frame frame, Module module) {
		for (Module dependency : collectModuleDependencies(module)) {
			ApplicationManager.getApplication().runReadAction(() -> {
				final VirtualFile[] sourceRoots = ModuleRootManager.getInstance(dependency).getModifiableModel().getSourceRoots(false);
				for (VirtualFile sourceRoot : sourceRoots)
					if (sourceRoot != null) frame.addSlot("moduleDependency", sourceRoot.getPath());
			});
		}
	}

	private String pathOf(String path) {
		try {
			return new URL(path).getFile();
		} catch (MalformedURLException e) {
			return path;
		}
	}

	private String findLanguageId(Configuration.LanguageLibrary language) {
		return LanguageResolver.moduleDependencyOf(module, language.name(), language.version()) != null ? "" : LanguageResolver.languageID(language.name(), language.version());
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
		Integer random = new Random().nextInt(10);
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
