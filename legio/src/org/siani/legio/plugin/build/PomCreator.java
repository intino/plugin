package org.siani.legio.plugin.build;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import org.jetbrains.annotations.NotNull;
import org.siani.itrules.model.Frame;
import org.siani.legio.Project.Dependencies.Compile;
import org.siani.legio.Project.Repositories.Repository;
import org.siani.legio.Project.Repositories.Snapshot;
import org.siani.legio.plugin.project.LegioConfiguration;
import tara.intellij.lang.psi.impl.TaraUtil;
import tara.intellij.project.configuration.Configuration;

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
		frame.addSlot("version", configuration.modelVersion());
		for (Compile dependency : ((LegioConfiguration) configuration).dependencies())
			frame.addSlot("dependency", createDependencyFrame(dependency));
		for (Repository repository : ((LegioConfiguration) configuration).legioRepositories())
			frame.addSlot("repository", createRepositoryFrame(repository));
		writePom(pom, frame);
		return pom;
	}

	private static Frame createDependencyFrame(Compile id) {
		return new Frame().addTypes("dependency").addSlot("groupId", id.groupId()).addSlot("artifactId", id.artifactId()).addSlot("version", id.version());
	}

	private static Frame createRepositoryFrame(Repository repo) {
		return new Frame().addTypes("repository", repo.getClass().getSimpleName()).
				addSlot("name", repo.name()).addSlot("url", repo.url()).
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
