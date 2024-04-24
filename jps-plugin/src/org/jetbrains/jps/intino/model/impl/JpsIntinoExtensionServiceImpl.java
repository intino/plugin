package org.jetbrains.jps.intino.model.impl;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jps.incremental.CompileContext;
import org.jetbrains.jps.incremental.ProjectBuildException;
import org.jetbrains.jps.intino.model.JpsIntinoExtensionService;
import org.jetbrains.jps.model.JpsProject;
import org.jetbrains.jps.model.module.JpsModule;

public class JpsIntinoExtensionServiceImpl extends JpsIntinoExtensionService {

	@NotNull
	@Override
	public JpsModuleConfiguration getOrCreateExtension(@NotNull JpsModule module) {
		JpsModuleConfiguration extension = module.getContainer().getChild(JpsModuleConfiguration.ROLE);
		if (extension == null) {
			extension = new JpsModuleConfiguration();
			module.getContainer().setChild(JpsModuleConfiguration.ROLE, extension);
		}
		return extension;
	}

	@Nullable
	@Override
	public JpsModuleConfiguration getConfiguration(@NotNull JpsModule module, CompileContext context) {
		try {
			return new JpsConfigurationLoader(module, context).load();
		} catch (ProjectBuildException e) {
			throw new RuntimeException(e);
		}
	}

	@Nullable
	@Override
	public IntinoJpsCompilerSettings getSettings(@NotNull JpsProject project) {
		return project.getContainer().getChild(IntinoJpsCompilerSettings.ROLE);
	}
}
