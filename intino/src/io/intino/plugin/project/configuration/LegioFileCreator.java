package io.intino.plugin.project.configuration;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import io.intino.itrules.Frame;
import io.intino.itrules.FrameBuilder;
import io.intino.plugin.dependencyresolution.ArtifactoryConnector;
import io.intino.plugin.dependencyresolution.Repositories;
import io.intino.plugin.lang.psi.impl.IntinoUtil;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static com.intellij.openapi.vfs.VfsUtil.findFileByIoFile;
import static io.intino.plugin.dependencyresolution.ArtifactoryConnector.repository;
import static io.intino.plugin.file.LegioFileType.ARTIFACT_LEGIO;
import static io.intino.plugin.file.LegioFileType.PROJECT_LEGIO;

public class LegioFileCreator {
	private final Module module;
	private final String[] dsls;

	public LegioFileCreator(Module module) {
		this(module, new String[0]);
	}

	public LegioFileCreator(Module module, String[] dsls) {
		this.module = module;
		this.dsls = dsls;
	}

	public LegioFileCreator(Module module, List<String> dsls) {
		this.module = module;
		this.dsls = dsls.toArray(new String[0]);
	}

	public VirtualFile getArtifact() {
		final File legioFile = legioFile();
		return legioFile.exists() ? findFileByIoFile(legioFile, true) : null;
	}

	public VirtualFile getProject(Project project) {
		final File legioFile = legioProjectFile(project);
		return legioFile.exists() ? findFileByIoFile(legioFile, true) : null;
	}

	public VirtualFile getOrCreateArtifact(String groupId) {
		final File legioFile = legioFile();
		if (legioFile.exists()) return findFileByIoFile(legioFile, true);
		return VfsUtil.findFileByIoFile(createArtifact(legioFile, groupId), true);
	}

	public void createArtifact(String legioContent) {
		write(legioContent, legioFile());
	}

	@NotNull
	public File createArtifact(File legioFile, String groupId) {
		return write(new LegioFileTemplate().render(frame(groupId)), legioFile).toFile();
	}

	public void createProjectIfNotExist(Project project) {
		File destination = legioProjectFile(project);
		if (!destination.exists()) write(new LegioFileTemplate().render(projectFrame(project.getName())), destination);
	}

	private Path write(String content, File destination) {
		try {
			return Files.write(destination.toPath(), content.getBytes());
		} catch (IOException ignored) {
		}
		return destination.toPath();
	}

	@NotNull
	public File legioFile() {
		return new File(IntinoUtil.moduleRoot(module), ARTIFACT_LEGIO);
	}

	@NotNull
	public File legioProjectFile(Project project) {
		return new File(IntinoUtil.projectRoot(project), PROJECT_LEGIO);
	}

	private Frame projectFrame(String project) {
		return new FrameBuilder("legio", "project").add("name", project).toFrame();
	}

	private Frame frame(String groupId) {
		FrameBuilder builder = new FrameBuilder("legio", "empty")
				.add("groupId", groupId)
				.add("name", module.getName());
		for (String dsl : dsls) {
			if (dsl.isEmpty()) continue;
			List<String> dslVersions = new ArtifactoryConnector(repository("intino-maven", Repositories.INTINO_RELEASES)).dslVersions(dsl);
			builder.add("dsl", new FrameBuilder("dsl")
					.add("name", dsl)
					.add("version", dslVersions == null || dslVersions.isEmpty() ? "LATEST" : dslVersions.get(dslVersions.size() - 1))
			);
		}
		return builder.toFrame();

	}

}