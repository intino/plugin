package io.intino.plugin.lang.psi.impl;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import io.intino.plugin.lang.psi.TaraTypes;
import org.jetbrains.annotations.NotNull;

public class StringMixin extends ASTWrapperPsiElement {

	public StringMixin(ASTNode astNode) {
		super(astNode);
	}

	public String getValue() {
		return getCleanedValue();
	}

	@NotNull
	private String getCleanedValue() {
		if (!isMultiLine())
			return this.getText().substring(1, this.getTextLength() - 1);
		else return getCleanMultiLine();
	}

	private String getCleanMultiLine() {
		StringBuilder value = new StringBuilder();
		PsiElement child = this.getFirstChild();
		while (child != null) {
			if (!child.getNode().getElementType().equals(TaraTypes.QUOTE_BEGIN) && !child.getNode().getElementType().equals(TaraTypes.QUOTE_END))
				value.append(child.getText());
			child = child.getNextSibling();
		}
		return value.toString().trim();
	}

	public boolean isMultiLine() {
		return this.getText().trim().startsWith("=");
	}

	public void toInline() {

	}

	public void toMultiline() {

	}
}
