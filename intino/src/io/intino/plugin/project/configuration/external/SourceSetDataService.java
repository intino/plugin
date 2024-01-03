package io.intino.plugin.project.configuration.external;

import com.intellij.openapi.externalSystem.model.DataNode;
import com.intellij.openapi.externalSystem.model.Key;
import com.intellij.openapi.externalSystem.model.project.ProjectData;
import com.intellij.openapi.externalSystem.service.project.IdeModifiableModelsProvider;
import com.intellij.openapi.externalSystem.service.project.manage.AbstractModuleDataService;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Computable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public class SourceSetDataService extends AbstractModuleDataService<SourceSetData> {
	@Override
	public @NotNull Key<SourceSetData> getTargetDataKey() {
		return SourceSetData.KEY;
	}

	@Override
	public void importData(@NotNull Collection<? extends DataNode<SourceSetData>> toImport, @Nullable ProjectData projectData, @NotNull Project project, @NotNull IdeModifiableModelsProvider modelsProvider) {
		super.importData(toImport, projectData, project, modelsProvider);
	}

	@Override
	public @NotNull Computable<Collection<Module>> computeOrphanData(@NotNull Collection<? extends DataNode<SourceSetData>> toImport, @NotNull ProjectData projectData, @NotNull Project project, @NotNull IdeModifiableModelsProvider modelsProvider) {
		return super.computeOrphanData(toImport, projectData, project, modelsProvider);
	}
}
