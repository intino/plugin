package io.intino.plugin.codeinsight.moveupdown;

import com.intellij.codeInsight.editorActions.moveUpDown.LineRange;
import com.intellij.codeInsight.editorActions.moveUpDown.StatementUpDownMover;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.util.PsiTreeUtil;
import io.intino.magritte.lang.model.Node;
import io.intino.plugin.lang.psi.StringValue;
import io.intino.plugin.lang.psi.TaraModel;
import io.intino.plugin.lang.psi.TaraNode;
import io.intino.plugin.lang.psi.TaraPsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

public class TaraNodeMover extends StatementUpDownMover {

	private static final List<String> QUOTES = Arrays.asList("'''", "\"\"\"", "'", "\"");

	@Override
	public boolean checkAvailable(@NotNull Editor editor, @NotNull PsiFile file, @NotNull MoveInfo info, boolean down) {
		if (!(file instanceof TaraModel)) return false;
		final int offset = editor.getCaretModel().getOffset();
		final SelectionModel selectionModel = editor.getSelectionModel();
		final Document document = editor.getDocument();
		final int lineNumber = document.getLineNumber(offset);
		int start = getLineStartSafeOffset(document, lineNumber);
		final int lineEndOffset = document.getLineEndOffset(lineNumber);
		int end = lineEndOffset == 0 ? 0 : lineEndOffset - 1;

		if (selectionModel.hasSelection()) {
			start = selectionModel.getSelectionStart();
			final int selectionEnd = selectionModel.getSelectionEnd();
			end = selectionEnd == 0 ? 0 : selectionEnd - 1;
		}
		PsiElement elementToMove1 = file.findElementAt(start);
		PsiElement elementToMove2 = file.findElementAt(end);
		if (elementToMove1 == null || elementToMove2 == null) return false;
		if (ifInsideString(document, lineNumber, elementToMove1, down)) return false;
		elementToMove1 = getCommentOrNode(document, elementToMove1);
		elementToMove2 = getCommentOrNode(document, elementToMove2);
		if (PsiTreeUtil.isAncestor(elementToMove1, elementToMove2, false)) {
			elementToMove2 = elementToMove1;
		} else if (PsiTreeUtil.isAncestor(elementToMove2, elementToMove1, false)) {
			elementToMove1 = elementToMove2;
		}
		info.toMove = new MyLineRange(elementToMove1, elementToMove2);
		info.toMove2 = getDestinationScope(file, editor, down ? elementToMove2 : elementToMove1, down);

		info.indentTarget = false;
		info.indentSource = false;

		return true;
	}

	private LineRange getDestinationScope(@NotNull final PsiFile file, @NotNull final Editor editor, @NotNull final PsiElement elementToMove, boolean down) {
		final Document document = file.getViewProvider().getDocument();
		if (document == null) return null;

		final int offset = down ? elementToMove.getTextRange().getEndOffset() : elementToMove.getTextRange().getStartOffset();
		int lineNumber = down ? document.getLineNumber(offset) + 1 : document.getLineNumber(offset) - 1;
		if (moveOutsideFile(document, lineNumber)) return null;
		int lineEndOffset = document.getLineEndOffset(lineNumber);
		final PsiElement destination = null;//getDestinationElement(elementToMove, document, lineEndOffset, down);
		final int start = destination != null ? destination.getTextRange().getStartOffset() : lineNumber;
		final int end = destination != null ? destination.getTextRange().getEndOffset() : lineNumber;
		final int startLine = document.getLineNumber(start);
		final int endLine = document.getLineNumber(end);

		if (elementToMove instanceof Node) {
			TaraPsiElement scope = (TaraPsiElement) ((Node) elementToMove).container();
			if (destination != null) return new ScopeRange(scope, destination, !down, true);
		}
		return new LineRange(startLine, endLine + 1);
	}

	private static boolean moveOutsideFile(@NotNull final Document document, int lineNumber) {
		return lineNumber < 0 || lineNumber >= document.getLineCount();
	}


	@Override
	public void beforeMove(@NotNull Editor editor, @NotNull MoveInfo info, boolean down) {
		super.beforeMove(editor, info, down);
	}

	@Override
	public void afterMove(@NotNull Editor editor, @NotNull PsiFile file, @NotNull MoveInfo info, boolean down) {
		super.afterMove(editor, file, info, down);
	}

