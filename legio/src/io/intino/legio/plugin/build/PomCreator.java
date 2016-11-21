package io.intino.legio.plugin.build;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.roots.CompilerModuleExtension;
import com.intellij.openapi.roots.CompilerProjectExtension;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import io.intino.legio.LifeCycle;
import io.intino.legio.Project;
import io.intino.legio.Project.Dependencies.Dependency;
import io.intino.legio.Project.Repositories.Repository;
import io.intino.legio.Project.Repositories.Snapshot;
import io.intino.legio.plugin.dependencyresolution.LanguageResolver;
import io.intino.legio.plugin.project.LegioConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.jps.model.java.JavaResourceRootType;
import org.jetbrains.jps.model.java.JavaSourceRootType;
import org.siani.itrules.model.Frame;
import tara.compiler.shared.Configuration;
import tara.intellij.lang.psi.impl.TaraUtil;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.util.List;

import static io.intino.legio.LifeCycle.Package.Type.LibrariesLinkedByManifest;
import static io.intino.legio.LifeCycle.Package.Type.ModulesAndLibrariesLinkedByManifest;


class PomCreator {
	private static final Logger LOG = Logger.getInstance(PomCreator.class.getName());

	static File createFrameworkPom(Module module) throws IOException {
		return createFrameworkPom(module, pom(module));
	}

	@NotNull
	private static File pom(Module module) throws IOException {
		return new File(new File(module.getModuleFilePath()).getParent(), "pom2.xml");
	}

	private static File createFrameworkPom(Module module, File pom) {
		final LegioConfiguration configuration = (LegioConfiguration) TaraUtil.configurationOf(module);
		LifeCycle.Package build = configuration.build();
		Frame frame = new Frame();
		frame.addTypes("pom");
		frame.addSlot("groupId", configuration.groupId());
		frame.addSlot("artifactId", configuration.artifactId());
		if (ApplicationManager.getApplication().isReadAccessAllowed()) fillDirectories(module, frame);
		else ApplicationManager.getApplication().runReadAction(() -> fillDirectories(module, frame));
		final CompilerModuleExtension compilerModuleExtension = CompilerModuleExtension.getInstance(module);
		if (compilerModuleExtension != null) {
			frame.addSlot("outDirectory", pathOf(compilerModuleExtension.getCompilerOutputUrl()));
			frame.addSlot("testOutDirectory", pathOf(compilerModuleExtension.getCompilerOutputUrlForTests()));
			frame.addSlot("buildDirectory", pathOf(CompilerProjectExtension.getInstance(module.getProject()).getCompilerOutputUrl()) + File.separator + "build" + File.separator);
		}
		frame.addSlot("version", configuration.modelVersion());
		if (build != null) configureBuild(module, frame, configuration.licence(), build);
		for (Dependency dependency : configuration.dependencies())
			frame.addSlot("dependency", createDependencyFrame(dependency));
		addModuleTypeDependencies(module, frame);
		if (configuration.level() != null) {
			final String languageId = findLanguageId(module);
			if (!languageId.isEmpty()) frame.addSlot("dependency", createDependencyFrame(languageId.split(":")));
		}
		configuration.legioRepositories().stream().filter(r -> !r.is(Project.Repositories.Language.class)).forEach(r ->
				frame.addSlot("repository", createRepositoryFrame(r)));
		frame.addSlot("repository", createRepositoryFrame(configuration.lifeCycle().distribution()));
		writePom(pom, frame);
		return pom;
	}

	private static void addModuleTypeDependencies(Module module, Frame frame) {
		for (Module dependantModule : ModuleRootManager.getInstance(module).getModuleDependencies()) {
			final Configuration configuration = TaraUtil.configurationOf(dependantModule);
			for (Dependency d : ((LegioConfiguration) configuration).dependencies())
				frame.addSlot("dependency", createDependencyFrame(d));
			if (configuration.level() != null)
				frame.addSlot("dependency", createDependencyFrame(findLanguageId(dependantModule).split(":")));
		}
	}

