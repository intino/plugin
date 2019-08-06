package io.intino.plugin.project;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectUtil;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;

import java.io.File;

public class IntinoDirectory {

	public static final String INTINO = ".intino";

	public static File of(Project project) {
		VirtualFile baseDir = ProjectUtil.guessProjectDir(project);
		VirtualFile intino = baseDir.findChild(INTINO);
		return intino == null ? VfsUtil.virtualToIoFile(createDirectory(baseDir, INTINO)) : new File(intino.getPath());
	}

	public static File artifactsDirectory(Project project) {
		VirtualFile baseDir = vfOf(project);
		VirtualFile artifacts = baseDir.findChild("artifacts");
		return artifacts == null ? VfsUtil.virtualToIoFile(createDirectory(baseDir, "artifacts")) : new File(artifacts.getPath());
	}

	public static File auditDirectory(Project project) {
		VirtualFile baseDir = vfOf(project);
		VirtualFile audit = baseDir.findChild("audit");
		return audit == null ? VfsUtil.virtualToIoFile(createDirectory(baseDir, "audit")) : new File(audit.getPath());
	}

	private static VirtualFile vfOf(Project project) {
		VirtualFile baseDir = ProjectUtil.guessProjectDir(project);
		VirtualFile intino = baseDir.findChild(INTINO);
		return intino == null ? createDirectory(baseDir, INTINO) : intino;
	}

	private static VirtualFile createDirectory(VirtualFile baseDir, String name) {
		File file = new File(baseDir.getPath(), name);
		file.mkdirs();
		return VfsUtil.findFileByIoFile(file, true);
	}
}