package io.intino.plugin.codeinsight.intentions;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.util.IncorrectOperationException;
import io.intino.plugin.lang.psi.Expression;
import io.intino.plugin.lang.psi.TaraElementFactory;
import io.intino.plugin.lang.psi.TaraVarInit;
import io.intino.plugin.lang.psi.Valued;
import io.intino.plugin.lang.psi.impl.TaraPsiUtil;
import io.intino.tara.language.model.Variable;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

public class InlineToMultiline extends PsiElementBaseIntentionAction implements IntentionAction {
	@Override
	public void invoke(@NotNull Project project, Editor editor, @NotNull PsiElement element) throws IncorrectOperationException {
		final Expression expression = TaraPsiUtil.getContainerByType(element, Expression.class);
		if (expression == null) return;
		final String indent = getIndent(expression) + "\t";
		final TaraElementFactory factory = TaraElementFactory.getInstance(project);
		final PsiElement newExpression = factory.createMultiLineExpression(expression.getValue(), indent, indent, "---");
		final Valued valued = TaraPsiUtil.getContainerByType(expression, Valued.class);
		if (valued == null) return;
		expression.getParent().getPrevSibling().delete();
		expression.getParent().getPrevSibling().delete();
		expression.getParent().getPrevSibling().delete();
		expression.delete();
		valued.addAfter(factory.createNewLineIndent(indent.length()), valued.getLastChild());
		valued.addAfter(newExpression, valued.getLastChild());
	}

	@Override
	public boolean isAvailable(@NotNull Project project, Editor editor, @NotNull PsiElement element) {
		final Expression expression = TaraPsiUtil.getContainerByType(element, Expression.class);
		final Valued valued = TaraPsiUtil.getContainerByType(element, Valued.class);
		return valued != null && expression != null &&
				!expression.isMultiLine() && valued.getValue() != null && valued.getValue().getExpressionList().size() == 1 &&
				(valued instanceof Variable || valued instanceof TaraVarInit);
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
		return "To multi-line";
	}

	private static String getIndent(Expression element) {
		final PsiElement prevSibling = element.getParent().getParent().getPrevSibling();
		return prevSibling != null ? prevSibling.getText().substring(1) : "";
	}
}
