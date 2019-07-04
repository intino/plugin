package io.intino.plugin.project;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;

import java.io.File;

public class IntinoDirectory {


	public static File of(Project project) {
		VirtualFile baseDir = project.getBaseDir();
		VirtualFile tara = baseDir.findChild(".intino");
		return tara == null ? createIntinoDirectory(baseDir) : new File(tara.getPath());
	}

	private static File createIntinoDirectory(VirtualFile baseDir) {
		File file = new File(baseDir.getPath(), ".intino");
		file.mkdirs();
		return file;
	}
}
