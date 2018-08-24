package io.intino.plugin.toolwindows.remote;

import com.intellij.openapi.project.Project;
import com.intellij.remote.RemoteCredentials;
import com.intellij.remote.RemoteSdkException;
import com.intellij.ssh.channels.ShellChannel;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.Charset;

public class TerminalCachingRunner extends TerminalRunner {
	private volatile ShellChannel myChannel = null;
	private final Object myChannelLock = new Object();


	public TerminalCachingRunner(Project project, RemoteCredentials credentials, Charset charset) {
		super(project, credentials, charset);
	}

	public void connect() throws RemoteSdkException {
		this.getShellChannel();
	}

	@NotNull
	protected ShellChannel getShellChannel() throws RemoteSdkException {
		Object var1 = this.myChannelLock;
		ShellChannel channel;
		synchronized(this.myChannelLock) {
			ShellChannel myChannel = this.myChannel;
			if (myChannel == null) {
				myChannel = this.createShellChannel();
				this.myChannel = myChannel;
			}
			channel = myChannel;
		}
		return channel;
	}
}