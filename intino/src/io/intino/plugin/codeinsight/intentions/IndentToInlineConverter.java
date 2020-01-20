package io.intino.plugin.codeinsight.intentions;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.IncorrectOperationException;
import io.intino.plugin.lang.TaraLanguage;
import io.intino.plugin.lang.psi.Body;
import io.intino.plugin.lang.psi.TaraBody;
import io.intino.plugin.lang.psi.TaraNode;
import io.intino.plugin.lang.psi.TaraTypes;
import io.intino.plugin.lang.psi.impl.TaraElementFactoryImpl;
import io.intino.plugin.lang.psi.impl.TaraPsiUtil;
import io.intino.tara.lang.model.Node;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static com.intellij.psi.TokenType.NEW_LINE_INDENT;

public class IndentToInlineConverter extends PsiElementBaseIntentionAction implements IntentionAction {
	@Override
	public void invoke(@NotNull Project project, Editor editor, @NotNull PsiElement element) throws IncorrectOperationException {
		PsiElement toReplace = getTargetElement(element);
		List<PsiElement> replaced = new ArrayList<>();
		if (toReplace == null) return;
		TaraElementFactoryImpl factory = new TaraElementFactoryImpl(project);
		Body body = TaraPsiUtil.getBodyContextOf(element);
		propagateIndents(replaced, factory, body);
		addSpaces(factory, replaced);
	}

	private void propagateIndents(List<PsiElement> replaced, TaraElementFactoryImpl factory, Body body) {
		for (LeafPsiElement leaf : PsiTreeUtil.getChildrenOfTypeAsList(body, LeafPsiElement.class)) {
			if (is(leaf, TaraTypes.NEWLINE))
				replaced.add(leaf.replace(factory.createInlineNewLine()));
			if (is(leaf, TaraTypes.NEW_LINE_INDENT))
				replaced.add(leaf.replace(factory.createInlineNewLineIndent()));
		}
		for (Node node : body.getNodeList()) {
			final TaraBody nodeBody = ((TaraNode) node).getBody();
			if (nodeBody != null) propagateIndents(replaced, factory, nodeBody);
		}
	}

	private PsiElement getTargetElement(PsiElement element) {
		PsiElement previous = element.getPrevSibling() != null ? element.getPrevSibling() : element.getParent().getPrevSibling();
		if (is(previous, NEW_LINE_INDENT))
			return previous;
		if (is(previous, TaraTypes.NEWLINE) && element.getParent().getParent() instanceof Body)
			return element.getParent().getParent().getFirstChild();
		if (previous == null)
			previous = ((PsiElement) TaraPsiUtil.getContainerOf(element)).getPrevSibling();
		return previous;
	}

	private void addSpaces(TaraElementFactoryImpl factory, List<PsiElement> replaced) {
		for (PsiElement replace : replaced) {
			replace.getParent().addAfter(factory.createWhiteSpace(), replace);
			replace.getParent().addBefore(factory.createWhiteSpace(), replace);
		}
	}

	@NotNull
	public String getText() {
		return "To inline statement";
	}

	@Override
	public boolean isAvailable(@NotNull Project project, Editor editor, @NotNull PsiElement element) {
		if (!element.isWritable()) return false;
		PsiElement toCheck = getReplacingElement(element);
		return toCheck != null && !toCheck.getText().contains(">");
	}

	private PsiElement getReplacingElement(PsiElement element) {
		if (is(element, NEW_LINE_INDENT) || (is(element, TaraTypes.NEWLINE) && element.getParent().getParent() instanceof Body))
			return element;
		PsiElement previous = calculatePrevious(element);
		if (is(previous, NEW_LINE_INDENT) || (is(previous, TaraTypes.NEWLINE) && element.getParent().getParent() instanceof Body))
			return previous;
		return null;
	}

	@Nullable
	private PsiElement calculatePrevious(PsiElement element) {
		PsiElement previous = element.getPrevSibling() != null ? element.getPrevSibling() : element.getParent().getPrevSibling();
		if (previous == null) {
			PsiElement contextOf = (PsiElement) TaraPsiUtil.getContainerOf(element);
			if (contextOf != null) previous = contextOf.getPrevSibling();
		}
		return previous;
	}

	private boolean is(PsiElement element, IElementType type) {
		return !(element == null || !element.getLanguage().is(TaraLanguage.INSTANCE)) && element.getNode().getElementType().equals(type);
	}

	@Override
	public boolean startInWriteAction() {
		return true;
	}

	@NotNull
	@Override
	public String getFamilyName() {
		return getText();
	}
}
