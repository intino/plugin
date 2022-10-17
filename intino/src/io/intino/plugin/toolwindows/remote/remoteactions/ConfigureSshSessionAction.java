package io.intino.plugin.toolwindows.remote.remoteactions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAware;
import io.intino.cesar.box.schemas.ProcessInfo;
import io.intino.cesar.box.schemas.ProcessStatus;
import io.intino.plugin.TerminalWindow;
import io.intino.plugin.cesar.CesarAccessor;
import io.intino.plugin.cesar.CesarInfo;
import io.intino.plugin.settings.IntinoSettings;
import io.intino.plugin.toolwindows.remote.IntinoConsoleAction;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

public class ConfigureSshSessionAction extends AnAction implements DumbAware, IntinoConsoleAction {
	private final CesarInfo.ServerInfo server;
	private final CesarAccessor cesar;

	public ConfigureSshSessionAction(CesarInfo.ServerInfo server, CesarAccessor cesarAccessor) {
		this.server = server;
		this.cesar = cesarAccessor;
	}

	@Override
	public void actionPerformed(@NotNull AnActionEvent e) {
		IntinoSettings settings = IntinoSettings.getInstance(e.getProject());
		Map<String, List<TerminalWindow.Tunnel>> savedTunnels = settings.tunnels();
		SshSessionConfiguration dialog = new SshSessionConfiguration(server, savedTunnels);
		dialog.pack();
		dialog.setTitle("Put user and password");
		dialog.setLocationRelativeTo(dialog.getParent());
		dialog.setVisible(true);
		savedTunnels.put(server.name(), dialog.tunnels());
		settings.tunnels(savedTunnels);
		settings.saveState();
	}

	@Override
	public void onChanging() {

	}

	@Override
	public void onProcessChange(ProcessInfo newProcess, ProcessStatus newProcessStatus) {

	}
}
