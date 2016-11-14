package io.intino.legio.plugin.file;

import com.intellij.codeInsight.editorActions.CopyPastePostProcessor;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class LegioDependencyPasteProcessor extends CopyPastePostProcessor {
	@NotNull
	@Override
	public List collectTransferableData(PsiFile psiFile, Editor editor, int[] ints, int[] ints1) {
		return Collections.emptyList();

	}
}
