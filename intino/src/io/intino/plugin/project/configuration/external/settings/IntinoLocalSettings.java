package io.intino.plugin.project.configuration.external.settings;

import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.components.StoragePathMacros;
import com.intellij.openapi.externalSystem.settings.AbstractExternalSystemLocalSettings;
import com.intellij.openapi.project.Project;
import io.intino.plugin.project.configuration.external.IntinoExternalSystemManager;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

@State(name = "IntinoLocalSettings", storages = @Storage(StoragePathMacros.CACHE_FILE))
public class IntinoLocalSettings extends AbstractExternalSystemLocalSettings<IntinoLocalSettings.MyState> {

	public IntinoLocalSettings(@NotNull Project project) {
		super(IntinoExternalSystemManager.SYSTEM_ID, project, new MyState());
	}

	@NotNull
	public static IntinoLocalSettings getInstance(@NotNull Project project) {
		return project.getService(IntinoLocalSettings.class);
	}

	public static class MyState extends State {
		public String userHome;
		public Map<String, String> myMavenHomes;
	}
}
