package io.intino.plugin.lang.psi;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.AbstractElementManipulator;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;

public class TaraExpressionManipulator extends AbstractElementManipulator<Expression> {

	public static TextRange getStringTokenRange(final Expression element) {
		return TextRange.from(1, element.getTextLength() - 2);
	}

	@Override
	public Expression handleContentChange(@NotNull Expression expression, @NotNull TextRange range, String newContent) throws IncorrectOperationException {
		return (Expression) expression.updateText(newContent);
	}

	@NotNull
	@Override
	public TextRange getRangeInElement(@NotNull final Expression element) {
		return getStringTokenRange(element);
	}
}
