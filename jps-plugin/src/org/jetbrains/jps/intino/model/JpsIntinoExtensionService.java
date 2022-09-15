package org.jetbrains.jps.intino.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jps.incremental.CompileContext;
import org.jetbrains.jps.intino.model.impl.JpsModuleConfiguration;
import org.jetbrains.jps.intino.model.impl.IntinoJpsCompilerSettings;
import org.jetbrains.jps.model.JpsProject;
import org.jetbrains.jps.model.module.JpsModule;
import org.jetbrains.jps.service.JpsServiceManager;

public abstract class JpsIntinoExtensionService {

	public static JpsIntinoExtensionService instance() {
		return JpsServiceManager.getInstance().getService(JpsIntinoExtensionService.class);
	}


	@NotNull
	public abstract JpsModuleConfiguration getOrCreateExtension(@NotNull JpsModule module);

	@Nullable
	public abstract JpsModuleConfiguration getConfiguration(@NotNull JpsModule module, CompileContext context);

	public abstract IntinoJpsCompilerSettings getSettings(@NotNull JpsProject project);
}
