package io.intino.plugin.stash;

import com.intellij.codeHighlighting.BackgroundEditorHighlighter;
import com.intellij.ide.structureView.StructureViewBuilder;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ApplicationNamesInfo;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileEditor.*;
import com.intellij.openapi.fileEditor.impl.text.TextEditorState;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.UserDataHolderBase;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.components.JBPanel;
import com.intellij.util.ArrayUtil;
import io.intino.plugin.lang.file.StashFileType;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

public class StashEditorProvider implements FileEditorProvider, DumbAware {
	private static final Logger LOG = Logger.getInstance("#StashEditorProvider");

	public boolean accept(@NotNull final Project project, @NotNull final VirtualFile file) {
		return file.getFileType() == StashFileType.instance() && StashFileType.instance().isBinary();
	}

	@NotNull
	public FileEditor createEditor(@NotNull final Project project, @NotNull final VirtualFile file) {
		LOG.assertTrue(accept(project, file));
		return isTooLargeForContentLoading(file) ?
				new LargeFileEditor(file) :
				new StashEditor(project, file);
	}

	private boolean isTooLargeForContentLoading(VirtualFile file) {
		return file.getLength() > (long) (1024 * 1024);
	}

	public void disposeEditor(@NotNull final FileEditor editor) {
		Disposer.dispose(editor);
	}

	@NotNull
	public FileEditorState readState(@NotNull final Element element, @NotNull final Project project, @NotNull final VirtualFile file) {
		return new StashEditorState(-1, ArrayUtil.EMPTY_STRING_ARRAY);
	}

	public void writeState(@NotNull final FileEditorState state, @NotNull final Project project, @NotNull final Element element) {
	}

	@NotNull
	public String getEditorTypeId() {
		return "stash-editor";
	}

	@NotNull
	public FileEditorPolicy getPolicy() {
		return FileEditorPolicy.HIDE_DEFAULT_EDITOR;
	}


	private static class LargeFileEditor extends UserDataHolderBase implements FileEditor {
		private final VirtualFile myFile;

		public LargeFileEditor(VirtualFile file) {
			myFile = file;
		}

		@NotNull
		@Override
		public JComponent getComponent() {
			final JBPanel panel = new JBPanel();
			JLabel label = new JLabel("File " + myFile.getPath() + " is too large for " + ApplicationNamesInfo.getInstance().getFullProductName() + " editor");
			label.setHorizontalAlignment(SwingConstants.CENTER);
			JButton button = new JButton("Convert to Tara");
			button.addActionListener(e -> ApplicationManager.getApplication().runWriteAction(() -> {
				try {
					final Path tara = StashToTara.createTara(myFile, new File(myFile.getPath() + ".tara"));
					VfsUtil.findFileByIoFile(tara.toFile(), true);
				} catch (IOException ignored) {
					ignored.printStackTrace();
				}
			}));
			button.setHorizontalAlignment(SwingConstants.CENTER);
			panel.add(label, 0);
			panel.add(button, 1);
			return panel;
		}

		@Override
		public JComponent getPreferredFocusedComponent() {
			return null;
		}

		@NotNull
		@Override
		public String getName() {
			return "Large file editor";
		}

		@NotNull
		@Override
		public FileEditorState getState(@NotNull FileEditorStateLevel level) {
			return new TextEditorState();
		}

		@Override
		public void setState(@NotNull FileEditorState state) {
		}

		@Override
		public boolean isModified() {
			return false;
		}

		@Override
		public boolean isValid() {
			return myFile.isValid();
		}

		@Override
		public void selectNotify() {
		}

		@Override
		public void deselectNotify() {
		}

		@Override
		public void addPropertyChangeListener(@NotNull PropertyChangeListener listener) {
		}

		@Override
		public void removePropertyChangeListener(@NotNull PropertyChangeListener listener) {
		}

		@Override
		public BackgroundEditorHighlighter getBackgroundHighlighter() {
			return null;
		}

		@Override
		public FileEditorLocation getCurrentLocation() {
			return null;
		}

		@Override
		public StructureViewBuilder getStructureViewBuilder() {
			return null;
		}

		@Override
		public void dispose() {
		}

	}
}