	private static void fillDirectories(Module module, Frame frame) {
		frame.addSlot("sourceDirectory", srcDirectories(module));
		frame.addSlot("resourceDirectory", resourceDirectories(module));
	}

	private static String[] srcDirectories(Module module) {
		final ModuleRootManager manager = ModuleRootManager.getInstance(module);
		final List<VirtualFile> sourceRoots = manager.getModifiableModel().getSourceRoots(JavaSourceRootType.SOURCE);
		return sourceRoots.stream().map(VirtualFile::getName).toArray(String[]::new);
	}

	private static String[] resourceDirectories(Module module) {
		final ModuleRootManager manager = ModuleRootManager.getInstance(module);
		final List<VirtualFile> sourceRoots = manager.getSourceRoots(JavaResourceRootType.RESOURCE);
		return sourceRoots.stream().map(VirtualFile::getName).toArray(String[]::new);
	}

	private static void configureBuild(Module module, Frame frame, Project.License license, LifeCycle.Package build) {
		if (build.attachSources()) frame.addSlot("attachSources", "");
		if (build.attachDoc()) frame.addSlot("attachJavaDoc", "");
		final LifeCycle.Package.Type type = build.type();
		if (type.equals(LibrariesLinkedByManifest) || type.equals(ModulesAndLibrariesLinkedByManifest))
			frame.addSlot("linkLibraries", "true");
		else frame.addSlot("linkLibraries", "false").addSlot("extractedLibraries", "");
		addDependantModuleSources(frame, module);
		if (build.mainClass() != null) frame.addSlot("mainClass", build.mainClass());
		if (build.classpathPrefix() != null) frame.addSlot("classpathPrefix", build.classpathPrefix());
		if (build.finalName() != null && !build.finalName().isEmpty()) frame.addSlot("finalName", build.finalName());
		if (license != null)
			frame.addSlot("license", new Frame().addTypes("license", license.type().name()));

	}

	private static void addDependantModuleSources(Frame frame, Module module) {
		final Module[] moduleDependencies = ModuleRootManager.getInstance(module).getModuleDependencies();
		for (Module dependency : moduleDependencies) {
			ApplicationManager.getApplication().runReadAction(() -> {
				final VirtualFile[] sourceRoots = ModuleRootManager.getInstance(dependency).getModifiableModel().getSourceRoots(false);
				for (VirtualFile sourceRoot : sourceRoots)
					if (sourceRoot != null) frame.addSlot("moduleDependency", sourceRoot.getPath());
			});
		}
	}

	private static String pathOf(String path) {
		try {
			return new File(new URL(path).getPath()).getPath();
		} catch (MalformedURLException e) {
			return path;
		}
	}

	private static String findLanguageId(Module module) {
		final Configuration configuration = TaraUtil.configurationOf(module);
		return LanguageResolver.moduleOf(module, configuration.dsl(), configuration.dslVersion()) != null ? "" : LanguageResolver.findLanguageId(configuration.dsl(), configuration.dslVersion());
	}

	private static Frame createDependencyFrame(Dependency id) {
		return new Frame().addTypes("dependency").addSlot("groupId", id.groupId()).addSlot("scope", id.concept().name()).addSlot("artifactId", id.artifactId()).addSlot("version", id.version());
	}

	private static Frame createDependencyFrame(String[] id) {
		return new Frame().addTypes("dependency").addSlot("groupId", id[0]).addSlot("scope", "compile").addSlot("artifactId", id[1]).addSlot("version", id[2]);
	}

	private static Frame createRepositoryFrame(Repository repo) {
		return new Frame().addTypes("repository", repo.getClass().getSimpleName()).
				addSlot("name", repo.mavenId()).addSlot("url", repo.url()).
				addSlot("type", repo instanceof Snapshot ? "snapshot" : "release");
	}

	private static void writePom(File pom, Frame frame) {
		try {
			Files.write(pom.toPath(), PomTemplate.create().format(frame).getBytes());
		} catch (IOException e) {
			LOG.error("Error creating pom to export: " + e.getMessage());
		}
	}
}
