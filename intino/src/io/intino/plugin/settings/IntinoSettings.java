package io.intino.plugin.settings;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import com.intellij.util.xmlb.XmlSerializerUtil;
import com.intellij.util.xmlb.annotations.Tag;
import io.intino.plugin.IntinoException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@State(
		name = "Intino.Settings",
		storages = {
				@Storage(file = "$PROJECT_FILE$"),
				@Storage(file = "$PROJECT_CONFIG_DIR$/IntinoSettings.xml")
		}
)
public class IntinoSettings implements PersistentStateComponent<io.intino.plugin.settings.IntinoSettings.State> {

	private final State myState = new State();
	private List<ArtifactoryCredential> artifactories = null;

	public static io.intino.plugin.settings.IntinoSettings getSafeInstance(Project project) {
		io.intino.plugin.settings.IntinoSettings settings = ServiceManager.getService(project, io.intino.plugin.settings.IntinoSettings.class);
		return settings != null ? settings : new io.intino.plugin.settings.IntinoSettings();
	}

	public void saveState() {
		new ArtifactoryCredentialsManager().saveCredentials(artifactories);
	}

	public List<ArtifactoryCredential> artifactories() {
		return artifactories == null ? artifactories = new ArtifactoryCredentialsManager().loadCredentials().stream().
				filter(c -> !c.serverId.endsWith("-snapshot")).collect(Collectors.toList()) : artifactories;
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

	public String cesarToken() {
		return myState.cesarToken;
	}

	public void cesarToken(String cesarUser) {
		myState.cesarToken = cesarUser;
	}

	public String cesarUrl() {
		return myState.cesarUrl;
	}

	public void cesarUrl(String url) {
		myState.cesarUrl = url;
	}

	@NotNull
	public Map.Entry<String, String> cesar() throws IntinoException {
		final String cesar = cesarUrl();
		final String token = cesarToken();
		if (cesar.isEmpty() || token.isEmpty())
			throw new IntinoException("Cesar credentials not found, please specify it in Intino settings");
		return new AbstractMap.SimpleEntry<>(cesar, token);
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
		@Tag("trackerProjectId")
		public String trackerProjectId = "1022010";

		@Tag("trackerApiToken")
		public String trackerApiToken = "ae3d1e4d4bcb011927e2768d7aa39f3a";

		@Tag("cesarUrl")
		public String cesarUrl = "";

		@Tag("cesarToken")
		public String cesarToken = "";
	}
}