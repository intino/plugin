package io.intino.plugin.codeinsight.notifications;

import com.intellij.ide.highlighter.JavaClassFileType;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.EditorNotificationPanel;
import com.intellij.ui.EditorNotifications;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AttachSourcesNotificationProvider extends EditorNotifications.Provider<EditorNotificationPanel> {

	@NotNull
	private static final Key<EditorNotificationPanel> KEY = Key.create("add sources to class");

	private final Project project;
	@NotNull
	private final EditorNotifications notifications;

	public AttachSourcesNotificationProvider(Project project, @NotNull EditorNotifications notifications) {
		this.project = project;
		this.notifications = notifications;
	}

	@NotNull
	@Override
	public Key<EditorNotificationPanel> getKey() {
		return KEY;
	}

	@Nullable
	@Override
	public EditorNotificationPanel createNotificationPanel(@NotNull VirtualFile file, @NotNull FileEditor fileEditor) {
		if (!JavaClassFileType.INSTANCE.equals(file.getFileType())) return null;
		if (isAlreadyAdded(file)) return null;
		return createPanel(project);
	}

	private boolean isAlreadyAdded(VirtualFile file) {
		return false;
	}


	private EditorNotificationPanel createPanel(@NotNull final Project project) {
		final EditorNotificationPanel panel = new EditorNotificationPanel();
		panel.setText("Compiled class");
		panel.createActionLabel("Download sources", notifications::updateAllNotifications);
		return panel;
	}
}
