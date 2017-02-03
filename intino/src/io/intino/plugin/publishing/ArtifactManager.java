package io.intino.plugin.publishing;

import com.intellij.notification.NotificationGroup;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.ui.MessageType;
import io.intino.cesar.RestCesarAccessor;
import io.intino.cesar.schemas.Runtime;
import io.intino.cesar.schemas.SystemSchema;
import io.intino.legio.LifeCycle;
import io.intino.konos.exceptions.BadRequest;
import io.intino.konos.exceptions.Unknown;
import io.intino.plugin.IntinoException;
import io.intino.plugin.MessageProvider;
import io.intino.plugin.project.LegioConfiguration;
import io.intino.tara.plugin.lang.psi.impl.TaraUtil;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

public class ArtifactManager {
	private Module module;
	private static final Logger LOG = Logger.getInstance(ArtifactManager.class.getName());

	public ArtifactManager(Module module) {
		this.module = module;
	}

	public void start() {
		try {
			final LegioConfiguration configuration = (LegioConfiguration) TaraUtil.configurationOf(module);
			if (configuration.lifeCycle().publishing().managementPort() == 0)
				throw new IntinoException("Management port not found. Define it");
			final ProcessBuilder jconsole = new ProcessBuilder("jconsole", findServer(configuration));
			jconsole.redirectErrorStream(true);
			jconsole.redirectOutput(new File("./error.txt"));
			jconsole.start();
		} catch (IOException | IntinoException e) {
			notifyError(e.getMessage());
		}
	}

	private String findServer(LegioConfiguration configuration) throws IntinoException {
		final URL url = urlOf(configuration.lifeCycle().publishing());
		if (url == null) throw new IntinoException(MessageProvider.message("cesar.url.not.found"));
		final RestCesarAccessor cesar = new RestCesarAccessor(url);
		try {
			final SystemSchema system = cesar.getSystem(configuration.groupId() + ":" + configuration.artifactId() + ":" + configuration.version());
			final Runtime runtime = system.runtime();
			return runtime.server() + ":" + runtime.jmxPort();
		} catch (BadRequest | Unknown e) {
			throw new IntinoException("Impossible to request Cesar: " + e.getMessage());
		}
	}

	static URL urlOf(LifeCycle.Publishing publishing) {
		try {
			final String direction = publishing.cesarURL();
			return new URL(direction.startsWith("http") ? direction : "http://" + direction);
		} catch (MalformedURLException e) {
			LOG.error(e.getMessage());
			return null;
		}
	}

	private void notifyError(String error) {
		NotificationGroup balloon = NotificationGroup.findRegisteredGroup("Tara Language");
		if (balloon == null) balloon = NotificationGroup.balloonGroup("Tara Language");
		balloon.createNotification(error, MessageType.ERROR).setImportant(false).notify(null);
	}
}
