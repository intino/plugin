package io.intino.plugin.toolwindows.remote;

import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.remote.AuthType;
import com.intellij.remote.RemoteCredentials;
import com.intellij.remote.RemoteSdkException;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.terminal.TerminalView;

import java.nio.charset.Charset;

public class TerminalManager {
	public static void runSshConsole(Project project) {
		if (project != null) {
			UIUtil.invokeLaterIfNeeded(() -> connectToSshUnderProgress(project, new TerminalCachingRunner(project, credentials(), Charset.defaultCharset())));
		}
	}

	private static void connectToSshUnderProgress(final Project project, final TerminalCachingRunner runner) {
		ProgressManager.getInstance().run(new Task.Backgroundable(project, "Connecting to " + runner.runningTargetName(), true) {
			public void run(@NotNull ProgressIndicator indicator) {
				createTerminalSession(indicator, runner, project, this.getTitle());
			}
		});
	}

	private static void createTerminalSession(@NotNull ProgressIndicator indicator, TerminalCachingRunner runner, Project project, String title) {
		try {
			runner.connect();
		} catch (RemoteSdkException e) {
			if (!indicator.isCanceled())
				UIUtil.invokeLaterIfNeeded(() -> Messages.showErrorDialog(project, e.getMessage(), title));
		}
		UIUtil.invokeLaterIfNeeded(() -> {
			TerminalView.getInstance(project).createNewSession(project, runner);
		});
	}

	@NotNull
	private static RemoteCredentials credentials() {
		return new RemoteCredentials() {
			@Override
			public String getHost() {
				return "vpn.bridge.monentia.es";
			}

			@Override
			public int getPort() {
				return 4044;
			}

			@Override
			public String getLiteralPort() {
				return "4044";
			}

			@Override
			public String getUserName() {
				return "root";
			}

			@Override
			public String getPassword() {
				return null;
			}

			@Override
			public String getPassphrase() {
				return null;
			}

			@NotNull
			@Override
			public AuthType getAuthType() {
				return AuthType.OPEN_SSH;
			}

			@Override
			public boolean isUseKeyPair() {
				return false;
			}

			@Override
			public boolean isUseAuthAgent() {
				return true;
			}

			@Override
			public String getPrivateKeyFile() {
				return null;
			}

			@Override
			public boolean isStorePassword() {
				return false;
			}

			@Override
			public boolean isStorePassphrase() {
				return false;
			}
		};
	}
}
