package io.intino.plugin.stash;

import com.intellij.ide.ui.customization.CustomActionsSchema;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.colors.EditorColorsManager;
import com.intellij.openapi.editor.event.*;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.editor.ex.EditorMarkupModel;
import com.intellij.openapi.editor.highlighter.EditorHighlighter;
import com.intellij.openapi.editor.highlighter.EditorHighlighterFactory;
import com.intellij.openapi.editor.impl.EditorImpl;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.TextEditor;
import com.intellij.openapi.fileEditor.impl.EditorHistoryManager;
import com.intellij.openapi.fileEditor.impl.text.FileDropHandler;
import com.intellij.openapi.fileTypes.FileTypeEvent;
import com.intellij.openapi.fileTypes.FileTypeListener;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileEvent;
import com.intellij.openapi.vfs.VirtualFileListener;
import com.intellij.openapi.vfs.VirtualFilePropertyEvent;
import com.intellij.ui.components.JBLoadingPanel;
import com.intellij.util.FileContentUtilCore;
import com.intellij.util.messages.MessageBusConnection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;

class StashEditorComponent extends JBLoadingPanel implements DataProvider {
	private static final Logger LOG = Logger.getInstance("#com.plugin.openapi.fileEditor.impl.text.TextEditorComponent");

	private final Project myProject;
	@NotNull
	private final VirtualFile myFile;
	private final Document myDocument;

	private final MyEditorMouseListener myEditorMouseListener;
	private final MyDocumentListener myDocumentListener;
	private final MyVirtualFileListener myVirtualFileListener;
	@NotNull
	private final Editor myEditor;
	private final MessageBusConnection myConnection;
	private boolean myModified;
	private boolean myValid;

	StashEditorComponent(@NotNull final Project project, @NotNull final VirtualFile file, @NotNull final TextEditor textEditor) {
		super(new BorderLayout(), textEditor);
		myProject = project;
		myFile = file;

		myDocument = FileDocumentManager.getInstance().getDocument(myFile);
		LOG.assertTrue(myDocument != null);
		myDocumentListener = new MyDocumentListener();
		myDocument.addDocumentListener(myDocumentListener);

		myEditorMouseListener = new MyEditorMouseListener();

		myEditor = createEditor();
		add(myEditor.getComponent(), BorderLayout.CENTER);
		myModified = isModifiedImpl();
		myValid = isEditorValidImpl();
		LOG.assertTrue(myValid);

		myVirtualFileListener = new MyVirtualFileListener();
		myFile.getFileSystem().addVirtualFileListener(myVirtualFileListener);
		myConnection = project.getMessageBus().connect();
		myConnection.subscribe(FileTypeManager.TOPIC, new MyFileTypeListener());
		myConnection.subscribe(DumbService.DUMB_MODE, new DumbService.DumbModeListener() {
			@Override
			public void enteredDumbMode() {
				updateHighlighters();
			}

			@Override
			public void exitDumbMode() {
				updateHighlighters();
			}
		});
	}

	void dispose() {
		myDocument.removeDocumentListener(myDocumentListener);
		if (!myProject.isDefault()) EditorHistoryManager.getInstance(myProject).updateHistoryEntry(myFile, false);
		disposeEditor(myEditor);
		myConnection.disconnect();
		myFile.getFileSystem().removeVirtualFileListener(myVirtualFileListener);
	}

	@NotNull
	Editor getEditor() {
		return myEditor;
	}

	@NotNull
	private Editor createEditor() {
		Editor editor = EditorFactory.getInstance().createEditor(myDocument, myProject);
		((EditorMarkupModel) editor.getMarkupModel()).setErrorStripeVisible(true);
		EditorHighlighter highlighter = EditorHighlighterFactory.getInstance().createEditorHighlighter(myFile, EditorColorsManager.getInstance().getGlobalScheme(), myProject);
		((EditorEx) editor).setHighlighter(highlighter);
		((EditorEx) editor).setFile(myFile);
		editor.addEditorMouseListener(myEditorMouseListener);
		((EditorImpl) editor).setDropHandler(new FileDropHandler(editor));
		return editor;
	}

	private void disposeEditor(@NotNull Editor editor) {
		EditorFactory.getInstance().releaseEditor(editor);
		editor.removeEditorMouseListener(myEditorMouseListener);
	}

	private boolean isModifiedImpl() {
		return FileDocumentManager.getInstance().isFileModified(myFile);
	}

