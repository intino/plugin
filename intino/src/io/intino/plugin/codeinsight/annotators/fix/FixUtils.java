package io.intino.plugin.codeinsight.annotators.fix;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.psi.PsiElement;
import com.intellij.util.IncorrectOperationException;
import io.intino.plugin.lang.psi.TaraElementFactory;
import io.intino.plugin.lang.psi.TaraNode;

public class FixUtils {


	static void addNewLine(TaraNode node) {
		if (node.getContainingFile() == null) return;
		try {
			if (ApplicationManager.getApplication().isWriteAccessAllowed()) newLine(node);
			else
				WriteCommandAction.writeCommandAction(node.getProject(), node.getContainingFile()).run(() -> newLine(node));
		} catch (IncorrectOperationException e) {
			WriteCommandAction.writeCommandAction(node.getProject(), node.getContainingFile()).run(() -> newLine(node));
		}
	}

	private static void newLine(TaraNode node) {
		final PsiElement newLine = TaraElementFactory.getInstance(node.getProject()).createNewLine();
		node.add(newLine.copy());
		node.add(newLine.copy());
	}

}
