package io.intino.plugin.project.configuration;

import com.intellij.openapi.externalSystem.model.Key;
import com.intellij.openapi.externalSystem.model.ProjectKeys;
import com.intellij.openapi.externalSystem.model.project.ModuleData;
import com.intellij.openapi.externalSystem.service.project.manage.AbstractModuleDataService;
import org.jetbrains.annotations.NotNull;

public class LegioModuleDataService extends AbstractModuleDataService<ModuleData> {

	@Override
	public @NotNull Key<ModuleData> getTargetDataKey() {
		return ProjectKeys.MODULE;
	}

}