	private void updateModifiedProperty() {
		myModified = isModifiedImpl();
	}

	private boolean isEditorValidImpl() {
		return FileDocumentManager.getInstance().getDocument(myFile) != null;
	}

	private void updateValidProperty() {
		myValid = isEditorValidImpl();
	}

	private void updateHighlighters() {
		if (!myProject.isDisposed() && !myEditor.isDisposed()) {
			final EditorHighlighter highlighter = EditorHighlighterFactory.getInstance().createEditorHighlighter(myProject, myFile);
			((EditorEx) myEditor).setHighlighter(highlighter);
		}
	}

	@Nullable
	private Editor validateCurrentEditor() {
		Component focusOwner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
		if (focusOwner instanceof JComponent && ((JComponent) focusOwner).getClientProperty("AuxEditorComponent") != null)
			return null;
		return myEditor;
	}

	@Override
	public Object getData(final String dataId) {
		final Editor e = validateCurrentEditor();
		if (e == null) return null;
		if (!myProject.isDisposed() && !myProject.isDefault()) {
			final Object o = FileEditorManager.getInstance(myProject).getData(dataId, e, e.getCaretModel().getCurrentCaret());
			if (o != null) return o;
		}
		if (CommonDataKeys.EDITOR.is(dataId)) return e;
		if (CommonDataKeys.VIRTUAL_FILE.is(dataId)) return myFile.isValid() ? myFile : null;
		return null;
	}

	@NotNull
	public VirtualFile getFile() {
		return myFile;
	}

	private static void assertThread() {
		ApplicationManager.getApplication().assertIsDispatchThread();
	}

	private static final class MyEditorMouseListener implements EditorMouseListener {
		@Override
		public void mouseClicked(@NotNull EditorMouseEvent e) {
			handle(e);
		}

		@Override
		public void mousePressed(@NotNull EditorMouseEvent e) {
			handle(e);
		}

		@Override
		public void mouseReleased(@NotNull EditorMouseEvent e) {
			handle(e);
		}

		private void handle(EditorMouseEvent e) {
			if (e.getMouseEvent().isPopupTrigger() && e.getArea() == EditorMouseEventArea.EDITING_AREA) {
				invokePopup(e);
				e.consume();
			}
		}

		void invokePopup(final EditorMouseEvent event) {
			if (!event.isConsumed() && event.getArea() == EditorMouseEventArea.EDITING_AREA) {
				ActionGroup group = (ActionGroup) CustomActionsSchema.getInstance().getCorrectedAction(IdeActions.GROUP_EDITOR_POPUP);
				ActionPopupMenu popupMenu = ActionManager.getInstance().createActionPopupMenu(ActionPlaces.EDITOR_POPUP, group);
				MouseEvent e = event.getMouseEvent();
				final Component c = e.getComponent();
				if (c != null && c.isShowing()) popupMenu.getComponent().show(c, e.getX(), e.getY());
				e.consume();
			}
		}
	}

	private final class MyDocumentListener implements DocumentListener {
		private final Runnable myUpdateRunnable;

		MyDocumentListener() {
			myUpdateRunnable = StashEditorComponent.this::updateModifiedProperty;
		}

		@Override
		public void documentChanged(DocumentEvent e) {
			ApplicationManager.getApplication().invokeLater(myUpdateRunnable);
		}
	}

	private final class MyFileTypeListener implements FileTypeListener {
		@Override
		public void fileTypesChanged(@NotNull final FileTypeEvent event) {
			assertThread();
			updateValidProperty();
			updateHighlighters();
		}
	}

	private final class MyVirtualFileListener implements VirtualFileListener {
		@Override
		public void propertyChanged(@NotNull final VirtualFilePropertyEvent e) {
			if (VirtualFile.PROP_NAME.equals(e.getPropertyName())) {
				updateValidProperty();
				if (Comparing.equal(e.getFile(), myFile) &&
						(FileContentUtilCore.FORCE_RELOAD_REQUESTOR.equals(e.getRequestor()) ||
								!Comparing.equal(e.getOldValue(), e.getNewValue()))) {
					updateHighlighters();
				}
			}
		}

		@Override
		public void contentsChanged(@NotNull VirtualFileEvent event) {
			if (event.isFromSave()) {
				assertThread();
				VirtualFile file = event.getFile();
				LOG.assertTrue(file.isValid());
				if (myFile.equals(file)) updateModifiedProperty();
			}
		}
	}
}
