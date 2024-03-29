package io.intino.plugin.deploy;

import com.intellij.notification.NotificationGroup;
import com.intellij.notification.NotificationGroupManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.ui.MessageType;
import com.jcraft.jsch.Channel;
import io.intino.alexandria.exceptions.BadRequest;
import io.intino.alexandria.exceptions.InternalServerError;
import io.intino.alexandria.exceptions.NotFound;
import io.intino.alexandria.exceptions.Unauthorized;
import io.intino.cesar.box.ApiAccessor;
import io.intino.cesar.box.schemas.Application;
import io.intino.cesar.box.schemas.Server;
import io.intino.plugin.IntinoException;
import io.intino.plugin.IntinoIcons;
import io.intino.plugin.MessageProvider;
import io.intino.plugin.lang.psi.impl.IntinoUtil;
import io.intino.plugin.project.Safe;
import io.intino.plugin.project.configuration.ArtifactLegioConfiguration;
import io.intino.plugin.project.configuration.model.LegioArtifact;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static io.intino.plugin.settings.IntinoSettings.getInstance;

public class ArtifactManager {
	private final Module module;
	private static final int DEFAULT_PROXY_PORT = 7777;

	private static final Logger LOG = Logger.getInstance(ArtifactManager.class.getName());
	private static final List<Integer> usedProxyPorts = new ArrayList<>();

	public ArtifactManager(Module module) {
		this.module = module;
	}

	public void start() {
		try {
			final ArtifactLegioConfiguration configuration = (ArtifactLegioConfiguration) IntinoUtil.configurationOf(module);
			final Application system = getApplication(configuration);
			final int proxyPort = nextProxyPort();
			Channel channel;
			if ((channel = initTunnel(serverOf(system), proxyPort)) == null) return;
			final ProcessBuilder jconsole = new ProcessBuilder("jconsole", getConnectionParameters(proxyPort), connectionChain(system)).redirectErrorStream(true).redirectOutput(new File("./error.txt"));
			final Process process = jconsole.start();
			startCloserListener(proxyPort, channel, process);
		} catch (IOException | IntinoException e) {
			notifyError(e.getMessage());
		}
	}

	private Channel initTunnel(Server serverSchema, int proxyPort) {
		String[] options = new String[]{"Cancel", "Accept"};
		final JPanel panel = userDialog();
		int option = JOptionPane.showOptionDialog(null, panel, "Management Connection",
				JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, IntinoIcons.INTINO_16, options, options[1]);
		if (option == 1) {
			final int componentCount = panel.getComponentCount();
			String user = ((JTextField) panel.getComponent(componentCount - 2)).getText();
			String password = new String(((JPasswordField) panel.getComponent(componentCount - 1)).getPassword());
			return new SOCKSTunnel(user, password, serverSchema.remoteConnection().url(), serverSchema.remoteConnection().port(), proxyPort).connect();
		}
		return null;
	}

	private void startCloserListener(int nextPort, Channel channel, Process process) {
		new Thread(() -> {
			while (process.isAlive())
				try {
					Thread.sleep(1000);
				} catch (InterruptedException ignored) {
				}
			process.destroy();
			channel.disconnect();
			releasePort(nextPort);
		}).start();
	}

	private int nextProxyPort() {
		if (usedProxyPorts.isEmpty()) usedProxyPorts.add(DEFAULT_PROXY_PORT);
		else usedProxyPorts.add(usedProxyPorts.get(usedProxyPorts.size() - 1) + 1);
		return usedProxyPorts.get(usedProxyPorts.size() - 1);
	}

	private void releasePort(Integer port) {
		usedProxyPorts.remove(port);
	}

	private String getConnectionParameters(int localProxyPort) {
		return "-J-DsocksProxyHost=localhost -J-DsocksProxyPort=" + localProxyPort;
	}

	private String connectionChain(Application app) {
		final String container = app.container();
		try {
			return serverOf(app).ip();
		} catch (IntinoException e) {
			return null;
		}
	}

	private Server serverOf(Application app) throws IntinoException {
		final Map.Entry<String, String> cesar = getInstance(module.getProject()).cesar();
		final URL url = urlOf(Safe.safe(cesar::getKey));
		if (url == null) throw new IntinoException(MessageProvider.message("cesar.url.not.found"));
		try {
			return new ApiAccessor(url, cesar.getValue()).getServer(app.container());
		} catch (BadRequest | InternalServerError | Unauthorized | NotFound e) {
			throw new IntinoException("Impossible to request Cesar: " + e.getMessage());
		}
	}

	private Application getApplication(ArtifactLegioConfiguration configuration) throws IntinoException {
		final Map.Entry<String, String> cesar = getInstance(module.getProject()).cesar();
		final URL url = urlOf(Safe.safe(cesar::getKey));
		if (url == null) throw new IntinoException(MessageProvider.message("cesar.url.not.found"));
		try {
			LegioArtifact artifact = configuration.artifact();
			return new ApiAccessor(url, cesar.getValue()).getApplication(module.getProject().getName(), artifact.groupId() + ":" + artifact.name() + ":" + artifact.version());
		} catch (BadRequest | InternalServerError | Unauthorized | NotFound e) {
			throw new IntinoException("Impossible to request Cesar: " + e.getMessage());
		}
	}

	public static URL urlOf(String cesar) {
		try {
			return new URL(cesar.startsWith("http") ? cesar : "https://" + cesar);
		} catch (MalformedURLException e) {
			LOG.error(e.getMessage());
			return null;
		}
	}

	private void notifyError(String error) {
		NotificationGroup balloon = NotificationGroup.findRegisteredGroup("Intino");
		if (balloon == null) balloon = NotificationGroupManager.getInstance().getNotificationGroup("Intino");
		balloon.createNotification(error, MessageType.ERROR).setImportant(false).notify(null);
	}

	private JPanel userDialog() {
		JPanel panel = new JPanel();
		JLabel label = new JLabel("Enter user and password:");
		panel.add(label);
		JTextField user = new JTextField(20);
		JPasswordField pass = new JPasswordField(30);
		panel.add(user);
		panel.add(pass);
		return panel;
	}
}
