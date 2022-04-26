package io.intino.plugin.project.module;

import com.intellij.ide.util.projectWizard.JavaModuleBuilder;
import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import com.intellij.ide.util.projectWizard.WizardContext;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.roots.ContentEntry;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.ui.configuration.ModulesProvider;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import io.intino.plugin.IntinoIcons;
import io.intino.plugin.project.IntinoDirectory;
import io.intino.plugin.project.configuration.MavenConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.jps.model.java.JavaResourceRootType;
import org.jetbrains.jps.model.java.JavaSourceRootType;
import org.jetbrains.jps.model.java.JpsJavaExtensionService;

import javax.swing.*;
import java.io.File;

import static io.intino.plugin.project.configuration.ConfigurationManager.*;

public class IntinoModuleBuilder extends JavaModuleBuilder {

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
	public ModuleType getModuleType() {
		return IntinoModuleType.getModuleType();
	}

	@Override
	public ModuleWizardStep[] createWizardSteps(@NotNull WizardContext wizardContext, @NotNull ModulesProvider modulesProvider) {
		return super.createWizardSteps(wizardContext, modulesProvider);
	}

	@Override
	public boolean isAvailable() {
		return true;
	}

	public void setupRootModel(ModifiableRootModel rootModel) throws ConfigurationException {
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
		module.setOption(IntinoModuleType.INTINO_MODULE_OPTION_NAME, "true");
		register(module, hasExternalProviders() ? newExternalProvider(module) : new MavenConfiguration(module).init());
	}

	@Override
	public int getWeight() {
		return 90;
	}
}
