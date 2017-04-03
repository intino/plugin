package io.intino.plugin.settings;

import com.intellij.openapi.components.*;
import com.intellij.openapi.project.Project;
import com.intellij.util.xmlb.XmlSerializerUtil;
import com.intellij.util.xmlb.annotations.Tag;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@State(
		name = "Intino.Settings",
		storages = {
				@Storage(id = "IntinoSettings", file = "$PROJECT_FILE$"),
				@Storage(file = "$PROJECT_CONFIG_DIR$/IntinoSettings.xml", scheme = StorageScheme.DIRECTORY_BASED)
		}
)
public class IntinoSettings implements PersistentStateComponent<io.intino.plugin.settings.IntinoSettings.State> {

	private State myState = new State();
	private List<ArtifactoryCredential> artifactories = null;

	public static io.intino.plugin.settings.IntinoSettings getSafeInstance(Project project) {
		io.intino.plugin.settings.IntinoSettings settings = ServiceManager.getService(project, io.intino.plugin.settings.IntinoSettings.class);
		return settings != null ? settings : new io.intino.plugin.settings.IntinoSettings();
	}

	public void saveState() {
		new ArtifactoryCredentialsManager().saveCredentials(artifactories);
	}

	public List<ArtifactoryCredential> artifactories() {
		return artifactories == null ? artifactories = new ArtifactoryCredentialsManager().loadCredentials() : artifactories;
	}

	public void artifactories(List<ArtifactoryCredential> artifactories) {
		this.artifactories.clear();
		this.artifactories.addAll(artifactories);
	}

	public String trackerProjectId() {
		return myState.trackerProjectId;
	}

	public void trackerProjectId(String trackerProjectId) {
		myState.trackerProjectId = trackerProjectId;
	}

	public String trackerApiToken() {
		return myState.trackerApiToken;
	}

	public void trackerApiToken(String trackerApiToken) {
		myState.trackerApiToken = trackerApiToken;
	}


	public String cesarUser() {
		return myState.cesarUser;
	}

	public void cesarUser(String cesarUser) {
		myState.cesarUser = cesarUser;
	}

	public boolean overrides() {
		return myState.overrides;
	}

	public void overrides(boolean overrides) {
		myState.overrides = overrides;
	}

	@Nullable
	@Override
	public State getState() {
		return myState;
	}

	@Override
	public void loadState(State state) {
		XmlSerializerUtil.copyBean(state, myState);
	}

	public static class State {
		public boolean overrides = false;

		@Tag("trackerProjectId")
		public String trackerProjectId = "1022010";

		@Tag("trackerApiToken")
		public String trackerApiToken = "ae3d1e4d4bcb011927e2768d7aa39f3a";

		@Tag("cesarUser")
		public String cesarUser = "";
	}

}