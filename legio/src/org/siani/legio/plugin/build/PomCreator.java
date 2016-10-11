package org.siani.legio.plugin.build;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.roots.CompilerModuleExtension;
import org.jetbrains.annotations.NotNull;
import org.siani.itrules.model.Frame;
import org.siani.legio.Project;
import org.siani.legio.Project.Dependencies.Compile;
import org.siani.legio.Project.Repositories.Repository;
import org.siani.legio.Project.Repositories.Snapshot;
import org.siani.legio.plugin.dependencyresolution.LanguageResolver;
import org.siani.legio.plugin.project.LegioConfiguration;
import tara.compiler.shared.Configuration;
import tara.intellij.lang.psi.impl.TaraUtil;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

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
		final Configuration configuration = TaraUtil.configurationOf(module);
		Frame frame = new Frame();
		frame.addTypes("pom");
		frame.addSlot("groupId", configuration.groupId());
		frame.addSlot("artifactId", configuration.artifactId());
		final CompilerModuleExtension compilerModuleExtension = CompilerModuleExtension.getInstance(module);
		if (compilerModuleExtension != null) {
			frame.addSlot("outDirectory", compilerModuleExtension.getCompilerOutputPath().getPath());
			frame.addSlot("testOutDirectory", compilerModuleExtension.getCompilerOutputPathForTests().getPath());
		}
		frame.addSlot("version", configuration.modelVersion());
		for (Compile dependency : ((LegioConfiguration) configuration).dependencies())
			frame.addSlot("dependency", createDependencyFrame(dependency));
		if (configuration.outDSL() != null)
			frame.addSlot("dependency", createDependencyFrame(findLanguageId(module).split(":")));
		((LegioConfiguration) configuration).legioRepositories().stream().filter(r -> !r.is(Project.Repositories.Language.class)).forEach(r ->
				frame.addSlot("repository", createRepositoryFrame(r)));
		writePom(pom, frame);
		return pom;
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
