package io.intino.plugin.lang.psi.impl;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.AbstractElementManipulator;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;

public class UseReferenceManipulator extends AbstractElementManipulator<TaraHeaderReferenceImpl> {

	@Override
	public TaraHeaderReferenceImpl handleContentChange(@NotNull TaraHeaderReferenceImpl element, @NotNull TextRange range, String newContent) throws IncorrectOperationException {
		String newName = range.replace(element.getText(), newContent.contains(".") ? newContent.split("\\.")[0] : newContent);
		element.replace(TaraElementFactoryImpl.getInstance(element.getProject()).createImport(newName).getAnImportList().get(0).getHeaderReference().copy());
		return element;
	}
}