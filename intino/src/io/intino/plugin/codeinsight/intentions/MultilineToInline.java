package io.intino.plugin.codeinsight.intentions;

import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.util.IncorrectOperationException;
import io.intino.plugin.lang.psi.*;
import io.intino.plugin.lang.psi.impl.TaraPsiUtil;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.intellij.psi.util.PsiTreeUtil.findChildrenOfType;

public class MultilineToInline extends PsiElementBaseIntentionAction {
	@Override
	public void invoke(@NotNull Project project, Editor editor, @NotNull PsiElement element) throws IncorrectOperationException {
		final Expression expression = TaraPsiUtil.getContainerByType(element, Expression.class);
		final PsiElement newExpression = TaraElementFactory.getInstance(project).createExpression(expression.getValue());
		final Valued valued = TaraPsiUtil.getContainerByType(expression, Valued.class);
		if (valued == null) return;
		expression.delete();
		if (valued.getValue() != null) valued.getValue().getExpressionList().add((TaraExpression) newExpression);
		else {
			Identifier identifier = lastOf(findChildrenOfType(valued, Identifier.class));
			valued.addAfter(newExpression.getParent().getPrevSibling().copy(), identifier);
			valued.addAfter(newExpression.getParent().getPrevSibling().getPrevSibling().copy(), identifier);
			valued.addAfter(newExpression.getParent().getPrevSibling().copy(), identifier);
			valued.addAfter(newExpression.getParent().copy(), identifier.getNextSibling().getNextSibling().getNextSibling());
			valued.getLastChild().delete();
		}
	}

	@Override
	public boolean isAvailable(@NotNull Project project, Editor editor, @NotNull PsiElement element) {
		final Expression expression = TaraPsiUtil.getContainerByType(element, Expression.class);
		return expression != null && expression.isMultiLine() && !expression.getValue().contains("\n");
	}

	@Nls
	@NotNull
	@Override
	public String getFamilyName() {
		return getText();
	}

	@NotNull
	@Override
	public String getText() {
		return "To inline";
	}

	private static <T> T lastOf(Collection<T> collection) {
		if (collection.isEmpty()) return null;
		final List<T> identifiers = new ArrayList<>(collection);
		return identifiers.get(identifiers.size() - 1);
	}
}
