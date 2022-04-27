package io.intino.plugin.project.configuration;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.ProjectUtil;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import io.intino.itrules.Frame;
import io.intino.itrules.FrameBuilder;
import io.intino.plugin.file.LegioFileType;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static com.intellij.openapi.vfs.VfsUtil.findFileByIoFile;

public class LegioFileCreator {
	private final Module module;

	public LegioFileCreator(Module module) {
		this.module = module;
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
		VirtualFile moduleDir = ProjectUtil.guessModuleDir(module);
		if (moduleDir == null)
			moduleDir = VfsUtil.findFileByIoFile(new File(module.getModuleFilePath()).getParentFile(), true);
		return new File(moduleDir.getPath(), LegioFileType.LEGIO_FILE);
	}

	private Frame frame(String groupId) {
		return new FrameBuilder("legio", "empty").add("groupId", groupId).add("name", module.getName()).toFrame();
	}
}