package io.intino.plugin.project.configuration.external;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.externalSystem.model.DataNode;
import com.intellij.openapi.externalSystem.model.Key;
import com.intellij.openapi.externalSystem.model.ProjectKeys;
import com.intellij.openapi.externalSystem.model.project.LibraryData;
import com.intellij.openapi.externalSystem.model.project.LibraryPathType;
import com.intellij.openapi.externalSystem.model.project.ProjectData;
import com.intellij.openapi.externalSystem.service.project.IdeModifiableModelsProvider;
import com.intellij.openapi.externalSystem.service.project.manage.AbstractProjectDataService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.libraries.Library;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class LegioLibraryDataService extends AbstractProjectDataService<LibraryData, Library> {
	private static final Logger LOG = Logger.getInstance(LegioLibraryDataService.class);

	@Override
	public @NotNull Key<LibraryData> getTargetDataKey() {
		return ProjectKeys.LIBRARY;
	}

	@Override
	public void importData(@NotNull Collection<? extends DataNode<LibraryData>> toImport, @Nullable ProjectData projectData, @NotNull Project project, @NotNull IdeModifiableModelsProvider modelsProvider) {
		super.importData(toImport, projectData, project, modelsProvider);
		Map<String, LibraryData> processedLibraries = new HashMap<>();
		for (DataNode<LibraryData> dataNode: toImport) {
			LibraryData libraryData = dataNode.getData();
			String libraryName = libraryData.getInternalName();
			LibraryData importedLibrary = processedLibraries.putIfAbsent(libraryName, libraryData);
			if (importedLibrary != null) {
				LOG.warn("Multiple project level libraries found with the same name '" + libraryName + "'");
				if (LOG.isDebugEnabled()) {
					LOG.debug("Chosen library:" + importedLibrary.getPaths(LibraryPathType.BINARY));
					LOG.debug("Ignored library:" + libraryData.getPaths(LibraryPathType.BINARY));
				}
			}
		}
	}
}
