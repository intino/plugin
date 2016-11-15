package io.intino.legio.plugin.file;

import com.intellij.codeInsight.editorActions.CopyPastePostProcessor;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.RangeMarker;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Ref;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;

import java.awt.datatransfer.Transferable;
import java.util.Collections;
import java.util.List;

public class LegioDependencyPasteProcessor extends CopyPastePostProcessor {
	@NotNull
	@Override
	public List collectTransferableData(PsiFile psiFile, Editor editor, int[] ints, int[] ints1) {
		return Collections.emptyList();

	}

	@NotNull
	@Override
	public List extractTransferableData(Transferable content) {
		return super.extractTransferableData(content);
	}

	@Override
	public void processTransferableData(Project project, Editor editor, RangeMarker bounds, int caretOffset, Ref indented, List values) {
		super.processTransferableData(project, editor, bounds, caretOffset, indented, values);
	}
}
