package io.intino.plugin.cesar;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import io.intino.alexandria.exceptions.BadRequest;
import io.intino.alexandria.exceptions.InternalServerError;
import io.intino.cesar.box.ApiAccessor;
import io.intino.cesar.box.schemas.ProcessInfo;
import io.intino.cesar.box.schemas.ProcessStatus;
import io.intino.cesar.box.schemas.ServerInfo;
import io.intino.plugin.IntinoException;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.function.Consumer;

import static io.intino.plugin.deploy.ArtifactManager.urlOf;
import static io.intino.plugin.settings.IntinoSettings.getSafeInstance;

public class CesarAccessor {
	private static final Logger LOG = Logger.getInstance(CesarAccessor.class.getName());

	private final Project project;
	private ApiAccessor accessor;
	private Map.Entry<String, String> credentials;

	public CesarAccessor(Project project) {
		this.project = project;
		this.credentials = credentials();
		this.accessor = createAccessor();
	}

	public ApiAccessor accessor() {
		return accessor;
	}

	public ProcessInfo processInfo(String server, String id) {
		try {
			checkCredentials();
			if (accessor == null) return null;
			return accessor.getProcess(server, id);
		} catch (BadRequest | InternalServerError e) {
			return null;
		}
	}

	private void checkCredentials() {
		this.credentials = credentials();
		this.accessor = createAccessor();
	}

	public ProcessStatus processStatus(String server, String id) {
		try {
			checkCredentials();
			if (accessor == null) return null;
			return accessor.getProcessStatus(server, id);
		} catch (BadRequest | InternalServerError e) {
			return null;
		}
	}

	public ServerInfo server(String server) {
		try {
			checkCredentials();
			if (accessor == null) return null;
			return accessor.getServer(server);
		} catch (BadRequest | InternalServerError e) {
			return null;
		}
	}

	public List<ProcessInfo> processes(String server) {
		try {
			checkCredentials();
			if (accessor == null) return null;
			return accessor.getProcesses(server);
		} catch (InternalServerError e) {
			return Collections.emptyList();
		}
	}

	public String talk(String text) {
		try {
			checkCredentials();
			if (accessor == null) return null;
			return accessor.postBot(text, TimeZone.getDefault().getID());
		} catch (InternalServerError e) {
			return "Error executing command: " + e.getMessage();
		}
	}

	public void subscribeToNotifications(Consumer<String> consumer) {
		try {
			ClassLoader currentClassLoader = Thread.currentThread().getContextClassLoader();
			Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
			if (accessor != null) accessor.listenBotNotifications(credentials.getValue(), consumer);
			Thread.currentThread().setContextClassLoader(currentClassLoader);
		} catch (Exception | InternalServerError e) {
			LOG.info(e);
			accessor.stopListenBotNotifications();
		}
	}

	public void disconnect() {
		if (accessor != null) {
			accessor.stopListenLog();
			accessor.stopListenBotNotifications();
		}
	}

	private ApiAccessor createAccessor() {
		if (credentials == null) return null;
		return new ApiAccessor(urlOf(credentials.getKey().trim()), 10000, credentials.getValue());
	}

	private Map.Entry<String, String> credentials() {
		try {
			return getSafeInstance(this.project).cesar();
		} catch (IntinoException e) {
		}
		return null;
	}
}
