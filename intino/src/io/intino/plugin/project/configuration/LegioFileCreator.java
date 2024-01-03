package io.intino.plugin.project.configuration;

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import io.intino.itrules.Frame;
import io.intino.itrules.FrameBuilder;
import io.intino.plugin.lang.psi.impl.IntinoUtil;
import io.intino.plugin.project.ArtifactorySensor;
import io.intino.plugin.project.module.IntinoWizardPanel.Components;
import io.intino.tara.dsls.Meta;
import io.intino.tara.dsls.Proteo;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

import static com.intellij.openapi.vfs.VfsUtil.findFileByIoFile;
import static io.intino.plugin.file.LegioFileType.ARTIFACT_LEGIO;
import static io.intino.plugin.file.LegioFileType.PROJECT_LEGIO;
import static io.intino.plugin.project.ArtifactorySensor.LanguageLibrary;

public class LegioFileCreator {
	private final Module module;
	private final List<Components> components;

	public LegioFileCreator(Module module) {
		this(module, Collections.emptyList());
	}

	public LegioFileCreator(Module module, List<Components> components) {
		this.module = module;
		this.components = components;
	}

	public VirtualFile getArtifact() {
		final File legioFile = legioFile();
		return legioFile.exists() ? findFileByIoFile(legioFile, true) : null;
	}

	public VirtualFile getProject(Project project) {
		final File legioFile = legioProjectFile(project);
		return legioFile.exists() ? findFileByIoFile(legioFile, true) : null;
	}

	VirtualFile getOrCreateArtifact(String groupId) {
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
		PropertiesComponent propertiesComponent = PropertiesComponent.getInstance();
		FrameBuilder builder = new FrameBuilder("legio", "empty")
				.add("groupId", groupId)
				.add("name", module.getName());
		if (components.contains(Components.Model) || components.contains(Components.MetaModel)) {
			boolean isModel = components.contains(Components.Model);
			List<String> modelBuilderVersions = propertiesComponent.getList(ArtifactorySensor.ModelBuilder);
			List<String> dslVersions = propertiesComponent.getList(LanguageLibrary + (isModel ? Proteo.class.getSimpleName() : Meta.class.getSimpleName()));
			builder.add("factory", new FrameBuilder("factory")
					.add("dsl", isModel ? Proteo.class.getSimpleName() : Meta.class.getSimpleName())
					.add("dslVersion", dslVersions == null || dslVersions.isEmpty() ? "LATEST" : dslVersions.get(dslVersions.size() - 1))
					.add("sdk", modelBuilderVersions == null || modelBuilderVersions.isEmpty() ? "LATEST" : modelBuilderVersions.get(modelBuilderVersions.size() - 1))
			);
			builder.add("level", isModel ? "Product" : "Platform");
		}
		if (hasBoxComponents()) {
			List<String> list = propertiesComponent.getList(ArtifactorySensor.BoxBuilder);
			builder.add("box", new FrameBuilder("box")
					.add("version", list == null || list.isEmpty() ? "LATEST" : list.get(list.size() - 1)));
		}
		return builder.toFrame();

	}

	private boolean hasBoxComponents() {
		return components.stream().anyMatch(c -> c.ordinal() > 1);
	}
}