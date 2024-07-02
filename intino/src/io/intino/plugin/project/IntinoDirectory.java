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

	public static boolean exists(Project project) {
		VirtualFile baseDir = VfsUtil.findFileByIoFile(new File(project.getBasePath()), true);
		VirtualFile intino = baseDir.findChild(INTINO);
		return intino != null && new File(intino.getPath()).exists();
	}

	public static File artifactsDirectory(Project project) {
		VirtualFile baseDir = vfOf(project);
		VirtualFile artifacts = baseDir.findChild("artifacts");
		return artifacts == null ? VfsUtil.virtualToIoFile(createDirectory(baseDir, "artifacts")) : new File(artifacts.getPath());
	}

	public static File dslDirectory(Project project, String dsl) {
		VirtualFile baseDir = vfOf(project);
		VirtualFile model = baseDir.findChild(dsl);
		return model == null ? VfsUtil.virtualToIoFile(createDirectory(baseDir, dsl)) : new File(model.getPath());
	}

	public static File dslDirectory(Module module, String dsl) {
		File file = dslDirectory(module.getProject(), dsl);
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