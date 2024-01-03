package io.intino.plugin.project.configuration.external;

import com.intellij.execution.configurations.SimpleJavaParameters;
import com.intellij.openapi.externalSystem.ExternalSystemAutoImportAware;
import com.intellij.openapi.externalSystem.ExternalSystemManager;
import com.intellij.openapi.externalSystem.model.ProjectSystemId;
import com.intellij.openapi.externalSystem.service.project.ExternalSystemProjectResolver;
import com.intellij.openapi.externalSystem.task.ExternalSystemTaskManager;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import com.intellij.util.Function;
import io.intino.plugin.project.configuration.external.settings.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class IntinoExternalSystemManager implements
		ExternalSystemAutoImportAware,
		com.intellij.openapi.startup.StartupActivity,
		ExternalSystemManager<IntinoProjectSettings, IntinoSettingsListener, IntinoSettings, IntinoLocalSettings, IntinoExecutionSettings> {

	public static final ProjectSystemId SYSTEM_ID = new ProjectSystemId("Intino".toUpperCase(), "Intino");


	@Override
	public @Nullable String getAffectedExternalProjectPath(@NotNull String changedFileOrDirPath, @NotNull Project project) {
		return null;
	}

	@Override
	public void runActivity(@NotNull Project project) {

	}

	@Override
	public @NotNull ProjectSystemId getSystemId() {
		return SYSTEM_ID;
	}

	@Override
	public @NotNull Function<Project, IntinoSettings> getSettingsProvider() {
		return IntinoSettings::getInstance;
	}

	@Override
	public @NotNull Function<Project, IntinoLocalSettings> getLocalSettingsProvider() {
		return IntinoLocalSettings::getInstance;
	}

	@Override
	public @NotNull Function<Pair<Project, String>, IntinoExecutionSettings> getExecutionSettingsProvider() {
		return pair -> {
			Project project = pair.first;
			String projectPath = pair.second;
			IntinoSettings settings = IntinoSettings.getInstance(project);
			IntinoProjectSettings projectSettings = settings.getLinkedProjectSettings(projectPath);
			return null;
		};
	}

	@Override
	public @NotNull Class<? extends ExternalSystemProjectResolver<IntinoExecutionSettings>> getProjectResolverClass() {
		return null;
	}

	@Override
	public Class<? extends ExternalSystemTaskManager<IntinoExecutionSettings>> getTaskManagerClass() {
		return null;
	}

	@Override
	public @NotNull FileChooserDescriptor getExternalProjectDescriptor() {
		System.out.println("externalDescriptor");
		return new IntinoLegioFileChooserDescriptor();
	}

	@Override
	public void enhanceRemoteProcessing(@NotNull SimpleJavaParameters parameters) {
		throw new UnsupportedOperationException();
	}
}
