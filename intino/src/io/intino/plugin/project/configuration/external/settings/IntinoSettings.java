package io.intino.plugin.project.configuration.external.settings;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.externalSystem.settings.AbstractExternalSystemSettings;
import com.intellij.openapi.externalSystem.settings.ExternalSystemSettingsListener;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

@State(name = "IntinoSettings", storages = @Storage("intino.xml"))
public class IntinoSettings extends AbstractExternalSystemSettings<IntinoSettings, IntinoProjectSettings, IntinoSettingsListener> implements PersistentStateComponent<IntinoSettings.MyState> {

	protected IntinoSettings( @NotNull Project project) {
		super(IntinoSettingsListener.TOPIC, project);
	}

	@NotNull
	public static IntinoSettings getInstance(@NotNull Project project) {
		return project.getService(IntinoSettings.class);
	}

	@Override
	protected void copyExtraSettingsFrom(@NotNull IntinoSettings settings) {

	}

	@Override
	protected void checkSettings(@NotNull IntinoProjectSettings old, @NotNull IntinoProjectSettings current) {

	}

	@Override
	public void subscribe(@NotNull ExternalSystemSettingsListener<IntinoProjectSettings> listener, @NotNull Disposable parentDisposable) {
		doSubscribe(new DelegatingIntinoSettingsListenerAdapter(listener), parentDisposable);
	}

	@Override
	public @Nullable MyState getState() {
		return null;
	}

	@Override
	public void loadState(@NotNull MyState state) {

	}

	public static class MyState implements State<IntinoProjectSettings> {
		@Override
		public Set<IntinoProjectSettings> getLinkedExternalProjectsSettings() {
			return null;
		}

		@Override
		public void setLinkedExternalProjectsSettings(Set<IntinoProjectSettings> settings) {

		}

	}
}
