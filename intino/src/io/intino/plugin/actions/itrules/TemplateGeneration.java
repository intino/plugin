package io.intino.plugin.actions.itrules;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import io.intino.Configuration;
import io.intino.plugin.IntinoException;
import io.intino.plugin.lang.psi.impl.IntinoUtil;
import io.intino.plugin.project.configuration.ArtifactLegioConfiguration;
import io.intino.plugin.project.configuration.Version;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.List;

import static com.intellij.notification.NotificationType.ERROR;
import static io.intino.plugin.project.configuration.model.DependencyFactory.createCompile;

public class TemplateGeneration extends GenerationAction {
	private static final String[] ItRulesCoors = new String[]{"io.intino.itrules", "engine", "2.0.0"};
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
		File outputFile = outputFile(rulesFile);
		Module module = getModuleOf(project, rulesFile);
		try {
			templateGenerator = createTask(module, rulesFile, outputFile);
		} catch (Exception e1) {
			error(project, e1.getMessage());
			return null;
		}
		ProgressManager.getInstance().run(templateGenerator);
		refreshFiles(outputFile);
		addItrulesEngineToConfiguration(module);
		return templateGenerator;
	}

	private void addItrulesEngineToConfiguration(Module module) {
		Configuration configuration = IntinoUtil.configurationOf(module);
		if (!(configuration instanceof ArtifactLegioConfiguration legio)) return;
		Configuration.Artifact.Dependency dep = legio.artifact().dependencies().stream().filter(d -> d.groupId().equals(ItRulesCoors[0]) && d.artifactId().equals(ItRulesCoors[1])).findFirst().orElse(null);
		if (dep == null)
			legio.artifact().addDependencies(createCompile(ItRulesCoors[0], ItRulesCoors[1], ItRulesCoors[2]));
		else if (!dep.version().equals(ItRulesCoors[2]) && isHigher(dep.version())) dep.version(ItRulesCoors[2]);
	}

	private boolean isHigher(String version) {
		try {
			return new Version(ItRulesCoors[2]).compareTo(new Version(version)) > 0;
		} catch (IntinoException e) {
			Logger.getInstance(this.getClass()).error(e);
			return false;
		}
	}

	@Override
	public @NotNull ActionUpdateThread getActionUpdateThread() {
		return ActionUpdateThread.BGT;
	}

	@NotNull
	private TemplateGenerator createTask(Module module, VirtualFile rulesFile, File destination) {
		return new TemplateGenerator(rulesFile, module, "Generate Template", destination, getPackage(rulesFile, module));
	}

	private void error(Project project, String message) {
		Notifications.Bus.notify(new Notification("Intino", "Error reading template", message, ERROR), project);
	}

	@NotNull
	private File outputFile(VirtualFile rulesFile) {
		return new File(rulesFile.getParent().getPath(), classSimpleName(rulesFile.getName()) + "Template" + JAVA);
	}

	private void notify(Project project, VirtualFile rulesFile) {
		Notifications.Bus.notify(
				new Notification("Intino", "Itrules", outputFile(rulesFile).getName() + " generated", NotificationType.INFORMATION), project);
	}

	private String getPackage(VirtualFile file, Module module) {
		String path = file.getParent().getPath();
		final VirtualFile virtualFile = srcRoot(module);
		return virtualFile == null ? "" : format(path, virtualFile.getPath());
	}

	private VirtualFile srcRoot(Module module) {
		VirtualFile[] sourceRoots = ModuleRootManager.getInstance(module).getSourceRoots();
		for (VirtualFile sourceRoot : sourceRoots) if (sourceRoot.getName().equals("src")) return sourceRoot;
		return null;
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