package io.intino.plugin.project.configuration;

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import io.intino.itrules.Frame;
import io.intino.itrules.FrameBuilder;
import io.intino.magritte.dsl.Meta;
import io.intino.magritte.dsl.Proteo;
import io.intino.plugin.file.LegioFileType;
import io.intino.plugin.lang.psi.impl.IntinoUtil;
import io.intino.plugin.project.ArtifactorySensor;
import io.intino.plugin.project.module.IntinoWizardPanel.Components;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

import static com.intellij.openapi.vfs.VfsUtil.findFileByIoFile;
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

	public VirtualFile get() {
		final File legioFile = legioFile();
		if (legioFile.exists()) return findFileByIoFile(legioFile, true);
		return null;
	}

	VirtualFile getOrCreate(String groupId) {
		final File legioFile = legioFile();
		if (legioFile.exists()) return findFileByIoFile(legioFile, true);
		return VfsUtil.findFileByIoFile(create(legioFile, groupId), true);
	}

	public void create(String legioContent) {
		create(legioContent, legioFile());
	}

	@NotNull
	public File create(File legioFile, String groupId) {
		return create(new LegioFileTemplate().render(frame(groupId)), legioFile).toFile();
	}

	private Path create(String content, File destination) {
		try {
			return Files.write(destination.toPath(), content.getBytes());
		} catch (IOException ignored) {
		}
		return destination.toPath();
	}

	@NotNull
	public File legioFile() {
		File root = IntinoUtil.moduleRoot(module);
		return new File(root, LegioFileType.LEGIO_FILE);
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