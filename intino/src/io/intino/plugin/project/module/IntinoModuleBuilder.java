package io.intino.plugin.project.module;

import com.intellij.ide.util.projectWizard.JavaModuleBuilder;
import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import com.intellij.ide.util.projectWizard.WizardContext;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.module.ModifiableModuleModel;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ContentEntry;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.ui.configuration.ModulesProvider;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import io.intino.Configuration;
import io.intino.plugin.IntinoIcons;
import io.intino.plugin.project.IntinoDirectory;
import io.intino.plugin.project.configuration.LegioFileCreator;
import io.intino.plugin.project.configuration.MavenConfiguration;
import io.intino.plugin.project.configuration.ModuleTemplateDeployer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jps.model.java.JavaResourceRootType;
import org.jetbrains.jps.model.java.JavaSourceRootType;
import org.jetbrains.jps.model.java.JpsJavaExtensionService;

import javax.swing.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.intellij.openapi.application.ApplicationManager.getApplication;
import static io.intino.plugin.project.configuration.ConfigurationManager.*;

public class IntinoModuleBuilder extends JavaModuleBuilder {

	private IntinoModuleType.Type intinoModuleType;
	private String groupId;
	private List<IntinoWizardPanel.Components> components;

	@Override
	public String getPresentableName() {
		return "Intino";
	}


	public Icon getBigIcon() {
		return IntinoIcons.LOGO_16;
	}

	@Override
	public Icon getNodeIcon() {
		return getBigIcon();
	}

	@Override
	public String getDescription() {
		return "Intino project";
	}

	@Override
	public ModuleType<?> getModuleType() {
		return IntinoModuleType.getModuleType();
	}

	@Override
	public ModuleWizardStep[] createWizardSteps(@NotNull WizardContext wizardContext, @NotNull ModulesProvider modulesProvider) {
		final ModuleWizardStep[] wizardSteps = super.createWizardSteps(wizardContext, modulesProvider);
		final List<ModuleWizardStep> moduleWizardSteps = new ArrayList<>(Arrays.asList(wizardSteps));
		moduleWizardSteps.add(new IntinoWizardStep(this));
		return moduleWizardSteps.toArray(new ModuleWizardStep[0]);
	}

	@Override
	public boolean isAvailable() {
		return true;
	}

	public void setupRootModel(@NotNull ModifiableRootModel rootModel) throws ConfigurationException {
		super.setupRootModel(rootModel);
		final ContentEntry contentEntry = rootModel.getContentEntries()[0];
		final File gen = new File(contentEntry.getFile().getPath(), "gen");
		final File res = new File(contentEntry.getFile().getPath(), "res");
		gen.mkdir();
		res.mkdir();
		final VirtualFile genVfile = VfsUtil.findFileByIoFile(gen, true);
		final VirtualFile resVfile = VfsUtil.findFileByIoFile(res, true);
		if (contentEntry.getFile().findChild(IntinoDirectory.INTINO) != null) {
			final VirtualFile intinoVfile = VfsUtil.findFileByIoFile(IntinoDirectory.of(rootModel.getProject()), true);
			if (intinoVfile != null) contentEntry.addExcludeFolder(intinoVfile);
		}
		if (contentEntry.getFile().findChild(".idea") != null) {
			VirtualFile baseDirectory = VfsUtil.findFileByIoFile(new File(rootModel.getProject().getBasePath()), true);
			if (baseDirectory != null) {
				final VirtualFile ideaVDirectory = baseDirectory.findChild(".idea");
				if (ideaVDirectory != null) contentEntry.addExcludeFolder(ideaVDirectory);
			}
		}
		contentEntry.addSourceFolder(genVfile, JavaSourceRootType.SOURCE, JpsJavaExtensionService.getInstance().createSourceRootProperties("", true));
		contentEntry.addSourceFolder(resVfile, JavaResourceRootType.RESOURCE, JpsJavaExtensionService.getInstance().createResourceRootProperties("", false));
		final Module module = rootModel.getModule();
		module.setOption(IntinoModuleType.INTINO_MODULE_OPTION_NAME, intinoModuleType.name());
		module.setOption(IntinoModuleType.INTINO_GROUPID_OPTION_NAME, groupId);
	}

	@Override
	public @Nullable Module commitModule(@NotNull Project project, @Nullable ModifiableModuleModel model) {
		final Module module = super.commitModule(project, model);
		createIntinoFiles(project, module);
		return module;
	}

	private void createIntinoFiles(@NotNull Project project, Module module) {
		new ModuleTemplateDeployer(module, components).deploy();
		final VirtualFile file = new LegioFileCreator(module, components).get();
		if (project.isInitialized()) FileEditorManager.getInstance(project).openFile(file, true);
		else getApplication().invokeLater(() -> FileEditorManager.getInstance(project).openFile(file, true));
		loadConfiguration(module);
	}

	private void loadConfiguration(Module module) {
		final Configuration configuration = register(module, hasExternalProviders() ? newExternalProvider(module) : new MavenConfiguration(module).init());
		configuration.reload();
	}

	@Override
	public int getWeight() {
		return 90;
	}

	public void setIntinoModuleType(IntinoModuleType.Type selected) {
		this.intinoModuleType = selected;
	}

	public void setGroupId(String selected) {
		this.groupId = selected;
	}

	public void setStartingComponents(List<IntinoWizardPanel.Components> components) {
		this.components = components;
	}
}
