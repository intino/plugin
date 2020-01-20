package io.intino.plugin.codefolding;

import com.intellij.lang.ASTNode;
import com.intellij.lang.folding.CustomFoldingBuilder;
import com.intellij.lang.folding.FoldingDescriptor;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import io.intino.plugin.lang.psi.TaraNode;
import io.intino.plugin.lang.psi.TaraStringValue;
import io.intino.plugin.lang.psi.TaraValue;
import io.intino.plugin.lang.psi.impl.TaraModelImpl;
import io.intino.plugin.lang.psi.impl.TaraUtil;
import io.intino.tara.lang.model.Node;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class TaraFoldingBuilder extends CustomFoldingBuilder {


	@Override
	protected void buildLanguageFoldRegions(@NotNull List<FoldingDescriptor> descriptors,
											@NotNull PsiElement root,
											@NotNull Document document,
											boolean quick) {
		for (final Node node : TaraUtil.getAllNodesOfFile((TaraModelImpl) root)) {
			processNode(descriptors, node);
			processStrings(descriptors, node);
		}
	}

	@Override
	protected boolean isRegionCollapsedByDefault(@NotNull ASTNode node) {
		final PsiElement value = node.getPsi().getParent();
		return value instanceof TaraStringValue ||
				(value instanceof TaraValue && ((TaraValue) value).values().size() >= StringFoldingBuilder.VALUE_MAX_SIZE);
	}

	@Override
	protected boolean isCustomFoldingRoot(ASTNode astNode) {
		return astNode.getPsi() instanceof Node;
	}


	@Override
	protected String getLanguagePlaceholderText(@NotNull ASTNode node, @NotNull TextRange range) {
		return " ...";
	}

	private void processNode(@NotNull List<FoldingDescriptor> descriptors, final Node node) {
		if (((TaraNode) node).getText() != null && ((TaraNode) node).getBody() != null)
			descriptors.add(new FoldingDescriptor(((TaraNode) node).getBody().getNode(), getRange(node)) {
				public String getPlaceholderText() {
					return buildNodeHolderText(node);
				}
			});
	}

	private void processStrings(@NotNull List<FoldingDescriptor> descriptors, Node node) {
		final StringFoldingBuilder builder = new StringFoldingBuilder();
		builder.processMultiLineValues(descriptors, node);
		builder.processMultiValuesParameters(descriptors, node);
	}

	private String buildNodeHolderText(Node node) {
		StringBuilder text = new StringBuilder();
		node.components().stream().filter(inner -> inner.name() != null).forEach(inner -> text.append(" ").append(inner.name()));
		return text.toString();
	}

	private TextRange getRange(Node node) {
		return new TextRange(((TaraNode) node).getBody().getTextRange().getStartOffset(), ((TaraNode) node).getTextRange().getEndOffset());
	}

	private TextRange getRange(PsiElement value) {
		return new TextRange(value.getTextRange().getStartOffset(), lastNoText(value));
	}

	private int lastNoText(PsiElement value) {
		return value.getTextRange().getEndOffset() - (value.getText().length() - value.getText().trim().length());
	}
}