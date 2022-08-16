package io.intino.plugin.toolwindows.metamodel;

import com.intellij.lang.Language;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.event.CaretEvent;
import com.intellij.openapi.editor.event.CaretListener;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.editor.ex.util.LexerEditorHighlighter;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.FileEditorManagerEvent;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.util.messages.MessageBusConnection;
import io.intino.magritte.lang.model.Node;
import io.intino.magritte.lang.semantics.Documentation;
import io.intino.plugin.highlighting.TaraSyntaxHighlighter;
import io.intino.plugin.lang.LanguageManager;
import io.intino.plugin.lang.TaraLanguage;
import io.intino.plugin.lang.psi.MetaIdentifier;
import io.intino.plugin.lang.psi.impl.TaraPsiUtil;
import io.intino.plugin.settings.IntinoSettings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import java.util.List;

import static io.intino.plugin.lang.psi.impl.TaraPsiUtil.write;

public class MetamodelWindow {
	private static final Logger LOG = Logger.getInstance(MetamodelWindow.class);
	private final Project project;
	private final EditorEx editor;
	private final Document document;
	private final EditorListener listener;
	private JPanel root;
	private PsiElement elementAtCaret;

	public MetamodelWindow(Project project) {
		this.project = project;
		listener = new EditorListener(project);
		document = EditorFactory.getInstance().createDocument("");
		editor = (EditorEx) EditorFactory.getInstance().createViewer(document, project);
		editor.setHighlighter(new LexerEditorHighlighter(new TaraSyntaxHighlighter(), editor.getColorsScheme()));
		editor.getSelectionModel().setSelection(0, document.getTextLength());
		GridConstraints constraints = new GridConstraints();
		constraints.setFill(GridConstraints.FILL_BOTH);
		root.add(editor.getComponent(), constraints);
		activeListener();
	}

	public JPanel content() {
		return root;
	}

	public void refreshRootElement() {
	}

	public PsiElement getRootElement() {
		return elementAtCaret;
	}

	public void selectElementAtCaret() {
		selectElementAtCaret(FileEditorManager.getInstance(project).getSelectedTextEditor());
	}

	public void selectElementAtCaret(@Nullable Editor editor) {
		if (editor == null) return;
		PsiFile psiFile = PsiDocumentManager.getInstance(project).getPsiFile(editor.getDocument());
		if (psiFile == null || !psiFile.getLanguage().is(TaraLanguage.INSTANCE)) return;
		Language selectedLanguage = psiFile.getLanguage();
		FileViewProvider viewProvider = psiFile.getViewProvider();
		PsiFile selectedRoot = viewProvider.getPsi(selectedLanguage);
		if (selectedRoot == null) selectedLanguage = null;
		if (selectedLanguage == null) selectedLanguage = psiFile.getLanguage();
		PsiElement elementAtCaret = viewProvider.findElementAt(editor.getCaretModel().getOffset(), selectedLanguage);
		if (elementAtCaret != null && elementAtCaret.getParent() != null) {
			if (elementAtCaret.getParent().getChildren().length == 0) elementAtCaret = elementAtCaret.getParent();
		}
		if (elementAtCaret != null && elementAtCaret != getSelectedElement() && isMetaIdentifier(elementAtCaret)) {
			setSelectedElement(elementAtCaret);
			updateEditor(psiFile);
		}
	}

	private void updateEditor(PsiFile psiFile) {
		Node node = TaraPsiUtil.getContainerNodeOf(elementAtCaret);
		if (node == null) return;
		List<String> types = node.types();
		io.intino.magritte.Language lang = LanguageManager.getLanguage(psiFile);
		if (lang != null && !types.isEmpty()) {
			Documentation doc = lang.doc(types.stream().filter(t -> !t.contains(":")).findFirst().orElse(types.get(0)));
			if (doc != null) write(() -> document.setText("dsl " + lang.metaLanguage() + "\n\n" + doc.description()));
		}
	}

	private boolean isMetaIdentifier(PsiElement element) {
		return element instanceof MetaIdentifier || element.getParent() instanceof MetaIdentifier;
	}

	private PsiElement getSelectedElement() {
		return elementAtCaret;
	}

	private void setSelectedElement(PsiElement elementAtCaret) {
		this.elementAtCaret = elementAtCaret;
	}

