package io.intino.plugin.lang.psi.impl;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.AbstractElementManipulator;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;

public class IdentifierManipulator extends AbstractElementManipulator<TaraIdentifierImpl> {
	@Override
	public TaraIdentifierImpl handleContentChange(@NotNull TaraIdentifierImpl element, @NotNull TextRange range, String newContent) throws IncorrectOperationException {
		String newName = range.replace(element.getText(), newContent.contains(".") ? newContent.split("\\.")[0] : newContent);
		element.replace(TaraElementFactoryImpl.getInstance(element.getProject()).createNameIdentifier(newName).copy());
		return element;
	}
}