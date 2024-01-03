package io.intino.plugin.project.configuration.external.settings;

import com.intellij.openapi.externalSystem.settings.ExternalSystemSettingsListener;
import com.intellij.util.messages.Topic;

public interface IntinoSettingsListener extends ExternalSystemSettingsListener<IntinoProjectSettings> {

	Topic<IntinoSettingsListener> TOPIC = new Topic<>(IntinoSettingsListener.class, Topic.BroadcastDirection.NONE);

}
