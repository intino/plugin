package io.intino.plugin.toolwindows.remote;


import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.remote.RemoteCredentials;
import com.intellij.remote.RemoteSdkException;
import com.intellij.ssh.RemoteCredentialsUtil;
import com.intellij.ssh.SshSession;
import com.intellij.ssh.SshTransportException;
import com.intellij.ssh.channels.ShellChannel;
import com.intellij.ssh.process.SshShellProcess;
import com.jediterm.terminal.TtyConnector;
import com.jetbrains.plugins.remotesdk.console.JschProcessTtyConnector;
import com.jetbrains.plugins.remotesdk.console.SshConsoleProcessHandler;
import io.intino.plugin.project.CesarReloader;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.terminal.AbstractTerminalRunner;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.concurrent.ExecutionException;

public class TerminalRunner extends AbstractTerminalRunner<SshShellProcess> {
	private static final Logger LOG = Logger.getInstance(CesarReloader.class.getName());

	private final RemoteCredentials myCredentials;
	private final Charset myDefaultCharset;

	public TerminalRunner(Project project, RemoteCredentials credentials, Charset charset) {
		super(project);
		this.myCredentials = credentials;
		this.myDefaultCharset = charset;
	}

	protected SshShellProcess createProcess(@Nullable String directory) throws ExecutionException {
		try {
			ShellChannel channel = this.getShellChannel();
			return new SshShellProcess(channel);
		} catch (Exception var3) {
			throw new ExecutionException(var3.getMessage(), var3);
		}
	}

	@NotNull
	protected ShellChannel getShellChannel() throws RemoteSdkException {
		return this.createShellChannel();
	}

	@NotNull
	protected final ShellChannel createShellChannel() throws RemoteSdkException {
		ShellChannel channel;
		channel = RemoteCredentialsUtil.connectionBuilder(this.myCredentials, this.myProject).shellBuilder().withAllocatePty(true).openChannel();
		try {
			channel.getOutputStream().write("tail /var/log/cesar.log -f\n".getBytes());
			channel.getOutputStream().flush();
		} catch (SshTransportException var2) {
			throw new RemoteSdkException(var2.getMessage(), var2);
		} catch (IOException e) {
			e.printStackTrace();
			return channel;
		}
		return channel;
	}

	protected SshConsoleProcessHandler createProcessHandler(SshShellProcess process) {
		return new SshConsoleProcessHandler(process);
	}

	protected TtyConnector createTtyConnector(SshShellProcess process) {
		return new JschProcessTtyConnector(process, this.myDefaultCharset);
	}

	public String runningTargetName() {
		return this.myCredentials.getHost() + ":" + this.myCredentials.getLiteralPort();
	}

	protected String getTerminalConnectionName(SshShellProcess process) {
		SshSession session = process.getSession();
		return session.getUserName() + "@" + session.getHost() + ":" + session.getPort();
	}
}