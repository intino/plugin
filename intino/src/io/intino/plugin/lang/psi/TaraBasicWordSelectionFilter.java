package io.intino.plugin.lang.psi;

import com.intellij.openapi.util.Condition;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;

public class TaraBasicWordSelectionFilter implements Condition<PsiElement> {
	@Override
	public boolean value(PsiElement element) {
		final IElementType elementType = element.getNode().getElementType();
		return elementType.equals(TaraTypes.STRING_VALUE) || elementType.equals(TaraTypes.EXPRESSION);
	}
}
