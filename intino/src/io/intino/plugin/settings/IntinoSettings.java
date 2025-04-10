package io.intino.plugin.settings;

import com.google.common.reflect.TypeToken;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import com.intellij.util.xmlb.XmlSerializerUtil;
import com.intellij.util.xmlb.annotations.Tag;
import io.intino.alexandria.Json;
import io.intino.plugin.IntinoException;
import io.intino.plugin.TerminalWindow.Tunnel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@State(
		name = "Intino.Settings",
		storages = {
				@Storage(value = "$PROJECT_FILE$"),
				@Storage(value = "$PROJECT_CONFIG_DIR$/IntinoSettings.xml")
		}
)
public class IntinoSettings implements PersistentStateComponent<IntinoSettings.State>, Disposable {
	private final State myState = new State();
	private List<ArtifactoryCredential> artifactories = null;

	@Override
	public void dispose() {

	}

	public static IntinoSettings getInstance(Project project) {
		IntinoSettings settings = project.getService(IntinoSettings.class);
		return settings != null ? settings : new IntinoSettings();
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

	public void cesarToken(String cesarToken) {
		myState.cesarToken = cesarToken;
	}

	public String bitbucketToken() {
		return myState.bitbucketToken;
	}

	public void bitbucketToken(String bitbucketToken) {
		myState.bitbucketToken = bitbucketToken;
	}

	public String cesarUrl() {
		return myState.cesarUrl;
	}

	public void cesarUrl(String url) {
		myState.cesarUrl = url;
	}

	public void modelMemory(int memory) {
		myState.modelMemory = memory;
	}

	public int modelMemory() {
		return myState.modelMemory;
	}

	public Map<String, List<Tunnel>> tunnels() {
		return Json.fromJson(myState.tunnels, new TypeToken<Map<String, ArrayList<Tunnel>>>() {
		}.getType());
	}

	public void tunnels(Map<String, List<Tunnel>> tunnels) {
		myState.tunnels = Json.toJson(tunnels);
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

		@Tag("bitbucketToken")
		public String bitbucketToken = "";

		@Tag("modelMemory")
		public int modelMemory = 1024;

		@Tag("boxMemory")
		public int boxMemory = 2048;

		@Tag("tunnels")
		public String tunnels = "";
	}
}