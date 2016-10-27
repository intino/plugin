package io.intino.legio.plugin.project;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import org.siani.itrules.model.Frame;

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

	VirtualFile create() {
		final String legio = LegioTemplate.create().format(new Frame().addTypes("legio").addSlot("name", module.getName()));
		final File destiny = new File(new File(module.getModuleFilePath()).getParent(), "configuration.legio");
		if (destiny.exists()) return findFileByIoFile(destiny, true);
		return VfsUtil.findFileByIoFile(write(legio, destiny).toFile(), true);
	}

	private Path write(String legio, File destiny) {
		try {
			return Files.write(destiny.toPath(), legio.getBytes());
		} catch (IOException ignored) {
		}
		return destiny.toPath();
	}
}
