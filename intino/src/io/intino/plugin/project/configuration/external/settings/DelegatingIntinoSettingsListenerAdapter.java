package io.intino.plugin.project.configuration.external.settings;

import com.intellij.openapi.externalSystem.settings.DelegatingExternalSystemSettingsListener;
import com.intellij.openapi.externalSystem.settings.ExternalSystemSettingsListener;
import org.jetbrains.annotations.NotNull;

public class DelegatingIntinoSettingsListenerAdapter extends DelegatingExternalSystemSettingsListener<IntinoProjectSettings> implements IntinoSettingsListener {

	public DelegatingIntinoSettingsListenerAdapter(@NotNull ExternalSystemSettingsListener<IntinoProjectSettings> delegate) {
		super(delegate);
	}
}