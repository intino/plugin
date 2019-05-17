package io.intino.plugin.toolwindows.store;

import com.intellij.openapi.diagnostic.Logger;
import com.jcabi.ssh.Shell;
import com.jcabi.ssh.Ssh;

import java.io.IOException;

public class SshConnection implements Connection {
	private static final Logger logger = Logger.getInstance(SshConnection.class);

	private final String url;

	public SshConnection(String url) {
		this.url = url;

	}

	@Override
	public File root() {
		try {
			Shell shell = new Ssh(url, 22, "username", "key...");
			String stdout = new Shell.Plain(shell).exec("echo 'Hello, world!'");
			return parse(stdout);
		} catch (IOException e) {
			logger.error(e);
			return null;
		}
	}

	private File parse(String stdout) {
		return null;
	}

}