	private static boolean ifInsideString(@NotNull final Document document, int lineNumber, @NotNull final PsiElement elementToMove1, boolean down) {
		int start = document.getLineStartOffset(lineNumber);
		final int end = document.getLineEndOffset(lineNumber);
		int nearLine = down ? lineNumber + 1 : lineNumber - 1;
		if (nearLine >= document.getLineCount() || nearLine <= 0) return false;
		final StringValue stringLiteralExpression = PsiTreeUtil.getParentOfType(elementToMove1, StringValue.class);
		if (stringLiteralExpression != null) {
			final Pair<String, String> quotes = getQuotes(stringLiteralExpression.getText());
			if (quotes != null && (quotes.first.equals("'''") || quotes.first.equals("\"\"\""))) {
				final String text1 = document.getText(TextRange.create(start, end)).trim();
				final String text2 = document.getText(TextRange.create(document.getLineStartOffset(nearLine), document.getLineEndOffset(nearLine))).trim();
				if (!text1.startsWith(quotes.first) && !text1.endsWith(quotes.second) && !text2.startsWith(quotes.first) && !text2.endsWith(quotes.second))
					return true;
			}
		}
		return false;
	}

	@Nullable
	private static Pair<String, String> getQuotes(@NotNull final String text) {
		boolean start = true;
		int pos = 0;
		for (int i = 0; i < text.length(); i++) {
			final char c = Character.toLowerCase(text.charAt(i));
			if (start) {
				if (c == 'u' || c == 'r' || c == 'b') {
					pos = i + 1;
				} else {
					start = false;
				}
			} else {
				break;
			}
		}
		final String prefix = text.substring(0, pos);
		final String mainText = text.substring(pos);
		for (String quote : QUOTES) {
			final Pair<String, String> quotes = getQuotes(mainText, prefix, quote);
			if (quotes != null) {
				return quotes;
			}
		}
		return null;
	}

	@Nullable
	private static Pair<String, String> getQuotes(@NotNull String text, @NotNull String prefix, @NotNull String quote) {
		final int length = text.length();
		final int n = quote.length();
		if (length >= 2 * n && text.startsWith(quote) && text.endsWith(quote)) {
			return Pair.create(prefix + text.substring(0, n), text.substring(length - n));
		}
		return null;
	}

	// use to keep elements
	static class MyLineRange extends LineRange {
		private PsiElement myStartElement;
		private PsiElement myEndElement;
		int size = 0;
		int statementsSize = 0;

		public MyLineRange(@NotNull PsiElement start, PsiElement end) {
			super(start, end);
			myStartElement = start;
			myEndElement = end;

			if (myStartElement == myEndElement) {
				size = 1;
				statementsSize = 1;
			} else {
				PsiElement counter = myStartElement;
				while (counter != myEndElement && counter != null) {
					size += 1;
					if (!(counter instanceof PsiWhiteSpace) && !(counter instanceof PsiComment))
						statementsSize += 1;
					counter = counter.getNextSibling();
				}
				size += 1;
				if (!(counter instanceof PsiWhiteSpace) && !(counter instanceof PsiComment))
					statementsSize += 1;
			}
		}
	}

	@NotNull
	private static PsiElement getCommentOrNode(@NotNull final Document document, @NotNull PsiElement destination) {
		final PsiElement node = PsiTreeUtil.getParentOfType(destination, TaraNode.class, false);
		if (node == null) return destination;
		if (destination instanceof PsiComment) {
			if (document.getLineNumber(destination.getTextOffset()) == document.getLineNumber(node.getTextOffset()))
				destination = node;
		} else
			destination = node;
		return destination;
	}


	static class SelectionContainer {
		private int myLen;
		private int myAdditional;
		private boolean myAtTheBeginning;

		public SelectionContainer(int len, int additional, boolean atTheBeginning) {
			myLen = len;
			myAdditional = additional;
			myAtTheBeginning = atTheBeginning;
		}
	}

	// Use when element scope changed
	static class ScopeRange extends LineRange {
		private PsiElement myScope;
		@NotNull
		private PsiElement myAnchor;
		private boolean addBefore;
		private boolean theSameLevel;

		public ScopeRange(@NotNull PsiElement scope, @NotNull PsiElement anchor, boolean before) {
			super(scope);
			myScope = scope;
			myAnchor = anchor;
			addBefore = before;
		}

		public ScopeRange(TaraPsiElement scope, @NotNull PsiElement anchor, boolean before, boolean b) {
			super(scope);
			myScope = scope;
			myAnchor = anchor;
			addBefore = before;
			theSameLevel = b;
		}

		@NotNull
		public PsiElement getAnchor() {
			return myAnchor;
		}

		public PsiElement getScope() {
			return myScope;
		}

		public boolean isAddBefore() {
			return addBefore;
		}

		public boolean isTheSameLevel() {
			return theSameLevel;
		}
	}
}
