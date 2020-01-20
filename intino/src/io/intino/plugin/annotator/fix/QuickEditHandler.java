package io.intino.plugin.annotator.fix;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.editor.*;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.editor.event.EditorFactoryEvent;
import com.intellij.openapi.editor.event.EditorFactoryListener;
import com.intellij.openapi.editor.ex.DocumentEx;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.fileEditor.ex.FileEditorManagerEx;
import com.intellij.openapi.fileEditor.impl.EditorWindow;
import com.intellij.openapi.fileEditor.impl.FileEditorManagerImpl;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiMethod;
import com.intellij.util.ObjectUtils;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class QuickEditHandler implements Disposable, DocumentListener {
	private final Project myProject;

	private final Editor myEditor;
	private final PsiMethod method;
	private final Document myOrigDocument;

	private final Document myNewDocument;
	private final PsiFile file;

	private static final Key<String> REPLACEMENT_KEY = Key.create("REPLACEMENT_KEY");

	QuickEditHandler(Project project, Editor editor, final PsiFile origFile, final PsiMethod method) {
		myProject = project;
		myEditor = editor;
		this.method = method;
		myOrigDocument = editor.getDocument();
		file = origFile;

		// suppress possible errors as in injected mode
		myNewDocument = PsiDocumentManager.getInstance(project).getDocument(file);
		assert myNewDocument != null;
		myOrigDocument.addDocumentListener(this, this);
		myNewDocument.addDocumentListener(this, this);
		EditorFactory editorFactory = ObjectUtils.assertNotNull(EditorFactory.getInstance());
		// not FileEditorManager listener because of RegExp checker and alike
		editorFactory.addEditorFactoryListener(new EditorFactoryListener() {
			int useCount;

			@Override
			public void editorCreated(@NotNull EditorFactoryEvent event) {
				if (event.getEditor().getDocument() != myNewDocument) return;
				useCount++;
			}

			@Override
			public void editorReleased(@NotNull EditorFactoryEvent event) {
				if (event.getEditor().getDocument() != myNewDocument) return;
				if (--useCount > 0) return;
				if (Boolean.TRUE.equals(origFile.getVirtualFile().getUserData(FileEditorManagerImpl.CLOSING_TO_REOPEN)))
					return;

				Disposer.dispose(QuickEditHandler.this);
			}
		}, this);

	}

	public void navigate() {
		final FileEditorManagerEx fileEditorManager = FileEditorManagerEx.getInstanceEx(myProject);
		final FileEditor[] editors = fileEditorManager.getEditors(file.getVirtualFile());
		if (editors.length == 0) {
			final EditorWindow curWindow = fileEditorManager.getCurrentWindow();
			curWindow.split(SwingConstants.HORIZONTAL, false, file.getVirtualFile(), true);
		}
		Editor editor = fileEditorManager.openTextEditor(new OpenFileDescriptor(myProject, file.getVirtualFile(), offset()), true);
		// fold missing values
		if (editor != null) {
			final FoldingModel foldingModel = editor.getFoldingModel();
			foldingModel.runBatchFoldingOperation(() -> {
				for (RangeMarker o : ContainerUtil.reverse(((DocumentEx) myNewDocument).getGuardedBlocks())) {
					String replacement = o.getUserData(REPLACEMENT_KEY);
					if (StringUtil.isEmpty(replacement)) continue;
					FoldRegion region = foldingModel.addFoldRegion(o.getStartOffset(), o.getEndOffset(), replacement);
					if (region != null) region.setExpanded(false);
				}
			});
		}
		SwingUtilities.invokeLater(() -> myEditor.getScrollingModel().scrollToCaret(ScrollType.MAKE_VISIBLE));

	}

	private int offset() {
		return this.method.getBody().getTextOffset();
	}


	@Override
	public void dispose() {
	}

}