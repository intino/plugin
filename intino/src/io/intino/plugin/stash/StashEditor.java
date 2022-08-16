package io.intino.plugin.stash;

import com.intellij.codeHighlighting.BackgroundEditorHighlighter;
import com.intellij.ide.structureView.StructureViewBuilder;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.io.FileUtilRt;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.pom.Navigatable;
import io.intino.plugin.lang.file.StashFileType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

class StashEditor implements TextEditor {
	private final VirtualFile stash;
	private StashEditorComponent myComponent;
	private VirtualFile taraVFile;


	StashEditor(Project project, VirtualFile stash) {
		this.stash = stash;
		try {
			Path path = StashToTara.createTara(stash, new File(FileUtilRt.getTempDirectory(), "__temp" + stash.getName() + ".tara"));
			taraVFile = VfsUtil.findFileByIoFile(path.toFile(), true);
			refreshFiles();
			if (taraVFile != null) myComponent = createEditorComponent(project, taraVFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void refreshFiles() {
		final Application a = ApplicationManager.getApplication();
		if (a.isWriteAccessAllowed()) VirtualFileManager.getInstance().refreshWithoutFileWatcher(false);
		else
			a.invokeAndWait(() -> a.runWriteAction(() -> VirtualFileManager.getInstance().refreshWithoutFileWatcher(false)), ModalityState.NON_MODAL);
	}

	@NotNull
	@Override
	public JComponent getComponent() {
		return myComponent;
	}

	@NotNull
	private StashEditorComponent createEditorComponent(final Project project, final VirtualFile file) {
		return new StashEditorComponent(project, file, this);
	}

	@Nullable
	@Override
	public JComponent getPreferredFocusedComponent() {
		return myComponent;
	}

	@NotNull
	@Override
	public String getName() {
		return "Stash Editor";
	}

	@NotNull
	@Override
	public FileEditorState getState(@NotNull FileEditorStateLevel level) {
		final Document document = FileDocumentManager.getInstance().getCachedDocument(stash);
		long modificationStamp = document != null ? document.getModificationStamp() : stash.getModificationStamp();
		return new StashEditorState(modificationStamp, new String[0]);
	}

	@NotNull
	@Override
	public Editor getEditor() {
		return myComponent.getEditor();
	}

	@Override
	public void setState(@NotNull FileEditorState state) {

	}

	@Override
	public boolean isModified() {
		return false;
	}

	@Override
	public boolean canNavigateTo(@NotNull Navigatable navigatable) {
		return false;
	}

	@Override
	public void navigateTo(@NotNull Navigatable navigatable) {

	}

	@Override
	public boolean isValid() {
		return taraVFile != null && FileDocumentManager.getInstance().getDocument(taraVFile) != null && stash.getFileType() == StashFileType.instance();
	}

	@Override
	public void selectNotify() {

	}

	@Override
	public void deselectNotify() {

	}

	@Override
	@Nullable
	public VirtualFile getFile() {
		return stash;
	}

	@Override
	public void addPropertyChangeListener(@NotNull PropertyChangeListener listener) {

	}

	@Override
	public void removePropertyChangeListener(@NotNull PropertyChangeListener listener) {

	}

	@Nullable
	@Override
	public BackgroundEditorHighlighter getBackgroundHighlighter() {
		return null;
	}

	@Nullable
	@Override
	public FileEditorLocation getCurrentLocation() {
		return null;
	}

	@Nullable
	@Override
	public StructureViewBuilder getStructureViewBuilder() {
		return null;
	}

	@Override
	public void dispose() {
		myComponent.dispose();
	}

	@Nullable
	@Override
	public <T> T getUserData(@NotNull Key<T> key) {
		return null;
	}

	@Override
	public <T> void putUserData(@NotNull Key<T> key, @Nullable T value) {

	}
}
