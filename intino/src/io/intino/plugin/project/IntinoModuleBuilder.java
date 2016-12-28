package io.intino.plugin.project;

import com.intellij.ide.util.projectWizard.JavaModuleBuilder;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.roots.ModifiableRootModel;
import io.intino.tara.plugin.lang.TaraIcons;
import io.intino.tara.plugin.project.TaraModuleType;
import io.intino.tara.plugin.project.configuration.MavenConfiguration;

import javax.swing.*;

import static io.intino.tara.plugin.project.configuration.ConfigurationManager.*;

public class IntinoModuleBuilder extends JavaModuleBuilder {

	@Override
	public String getPresentableName() {
		return "Intino";
	}

	@Override
	public Icon getBigIcon() {
		return TaraIcons.LOGO_24;
	}

	@Override
	public Icon getNodeIcon() {
		return getBigIcon();
	}

	@Override
	public String getDescription() {
		return "Tara project";
	}

	@Override
	public ModuleType getModuleType() {
		return TaraModuleType.getModuleType();
	}

	@Override
	protected boolean isAvailable() {
		return true;
	}

	public void setupRootModel(ModifiableRootModel rootModel) throws ConfigurationException {
		super.setupRootModel(rootModel);
		final Module module = rootModel.getModule();
		module.setOption(TaraModuleType.TARA_MODULE_OPTION_NAME, "true");
		register(module, hasExternalProviders() ? newExternalProvider(module) : new MavenConfiguration(module).init());
	}

	@Override
	public int getWeight() {
		return 90;
	}
}
