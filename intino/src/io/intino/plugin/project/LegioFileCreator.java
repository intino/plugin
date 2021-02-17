package io.intino.plugin.project;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.ProjectUtil;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import io.intino.itrules.FrameBuilder;
import io.intino.plugin.file.LegioFileType;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static com.intellij.openapi.vfs.VfsUtil.findFileByIoFile;

class LegioFileCreator {
	private final Module module;

	LegioFileCreator(Module module) {
		this.module = module;
	}

	VirtualFile getOrCreate() {
		VirtualFile moduleDir = ProjectUtil.guessModuleDir(module);
		if (moduleDir == null)
			moduleDir = VfsUtil.findFileByIoFile(new File(module.getModuleFilePath()).getParentFile(), true);
		FrameBuilder builder = new FrameBuilder("legio", "empty").add("name", module.getName());
		final File destination = new File(moduleDir.getPath(), LegioFileType.LEGIO_FILE);
		if (destination.exists()) return findFileByIoFile(destination, true);
		return VfsUtil.findFileByIoFile(write(new LegioFileTemplate().render(builder.toFrame()), destination).toFile(), true);
	}

	private Path write(String legio, File destiny) {
		try {
			return Files.write(destiny.toPath(), legio.getBytes());
		} catch (IOException ignored) {
		}
		return destiny.toPath();
	}
}