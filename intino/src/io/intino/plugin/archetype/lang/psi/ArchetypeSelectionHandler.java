package io.intino.plugin.archetype.lang.psi;

import com.intellij.codeInsight.editorActions.ExtendWordSelectionHandler;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.ElementManipulators;
import com.intellij.psi.PsiElement;

import java.util.ArrayList;
import java.util.List;

public class ArchetypeSelectionHandler implements ExtendWordSelectionHandler {

	@Override
	public boolean canSelect(PsiElement e) {
		return !e.getNode().getElementType().equals(ArchetypeTypes.NEWLINE);
	}

	@Override
	public List<TextRange> select(PsiElement e, CharSequence editorText, int cursorOffset, Editor editor) {
		final List<TextRange> result = new ArrayList<>();
		PsiElement source = e.getNode().getElementType().equals(ArchetypeTypes.CHARACTER) ? e.getParent() : e;
		result.add(ElementManipulators.getValueTextRange(source).shiftRight(source.getTextOffset()));
		return result;
	}
}