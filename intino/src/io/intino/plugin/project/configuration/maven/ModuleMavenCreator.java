package io.intino.plugin.project.configuration.maven;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import io.intino.itrules.Frame;
import io.intino.itrules.FrameBuilder;
import org.jetbrains.idea.maven.project.MavenProject;
import org.jetbrains.idea.maven.project.MavenProjectsManager;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;

public class ModuleMavenCreator {

	private static final String POM_XML = "pom.xml";
	private final Module module;

	public ModuleMavenCreator(Module module) {
		this.module = module;
	}

	public void mavenize() {
		VirtualFile pomFile = createPom(module);
		if (pomFile == null) return;
		MavenProjectsManager manager = MavenProjectsManager.getInstance(module.getProject());
		manager.addManagedFilesOrUnignore(Collections.singletonList(pomFile));
		manager.importProjects();
		manager.forceUpdateAllProjectsOrFindAllAvailablePomFiles();
		FileEditorManager.getInstance(module.getProject()).openFile(pomFile, true);
	}

	private VirtualFile createPom(final Module module) {
		final PsiFile[] files = new PsiFile[1];
		ApplicationManager.getApplication().runWriteAction(() -> {
			MavenProject project = MavenProjectsManager.getInstance(module.getProject()).findProject(module);
			if (project == null) {
				PsiDirectory root = getModuleRoot(module);
				files[0] = root.findFile(POM_XML);
				if (files[0] == null)
					writePom((files[0] = root.createFile(POM_XML)).getVirtualFile().getPath(), new ModulePomTemplate().render(createModuleFrame(module)));
			}
		});
		return files[0] == null ? null : files[0].getVirtualFile();
	}


	private PsiDirectory getModuleRoot(Module module) {
		VirtualFile moduleFile = module.getModuleFile();
		final PsiManager manager = PsiManager.getInstance(module.getProject());
		PsiDirectory directory = moduleFile != null ?
				manager.findDirectory(moduleFile.getParent()) :
				manager.findDirectory(VfsUtil.findFile(new File(module.getProject().getBasePath()).toPath(), true)).findSubdirectory(module.getName());
		if (directory == null) directory = create(manager, new File(module.getModuleFilePath()).getParentFile());
		return directory;
	}

	private PsiDirectory create(PsiManager manager, File moduleDir) {
		moduleDir.mkdirs();
		final VirtualFile file = VfsUtil.findFileByIoFile(moduleDir, true);
		if (file != null)
			return manager.findDirectory(file);
		return null;
	}

	private void writePom(String path, String text) {
		try {
			File file = new File(path);
			FileWriter writer = new FileWriter(file);
			writer.write(text);
			writer.close();
		} catch (IOException ignored) {
		}
	}

	private Frame createModuleFrame(Module module) {
		FrameBuilder builder = new FrameBuilder("pom")
				.add("project", module.getProject().getName())
				.add("name", module.getName())
				.add("version", "1.0");
		if (new File(module.getModuleFilePath()).getParent().equals(new File(module.getProject().getBasePath()).getAbsolutePath()))
			builder.add("default", "");
		return builder.toFrame();
	}

}