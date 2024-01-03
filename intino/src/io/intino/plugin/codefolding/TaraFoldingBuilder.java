package io.intino.plugin.codefolding;

import com.intellij.lang.ASTNode;
import com.intellij.lang.folding.CustomFoldingBuilder;
import com.intellij.lang.folding.FoldingDescriptor;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import io.intino.plugin.lang.psi.TaraMogram;
import io.intino.plugin.lang.psi.TaraStringValue;
import io.intino.plugin.lang.psi.TaraValue;
import io.intino.plugin.lang.psi.impl.IntinoUtil;
import io.intino.plugin.lang.psi.impl.TaraModelImpl;
import io.intino.tara.language.model.Mogram;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class TaraFoldingBuilder extends CustomFoldingBuilder {


	@Override
	protected void buildLanguageFoldRegions(@NotNull List<FoldingDescriptor> descriptors,
											@NotNull PsiElement root,
											@NotNull Document document,
											boolean quick) {
		for (final Mogram node : IntinoUtil.getAllNodesOfFile((TaraModelImpl) root)) {
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
		return astNode.getPsi() instanceof Mogram;
	}


	@Override
	protected String getLanguagePlaceholderText(@NotNull ASTNode node, @NotNull TextRange range) {
		return " ...";
	}

	private void processNode(@NotNull List<FoldingDescriptor> descriptors, final Mogram node) {
		if (((TaraMogram) node).getText() != null && ((TaraMogram) node).getBody() != null)
			descriptors.add(new FoldingDescriptor(((TaraMogram) node).getBody().getNode(), getRange(node)) {
				public String getPlaceholderText() {
					return buildNodeHolderText(node);
				}
			});
	}

	private void processStrings(@NotNull List<FoldingDescriptor> descriptors, Mogram node) {
		final StringFoldingBuilder builder = new StringFoldingBuilder();
		builder.processMultiLineValues(descriptors, node);
		builder.processMultiValuesParameters(descriptors, node);
	}

	private String buildNodeHolderText(Mogram node) {
		StringBuilder text = new StringBuilder();
		node.components().stream().filter(inner -> inner.name() != null).forEach(inner -> text.append(" ").append(inner.name()));
		return text.toString();
	}

	private TextRange getRange(Mogram node) {
		return new TextRange(((TaraMogram) node).getBody().getTextRange().getStartOffset(), ((TaraMogram) node).getTextRange().getEndOffset());
	}
}