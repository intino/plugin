package io.intino.plugin.cesar;

import com.google.gson.reflect.TypeToken;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
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
import java.util.LinkedHashMap;
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

	public static CesarInfo getSafeInstance(Project project) {
		CesarInfo settings = ServiceManager.getService(project, CesarInfo.class);
		return settings != null ? settings : new CesarInfo();
	}


	public Map<String, ServerInfo> serversInfo() {
		Map<String, ServerInfo> map = Json.gsonReader().fromJson(myState.serversInfo, new TypeToken<Map<String, ServerInfo>>() {
		}.getType());
		return map == null ? Collections.emptyMap() : map;
	}

	public void serversInfo(Map<String, ServerInfo> infos) {
		Map<String, ServerInfo> current = new LinkedHashMap<>(serversInfo());
		current.putAll(infos);
		myState.serversInfo = Json.gsonReader().toJson(current);
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
		@Tag("serverInfo")
		public String serversInfo = "{}";
	}

	public static class ServerInfo {
		String name;
		String type;
		List<ProcessInfo> processses;

		public ServerInfo(String name, String type, List<ProcessInfo> processses) {
			this.name = name;
			this.type = type;
			this.processses = processses;
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
			return processses;
		}
	}
}