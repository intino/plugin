package io.intino.plugin.project.configuration;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.SimpleJavaParameters;
import com.intellij.openapi.externalSystem.ExternalSystemManager;
import com.intellij.openapi.externalSystem.model.ProjectSystemId;
import com.intellij.openapi.externalSystem.service.project.ExternalSystemProjectResolver;
import com.intellij.openapi.externalSystem.task.ExternalSystemTaskManager;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.util.Function;
import org.jetbrains.annotations.NotNull;

public class LegioProjectManager implements ExternalSystemManager {
	@Override
	public @NotNull ProjectSystemId getSystemId() {
		return null;
	}

	@Override
	public @NotNull Function getSettingsProvider() {
		return null;
	}

	@Override
	public @NotNull Function getLocalSettingsProvider() {
		return null;
	}

	@Override
	public @NotNull Function getExecutionSettingsProvider() {
		return null;
	}

	@Override
	public @NotNull Class<? extends ExternalSystemProjectResolver> getProjectResolverClass() {
		return null;
	}

	@Override
	public Class<? extends ExternalSystemTaskManager> getTaskManagerClass() {
		return null;
	}

	@Override
	public @NotNull FileChooserDescriptor getExternalProjectDescriptor() {
		return null;
	}

	@Override
	public void enhanceRemoteProcessing(@NotNull SimpleJavaParameters parameters) throws ExecutionException {

	}
}
