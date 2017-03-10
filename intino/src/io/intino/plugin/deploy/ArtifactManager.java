package io.intino.plugin.deploy;

import com.intellij.notification.NotificationGroup;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.ui.MessageType;
import com.jcraft.jsch.Channel;
import io.intino.cesar.RestCesarAccessor;
import io.intino.cesar.schemas.Runtime;
import io.intino.cesar.schemas.ServerSchema;
import io.intino.cesar.schemas.SystemSchema;
import io.intino.konos.exceptions.BadRequest;
import io.intino.konos.exceptions.Unknown;
import io.intino.legio.LifeCycle;
import io.intino.plugin.IntinoException;
import io.intino.plugin.IntinoIcons;
import io.intino.plugin.MessageProvider;
import io.intino.plugin.project.LegioConfiguration;
import io.intino.tara.plugin.lang.psi.impl.TaraUtil;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class ArtifactManager {
	private Module module;
	private static final int DEFAULT_PROXY_PORT = 7777;

	private static final Logger LOG = Logger.getInstance(ArtifactManager.class.getName());
	private static final List<Integer> usedProxyPorts = new ArrayList<>();

	public ArtifactManager(Module module) {
		this.module = module;
	}

	public void start() {
		try {
			final LegioConfiguration configuration = (LegioConfiguration) TaraUtil.configurationOf(module);
			final SystemSchema system = getSystem(configuration);
			final int proxyPort = nextProxyPort();
			Channel channel;
			if ((channel = initTunnel(serverOf(system, configuration), proxyPort)) == null) return;
			final ProcessBuilder jconsole = new ProcessBuilder("jconsole", getConnectionParameters(proxyPort), connectionChain(system)).redirectErrorStream(true).redirectOutput(new File("./error.txt"));
			final Process process = jconsole.start();
			startCloserListener(proxyPort, channel, process);
		} catch (IOException | IntinoException e) {
			notifyError(e.getMessage());
		}
	}

	private Channel initTunnel(ServerSchema serverSchema, int proxyPort) {
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

	private boolean releasePort(Integer port) {
		return usedProxyPorts.remove(port);
	}

	private String getConnectionParameters(int localProxyPort) {
		return "-J-DsocksProxyHost=localhost -J-DsocksProxyPort=" + localProxyPort;
	}

	private String connectionChain(SystemSchema system) throws IntinoException {
		final Runtime runtime = system.runtime();
		return runtime.ip();
	}

	private ServerSchema serverOf(SystemSchema system, LegioConfiguration configuration) throws IntinoException {
		final URL url = urlOf(configuration.lifeCycle().deploy());
		if (url == null) throw new IntinoException(MessageProvider.message("cesar.url.not.found"));
		try {
			return new RestCesarAccessor(url).getServer(system.runtime().serverName());
		} catch (BadRequest | Unknown e) {
			throw new IntinoException("Impossible to request Cesar: " + e.getMessage());
		}
	}

	private SystemSchema getSystem(LegioConfiguration configuration) throws IntinoException {
		final URL url = urlOf(configuration.lifeCycle().deploy());
		if (url == null) throw new IntinoException(MessageProvider.message("cesar.url.not.found"));
		try {
			return new RestCesarAccessor(url).getSystem(configuration.groupId() + ":" + configuration.artifactId() + ":" + configuration.version());
		} catch (BadRequest | Unknown e) {
			throw new IntinoException("Impossible to request Cesar: " + e.getMessage());
		}
	}

	static URL urlOf(LifeCycle.Deploy publishing) {
		try {
			final String direction = publishing.cesarURL();
			return new URL(direction.startsWith("http") ? direction : "https://" + direction);
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
