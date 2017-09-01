package io.intino.plugin.actions;

import com.intellij.ide.DataManager;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.util.messages.MessageBus;
import io.intino.plugin.file.konos.KonosFileType;
import io.intino.plugin.toolwindows.console.IntinoTopics;
import io.intino.tara.compiler.shared.Configuration;
import io.intino.tara.plugin.lang.psi.impl.TaraUtil;
import io.intino.tara.plugin.project.module.ModuleProvider;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import static com.intellij.openapi.actionSystem.LangDataKeys.MODULE;

public class InterfaceGenerationAction extends AnAction {
	private boolean isConnected = false;
	private Set<PsiFile> pendingFiles = new HashSet<>();

	@Override
	public void actionPerformed(AnActionEvent e) {
		execute(e.getData(MODULE));
	}

	public boolean execute(Module module) {
		return !interfaceModified(module) || doExecute(module);
	}

	boolean force(Module module) {
		return doExecute(module);
	}

	private boolean doExecute(Module module) {
		final Configuration configuration = TaraUtil.configurationOf(module);
		if (configuration == null) return false;
		final String version = configuration.interfaceVersion();
		if (version == null || version.isEmpty()) return false;
		final AnAction action = ActionManager.getInstance().getAction("CreateKonosBox" + version);
		if (action == null) {
			Notifications.Bus.notify(new Notification("Tara Language", "Interface not found", "Interface version not found", NotificationType.ERROR), null);
			return false;
		} else {
			ApplicationManager.getApplication().invokeAndWait(() -> action.actionPerformed(createActionEvent()));
			pendingFiles.clear();
			return true;
		}
	}

	private boolean interfaceModified(Module module) {
		return pendingFiles.stream().anyMatch(f -> module.equals(ModuleProvider.moduleOf(f)));
	}

	private AnActionEvent createActionEvent() {
		final DataContext dataContext = DataManager.getInstance().getDataContextFromFocus().getResultSync();
		return new AnActionEvent(null, dataContext,
				ActionPlaces.UNKNOWN, new Presentation(),
				ActionManager.getInstance(), 0);
	}

	@Override
	public void update(AnActionEvent e) {
		super.update(e);
		if (!isConnected && e.getProject() != null) {
			final MessageBus messageBus = e.getProject().getMessageBus();
			final String konosExtension = KonosFileType.instance().getDefaultExtension();
			messageBus.connect().subscribe(IntinoTopics.FILE_MODIFICATION, file -> {
				final VirtualFile vFile = VfsUtil.findFileByIoFile(new File(file), true);
				if (vFile == null || !konosExtension.equalsIgnoreCase(vFile.getExtension())) return;
				pendingFiles.add(PsiManager.getInstance(e.getProject()).findFile(vFile));
			});
			isConnected = true;
		}
		e.getPresentation().setVisible(false);
	}
}
