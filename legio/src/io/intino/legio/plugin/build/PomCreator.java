package io.intino.legio.plugin.build;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.roots.CompilerModuleExtension;
import com.intellij.openapi.roots.CompilerProjectExtension;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.siani.itrules.model.Frame;
import io.intino.legio.Project;
import io.intino.legio.Project.Dependencies.Compile;
import io.intino.legio.Project.Repositories.Repository;
import io.intino.legio.Project.Repositories.Snapshot;
import io.intino.legio.plugin.dependencyresolution.LanguageResolver;
import io.intino.legio.plugin.project.LegioConfiguration;
import tara.compiler.shared.Configuration;
import tara.intellij.lang.psi.impl.TaraUtil;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;

import static io.intino.legio.Project.Build.Package.Type.LibrariesExtracted;
import static io.intino.legio.Project.Build.Package.Type.OnlyLibrariesLinkedByManifest;

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
		Project.Build build = configuration.build();
		Frame frame = new Frame();
		frame.addTypes("pom");
		frame.addSlot("groupId", configuration.groupId());
		frame.addSlot("artifactId", configuration.artifactId());
		final CompilerModuleExtension compilerModuleExtension = CompilerModuleExtension.getInstance(module);
		if (compilerModuleExtension != null) {
			frame.addSlot("outDirectory", pathOf(compilerModuleExtension.getCompilerOutputUrl()));
			frame.addSlot("testOutDirectory", pathOf(compilerModuleExtension.getCompilerOutputUrlForTests()));
			frame.addSlot("buildDirectory", pathOf(CompilerProjectExtension.getInstance(module.getProject()).getCompilerOutputUrl()) + File.separator + "build" + File.separator);
		}
		frame.addSlot("version", configuration.modelVersion());
		if (build != null) configureBuild(module, frame, build);
		for (Compile dependency : configuration.dependencies())
			frame.addSlot("dependency", createDependencyFrame(dependency));
		if (configuration.outDSL() != null)
			frame.addSlot("dependency", createDependencyFrame(findLanguageId(module).split(":")));
		configuration.legioRepositories().stream().filter(r -> !r.is(Project.Repositories.Language.class)).forEach(r ->
				frame.addSlot("repository", createRepositoryFrame(r)));
		writePom(pom, frame);
		return pom;
	}

	private static void configureBuild(Module module, Frame frame, Project.Build build) {
		if (build.attachSources()) frame.addSlot("attachSources", "");
		if (build.attachDoc()) frame.addSlot("attachJavaDoc", "");
		final Project.Build.Package.Type type = build.package$().type();
		if (type.equals(OnlyLibrariesLinkedByManifest)) {
			frame.addSlot("linkLibraries", "true");
			addDependantModuleSources(frame, module);
		} else if (type.equals(LibrariesExtracted)) {
			frame.addSlot("linkLibraries", "false");
			frame.addSlot("extractedLibraries", "");
			addDependantModuleSources(frame, module);
		}
		if (build.mainClass() != null) frame.addSlot("mainClass", build.mainClass());
		if (build.finalName() != null && !build.finalName().isEmpty()) frame.addSlot("finalName", build.finalName());
		if (!build.licenseList().isEmpty())
			for (Project.Build.License license : build.licenseList())
				frame.addSlot("license", new Frame().addTypes("license", license.type().name()));

	}

	private static void addDependantModuleSources(Frame frame, Module module) {
		final Module[] moduleDependencies = ModuleRootManager.getInstance(module).getModuleDependencies();
		for (Module dependency : moduleDependencies) {
			final VirtualFile[] sourceRoots = ModuleRootManager.getInstance(dependency).getModifiableModel().getSourceRoots(false);
			for (VirtualFile sourceRoot : sourceRoots)
				if (sourceRoot != null) frame.addSlot("moduleDependency", sourceRoot.getPath());
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
		return LanguageResolver.findLanguageId(configuration.dsl(), configuration.dslVersion());
	}

	private static Frame createDependencyFrame(Compile id) {
		return new Frame().addTypes("dependency").addSlot("groupId", id.groupId()).addSlot("artifactId", id.artifactId()).addSlot("version", id.version());
	}

	private static Frame createDependencyFrame(String[] id) {
		return new Frame().addTypes("dependency").addSlot("groupId", id[0]).addSlot("artifactId", id[1]).addSlot("version", id[2]);
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
