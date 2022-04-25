package io.intino.plugin.project;

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

	VirtualFile getOrCreate() {
		final File legioFile = legioFile();
		if (legioFile.exists()) return findFileByIoFile(legioFile, true);
		return VfsUtil.findFileByIoFile(write(new LegioFileTemplate().render(frame()), legioFile).toFile(), true);
	}

	@NotNull
	public File legioFile() {
		VirtualFile moduleDir = ProjectUtil.guessModuleDir(module);
		if (moduleDir == null)
			moduleDir = VfsUtil.findFileByIoFile(new File(module.getModuleFilePath()).getParentFile(), true);
		final File destination = new File(moduleDir.getPath(), LegioFileType.LEGIO_FILE);
		return destination;
	}

	private Frame frame() {
		return new FrameBuilder("legio", "empty").add("name", module.getName()).toFrame();
	}

	private Path write(String legio, File destiny) {
		try {
			return Files.write(destiny.toPath(), legio.getBytes());
		} catch (IOException ignored) {
		}
		return destiny.toPath();
	}
}