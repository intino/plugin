package io.intino.plugin.actions.itrules;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.List;

import static com.intellij.notification.NotificationType.ERROR;

public class TemplateGeneration extends GenerationAction {

	private static final String JAVA = ".java";

	public void actionPerformed(@NotNull AnActionEvent e) {
		Project project = e.getData(PlatformDataKeys.PROJECT);
		if (projectExists(e, project)) return;
		List<VirtualFile> rulesFiles = getVirtualFile(e);
		rulesFiles.forEach(r -> {
			final TemplateGenerator generator = createTemplate(project, r);
			if (generator != null) notify(project, r);
		});
	}

	public TemplateGenerator createTemplate(Project project, VirtualFile rulesFile) {
		if (checkDocument(project, rulesFile)) return null;
		TemplateGenerator templateGenerator;
		File destiny = getDestinyFile(rulesFile);
		try {
			templateGenerator = createTask(getModuleOf(project, rulesFile), rulesFile, "Generate Template", destiny);
		} catch (Exception e1) {
			error(project, e1.getMessage());
			return null;
		}
		ProgressManager.getInstance().run(templateGenerator);
		refreshFiles(destiny);
		return templateGenerator;
	}

	@NotNull
	private TemplateGenerator createTask(Module module, VirtualFile rulesFile, String title, File destiny) throws Exception {
		return new TemplateGenerator(rulesFile, module, title, destiny, getPackage(rulesFile, module));
	}

	private void error(Project project, String message) {
		Notifications.Bus.notify(new Notification("Itrules", "Error reading template", message, ERROR), project);
	}

	@NotNull
	private File getDestinyFile(VirtualFile rulesFile) {
		return new File(rulesFile.getParent().getPath(), classSimpleName(rulesFile.getName()) + "Template" + JAVA);
	}

	private void notify(Project project, VirtualFile rulesFile) {
		Notifications.Bus.notify(
				new Notification("Itrules", "Itrules", getDestinyFile(rulesFile).getName() + " generated", NotificationType.INFORMATION), project);
	}

	private VirtualFile find(Module module, String sourcePath) {
		VirtualFile[] sourceRoots = ModuleRootManager.getInstance(module).getSourceRoots();
		for (VirtualFile sourceRoot : sourceRoots) if (sourceRoot.getName().equals(sourcePath)) return sourceRoot;
		return null;
	}

	private String getPackage(VirtualFile file, Module module) throws Exception {
		String path = file.getParent().getPath();
		final VirtualFile virtualFile = find(module, "src");
		return virtualFile == null ? "" : format(path, virtualFile.getPath());
	}

	private String format(String path, String modulePath) {
		String name = new File(path).toURI().getPath().replace(new File(modulePath).toURI().getPath(), "");
		if (name.endsWith("/")) name = name.substring(0, name.length() - 1);
		return name.replace("/", ".");
	}

	@NotNull
	private String classSimpleName(String rulesFile) {
		String name = rulesFile.substring(0, rulesFile.lastIndexOf("."));
		return name.substring(0, 1).toUpperCase() + name.substring(1);
	}
}