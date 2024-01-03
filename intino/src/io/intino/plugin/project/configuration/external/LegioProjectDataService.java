package io.intino.plugin.project.configuration.external;

import com.intellij.openapi.externalSystem.model.DataNode;
import com.intellij.openapi.externalSystem.model.Key;
import com.intellij.openapi.externalSystem.model.ProjectKeys;
import com.intellij.openapi.externalSystem.model.ProjectSystemId;
import com.intellij.openapi.externalSystem.model.project.ProjectData;
import com.intellij.openapi.externalSystem.service.project.IdeModifiableModelsProvider;
import com.intellij.openapi.externalSystem.service.project.manage.AbstractProjectDataService;
import com.intellij.openapi.externalSystem.util.DisposeAwareProjectChange;
import com.intellij.openapi.externalSystem.util.ExternalSystemApiUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ex.ProjectEx;
import com.intellij.openapi.util.Computable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public class LegioProjectDataService extends AbstractProjectDataService<ProjectData, Project> {
	@Override
	public @NotNull Key<ProjectData> getTargetDataKey() {
		return ProjectKeys.PROJECT;
	}

	@Override
	public void importData(Collection<? extends DataNode<ProjectData>> toImport, @Nullable ProjectData projectData, @NotNull Project project, @NotNull IdeModifiableModelsProvider modelsProvider) {
		if (toImport.isEmpty()) return;
		if (toImport.size() != 1)
			throw new IllegalArgumentException(String.format("Expected to get a single project but got %d: %s", toImport.size(), toImport));
		DataNode<ProjectData> mogram = toImport.iterator().next();
		assert projectData == mogram.getData();
		if (!ExternalSystemApiUtil.isOneToOneMapping(project, mogram.getData(), modelsProvider.getModules())) return;
		if (!project.getName().equals(projectData.getInternalName()))
			renameProject(projectData.getInternalName(), projectData.getOwner(), project);
	}

	private static void renameProject(final @NotNull String newName,
									  final @NotNull ProjectSystemId externalSystemId,
									  final @NotNull Project project) {
		if (!(project instanceof ProjectEx) || newName.equals(project.getName())) return;
		//TODO Update
		ExternalSystemApiUtil.executeProjectChangeAction(true, new DisposeAwareProjectChange(project) {
			@Override
			public void execute() {
				String oldName = project.getName();
				((ProjectEx) project).setProjectName(newName);
				ExternalSystemApiUtil.getSettings(project, externalSystemId).getPublisher().onProjectRenamed(oldName, newName);
			}
		});
	}

	@Override
	public @NotNull Computable<Collection<Project>> computeOrphanData(Collection<? extends DataNode<ProjectData>> toImport, @NotNull ProjectData projectData, @NotNull Project project, @NotNull IdeModifiableModelsProvider modelsProvider) {
		return null;
	}

	@Override
	public void removeData(Computable<? extends Collection<? extends Project>> toRemove, Collection<? extends DataNode<ProjectData>> toIgnore, @NotNull ProjectData projectData, @NotNull Project project, @NotNull IdeModifiableModelsProvider modelsProvider) {

	}
}
