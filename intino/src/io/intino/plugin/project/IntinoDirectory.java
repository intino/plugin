package io.intino.plugin.project;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;

import java.io.File;

public class IntinoDirectory {

	public static final String INTINO = ".intino";

	public static File of(Project project) {
		VirtualFile baseDir = VfsUtil.findFileByIoFile(new File(project.getBasePath()), true);
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

	public static File boxDirectory(Project project) {
		VirtualFile baseDir = vfOf(project);
		VirtualFile box = baseDir.findChild("box");
		return box == null ? VfsUtil.virtualToIoFile(createDirectory(baseDir, "box")) : new File(box.getPath());
	}

	public static File boxDirectory(Module module) {
		File file = boxDirectory(module.getProject());
		File moduleBox = new File(file, module.getName());
		moduleBox.mkdirs();
		return moduleBox;
	}

	private static VirtualFile vfOf(Project project) {
		VirtualFile baseDir = VfsUtil.findFileByIoFile(new File(project.getBasePath()), true);
		VirtualFile intino = baseDir.findChild(INTINO);
		return intino == null ? createDirectory(baseDir, INTINO) : intino;
	}

	private static VirtualFile createDirectory(VirtualFile baseDir, String name) {
		File file = new File(baseDir.getPath(), name);
		file.mkdirs();
		return VfsUtil.findFileByIoFile(file, true);
	}
}