	private void activeListener() {
		root.addAncestorListener(new AncestorListener() {
			@Override
			public void ancestorAdded(AncestorEvent ancestorEvent) {
				listener.start();
			}

			@Override
			public void ancestorRemoved(AncestorEvent ancestorEvent) {
				listener.stop();

			}

			@Override
			public void ancestorMoved(AncestorEvent ancestorEvent) {
			}
		});
	}

	private void debug(String message) {
		if (LOG.isDebugEnabled()) {
			LOG.debug(message);
		}
	}

	public class EditorListener implements FileEditorManagerListener, CaretListener, Disposable {
		private final Project myProject;
		private final PsiTreeChangeListener myTreeChangeListener;
		private Editor myCurrentEditor;
		private MessageBusConnection myMessageBus;

		public EditorListener(Project project) {
			myProject = project;
			myTreeChangeListener = new PsiTreeChangeAdapter() {
				public void childrenChanged(@NotNull final PsiTreeChangeEvent event) {
					updateTreeFromPsiTreeChange(event);
				}

				public void childAdded(@NotNull PsiTreeChangeEvent event) {
					updateTreeFromPsiTreeChange(event);
				}

				public void childMoved(@NotNull PsiTreeChangeEvent event) {
					updateTreeFromPsiTreeChange(event);
				}

				public void childRemoved(@NotNull PsiTreeChangeEvent event) {
					updateTreeFromPsiTreeChange(event);
				}

				public void childReplaced(@NotNull PsiTreeChangeEvent event) {
					updateTreeFromPsiTreeChange(event);
				}

				public void propertyChanged(@NotNull PsiTreeChangeEvent event) {
					updateTreeFromPsiTreeChange(event);
				}
			};
		}

		private void updateTreeFromPsiTreeChange(final PsiTreeChangeEvent event) {
			if (isElementChangedUnderViewerRoot(event)) {
				LOG.debug("PSI Change, starting update timer");
				ApplicationManager.getApplication().runWriteAction(MetamodelWindow.this::refreshRootElement);
			}
		}

		private boolean isElementChangedUnderViewerRoot(final PsiTreeChangeEvent event) {
			PsiElement elementChangedByPsi = event.getParent();
			PsiElement viewerRootElement = MetamodelWindow.this.getRootElement();
			boolean b = false;
			try {
				b = PsiTreeUtil.isAncestor(viewerRootElement, elementChangedByPsi, false);
			} catch (Throwable ignored) {
			}
			return b;
		}

		public void fileOpened(@NotNull FileEditorManager source, @NotNull VirtualFile file) {
			debug("source = [" + source + "], file = [" + file + "]");
		}

		public void fileClosed(@NotNull FileEditorManager source, @NotNull VirtualFile file) {
			debug("source = [" + source + "], file = [" + file + "]");
		}

		public void selectionChanged(@NotNull FileEditorManagerEvent event) {
			debug("selection changed " + event);
			if (event.getNewFile() == null || myCurrentEditor == null) return;
			Editor newEditor = event.getManager().getSelectedTextEditor();
			if (myCurrentEditor != newEditor) myCurrentEditor.getCaretModel().removeCaretListener(this);
			MetamodelWindow.this.selectElementAtCaret();
			if (newEditor != null) myCurrentEditor = newEditor;
			myCurrentEditor.getCaretModel().addCaretListener(this, IntinoSettings.getInstance(myProject));
		}


		public void caretPositionChanged(CaretEvent event) {
			final Editor editor = event.getEditor();
			debug("caret moved to " + editor.getCaretModel().getOffset() + " in editor " + editor);
			MetamodelWindow.this.selectElementAtCaret();
		}

		public void start() {
			IntinoSettings pluginDisposable = IntinoSettings.getInstance(myProject);
			myMessageBus = myProject.getMessageBus().connect(pluginDisposable);
			myMessageBus.subscribe(FileEditorManagerListener.FILE_EDITOR_MANAGER, this);
			PsiManager.getInstance(myProject).addPsiTreeChangeListener(myTreeChangeListener, pluginDisposable);
			myCurrentEditor = FileEditorManager.getInstance(myProject).getSelectedTextEditor();
			if (myCurrentEditor != null) myCurrentEditor.getCaretModel().addCaretListener(this, pluginDisposable);
		}

		public void stop() {
			if (myMessageBus != null) {
				myMessageBus.disconnect();
				myMessageBus = null;
			}
			PsiManager.getInstance(myProject).removePsiTreeChangeListener(myTreeChangeListener);
		}

		@Override
		public void dispose() {
			stop();
		}
	}
}