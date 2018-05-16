package io.intino.plugin.codeinsight.annotators.fix;

import com.intellij.openapi.application.Result;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.psi.PsiElement;
import com.intellij.util.IncorrectOperationException;
import io.intino.tara.plugin.lang.psi.TaraElementFactory;
import io.intino.tara.plugin.lang.psi.TaraNode;
import org.jetbrains.annotations.NotNull;

public class FixUtils {


	static void addNewLine(TaraNode node) {
		try {
			final PsiElement newLine = TaraElementFactory.getInstance(node.getProject()).createNewLine();
			node.add(newLine.copy());
			node.add(newLine.copy());
		} catch (IncorrectOperationException e) {
			new WriteCommandAction(node.getProject(), node.getContainingFile()) {
				protected void run(@NotNull Result result) {
					final PsiElement newLine = TaraElementFactory.getInstance(node.getProject()).createNewLine();
					node.add(newLine.copy());
					node.add(newLine.copy());
				}
			}.execute();
		}
	}

}
