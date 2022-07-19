package io.intino.plugin.deploy;

import com.jcraft.jsch.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class SOCKSTunnel {
	private static final com.intellij.openapi.diagnostic.Logger LOG = com.intellij.openapi.diagnostic.Logger.getInstance(ArtifactManager.class.getName());
	private static final File SSH_DIRECTORY = new File(System.getProperty("user.home") + File.separator + ".ssh");
	private final String user;
	private final String password;
	private final String server;
	private final int serverPort;
	private final int proxyPort;

	public SOCKSTunnel(String user, String password, String server, int serverPort, int proxyPort) {
		this.user = user;
		this.password = password;
		this.server = server;
		this.serverPort = serverPort;
		this.proxyPort = proxyPort;
		JSch.setLogger(new Logger() {
			@Override
			public boolean isEnabled(int level) {
				return true;
			}

			@Override
			public void log(int level, String message) {
				System.out.println(message);
			}
		});
	}

	public Channel connect() {
		try {
			JSch jsch = new JSch();
			jsch.addIdentity(new File(SSH_DIRECTORY, "id_rsa").getAbsolutePath(), password);
			Session session = jsch.getSession(user, server, serverPort);
			session.setPassword(password);
			session.setPortForwardingR(proxyPort, Parrot.class.getName());
			session.setConfig("StrictHostKeyChecking", "no");
			session.setUserInfo(new MyUserInfo());
			session.connect();
			Channel channel = session.openChannel("direct-tcpip");
			channel.connect();
			return channel;
		} catch (Exception e) {
			LOG.error(e);
		}
		return null;
	}

	public static class MyUserInfo
			implements UserInfo, UIKeyboardInteractive {
		public String getPassword() {
			return null;
		}

		public boolean promptYesNo(String str) {
			return false;
		}

		public String getPassphrase() {
			return null;
		}

		public boolean promptPassphrase(String message) {
			return false;
		}

		public boolean promptPassword(String message) {
			return false;
		}

		public void showMessage(String message) {
		}

		public String[] promptKeyboardInteractive(String destination,
												  String name,
												  String instruction,
												  String[] prompt,
												  boolean[] echo) {
			return null;
		}
	}

	public static class Parrot implements ForwardedTCPIPDaemon {
		ChannelForwardedTCPIP channel;
		Object[] arg;
		InputStream in;
		OutputStream out;

		public void setChannel(ChannelForwardedTCPIP c, InputStream in, OutputStream out) {
			this.channel = c;
			this.in = in;
			this.out = out;
		}

		public void setArg(Object[] arg) {
			this.arg = arg;
		}

		public void run() {
			try {
				byte[] buf = new byte[1024];
				System.out.println("remote port: " + channel.getRemotePort());
				System.out.println("remote host: " + channel.getSession().getHost());
				while (true) {
					int i = in.read(buf, 0, buf.length);
					if (i <= 0) break;
					out.write(buf, 0, i);
					out.flush();
					if (buf[0] == '.') break;
				}
			} catch (JSchException e) {
				System.out.println("session is down.");
			} catch (IOException e) {
			}
		}
	}

}
