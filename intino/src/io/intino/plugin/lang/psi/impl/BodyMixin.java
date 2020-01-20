package io.intino.plugin.lang.psi.impl;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.TokenType;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.IncorrectOperationException;
import io.intino.plugin.lang.psi.TaraBody;
import io.intino.plugin.lang.psi.TaraNodeReference;
import io.intino.tara.lang.model.Node;
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
		ASTNode node = getNode();
		ASTNode prev = node.getTreePrev();
		ASTNode next = node.getTreeNext();
		parentNode.removeChild(node);
		if ((prev == null || prev.getElementType() == TokenType.WHITE_SPACE) && next != null &&
				next.getElementType() == TokenType.WHITE_SPACE) {
			parentNode.removeChild(next);
		}
	}

	public List<Node> getNodeLinks() {
		Node[] references = PsiTreeUtil.getChildrenOfType(this, TaraNodeReference.class);
		return references == null ? Collections.EMPTY_LIST : Collections.unmodifiableList(Arrays.asList(references));
	}

	public List<PsiElement> getStatements() {
		List<PsiElement> statements = new ArrayList<>();
		statements.addAll(((TaraBody) this).getVariableList());
		statements.addAll(((TaraBody) this).getVarInitList());
		statements.addAll(((TaraBody) this).getNodeList());
		statements.addAll(((TaraBody) this).getNodeReferenceList());
		return statements;
	}

}
