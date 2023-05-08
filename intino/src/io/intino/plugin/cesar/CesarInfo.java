package io.intino.plugin.cesar;

import com.google.gson.reflect.TypeToken;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import com.intellij.util.xmlb.XmlSerializerUtil;
import com.intellij.util.xmlb.annotations.Tag;
import io.intino.Configuration;
import io.intino.alexandria.Json;
import io.intino.cesar.box.schemas.ProcessInfo;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@State(
		name = "Cesar.Info",
		storages = {
				@Storage(file = "$PROJECT_FILE$"),
				@Storage(file = "$PROJECT_CONFIG_DIR$/CesarInfo.xml")
		}
)
public class CesarInfo implements PersistentStateComponent<CesarInfo.State> {
	private final State myState = new State();

	public Map<String, ServerInfo> serversInfo() {
		if (myState.serversInfo.equals("{}")) return Collections.emptyMap();
		Map<String, ServerInfo> map = Json.fromJson(myState.serversInfo, new TypeToken<Map<String, ServerInfo>>() {
		}.getType());
		return map == null ? Collections.emptyMap() : map;
	}

	public void serversInfo(Map<String, ServerInfo> infos) {
		myState.serversInfo = Json.toJson(infos);
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

	public static CesarInfo getSafeInstance(Project project) {
		CesarInfo settings = project.getService(CesarInfo.class);
		return settings != null ? settings : new CesarInfo();
	}

	public static class State {
		@Tag("serverInfo")
		public String serversInfo = "{}";
	}

	public static class ServerInfo {
		String name;
		String type;
		List<ProcessInfo> processes;

		public ServerInfo(String name, String type, List<ProcessInfo> processes) {
			this.name = name;
			this.type = type;
			this.processes = processes;
		}

		public ServerInfo() {
		}

		public String name() {
			return name;
		}

		public Configuration.Server.Type type() {
			return Configuration.Server.Type.valueOf(type);
		}

		public List<ProcessInfo> processes() {
			return processes != null ? processes : Collections.emptyList();
		}
	}
}