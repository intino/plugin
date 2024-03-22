package io.intino.plugin.cesar;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import io.intino.alexandria.exceptions.BadRequest;
import io.intino.alexandria.exceptions.InternalServerError;
import io.intino.alexandria.exceptions.NotFound;
import io.intino.alexandria.exceptions.Unauthorized;
import io.intino.cesar.box.ApiAccessor;
import io.intino.cesar.box.schemas.Application;
import io.intino.cesar.box.schemas.Server;
import io.intino.plugin.IntinoException;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.function.Consumer;

import static io.intino.plugin.deploy.ArtifactManager.urlOf;
import static io.intino.plugin.settings.IntinoSettings.getInstance;

public class CesarAccessor {
	private static final Logger LOG = Logger.getInstance(CesarAccessor.class.getName());
	private final Project project;
	private ApiAccessor accessor;
	private Map.Entry<String, String> credentials;

	public CesarAccessor(Project project) {
		this.project = project;
		this.credentials = credentials();
		this.accessor = createAccessor(1500);
	}

	public CesarAccessor(Project project, int timeoutMillis) {
		this.project = project;
		this.credentials = credentials();
		this.accessor = createAccessor(timeoutMillis);
	}

	public ApiAccessor accessor() {
		return accessor;
	}

	private void checkCredentials() {
		this.credentials = credentials();
		this.accessor = createAccessor(1500);
	}

	public Server server(String server) {
		try {
			checkCredentials();
			if (accessor == null) return null;
			return accessor.getServer(server);
		} catch (BadRequest | InternalServerError | NotFound | Unauthorized e) {
			return null;
		}
	}

	public List<Server> servers() {
		try {
			checkCredentials();
			if (accessor == null) return null;
			return accessor.getServers(List.of(project.getName().split("\\.")));
		} catch (InternalServerError | Unauthorized e) {
			return Collections.emptyList();
		}
	}

	public List<Application> applications(String server) {
		try {
			checkCredentials();
			if (accessor == null) return null;
			return accessor.getJavaApplications(server);
		} catch (InternalServerError | Unauthorized e) {
			return Collections.emptyList();
		}
	}

	public Application application(String server, String id) {
		try {
			checkCredentials();
			if (accessor == null) return null;
			return accessor.getApplication(server, id);
		} catch (BadRequest | InternalServerError | NotFound | Unauthorized e) {
			return null;
		}
	}

	public String talk(String text) {
		try {
			checkCredentials();
			if (accessor == null) return null;
			return accessor.postBot(text, TimeZone.getDefault().getID()).raw();
		} catch (InternalServerError | Unauthorized e) {
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

	private ApiAccessor createAccessor(int timeoutMillis) {
		if (credentials == null) return null;
		return new ApiAccessor(urlOf(credentials.getKey().trim()), timeoutMillis, credentials.getValue());
	}

	private Map.Entry<String, String> credentials() {
		try {
			return getInstance(this.project).cesar();
		} catch (IntinoException ignored) {
		}
		return null;
	}
}
