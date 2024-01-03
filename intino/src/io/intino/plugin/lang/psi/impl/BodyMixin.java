package io.intino.plugin.lang.psi.impl;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.TokenType;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.IncorrectOperationException;
import io.intino.plugin.lang.psi.TaraBody;
import io.intino.plugin.lang.psi.TaraMogramReference;
import io.intino.tara.language.model.Mogram;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class BodyMixin extends ASTWrapperPsiElement {

	public BodyMixin(@NotNull ASTNode node) {
		super(node);
	}

	public void delete() throws IncorrectOperationException {
		final ASTNode parentNode = getParent().getNode();
		assert parentNode != null;
		ASTNode mogram = getNode();
		ASTNode prev = mogram.getTreePrev();
		ASTNode next = mogram.getTreeNext();
		parentNode.removeChild(mogram);
		if ((prev == null || prev.getElementType() == TokenType.WHITE_SPACE) && next != null &&
				next.getElementType() == TokenType.WHITE_SPACE) {
			parentNode.removeChild(next);
		}
	}

	public List<Mogram> getNodeLinks() {
		Mogram[] references = PsiTreeUtil.getChildrenOfType(this, TaraMogramReference.class);
		return references == null ? Collections.emptyList() : Collections.unmodifiableList(Arrays.asList(references));
	}

	public List<PsiElement> getStatements() {
		List<PsiElement> statements = new ArrayList<>();
		statements.addAll(((TaraBody) this).getVariableList());
		statements.addAll(((TaraBody) this).getVarInitList());
		statements.addAll(((TaraBody) this).getMogramList());
		statements.addAll(((TaraBody) this).getMogramReferenceList());
		return statements;
	}

}
