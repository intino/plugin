package io.intino.plugin.annotator;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.psi.PsiElement;
import io.intino.plugin.annotator.semanticanalizer.ModelAnalyzer;
import io.intino.plugin.annotator.semanticanalizer.NodeAnalyzer;
import io.intino.plugin.annotator.semanticanalizer.NodeReferenceAnalyzer;
import io.intino.plugin.annotator.semanticanalizer.TaraAnalyzer;
import io.intino.plugin.lang.psi.TaraIdentifier;
import io.intino.plugin.lang.psi.TaraModel;
import io.intino.plugin.lang.psi.TaraNode;
import io.intino.plugin.lang.psi.TaraNodeReference;
import io.intino.tara.lang.model.Node;
import io.intino.tara.lang.model.Tag;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

import static com.intellij.openapi.editor.colors.TextAttributesKey.createTextAttributesKey;

public class NodeAnnotator extends TaraAnnotator {

	@Override
	public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
		this.holder = holder;
		if (element instanceof TaraModel) asModel((TaraModel) element);
		else if (element instanceof Node && ((Node) element).isReference())
			asNodeReference((TaraNodeReference) element);
		else if (element instanceof Node) asNode((Node) element);
	}

	private void asNode(Node node) {
		TaraAnalyzer analyzer = new NodeAnalyzer(node);
		analyzeAndAnnotate(analyzer);
		if (analyzer.hasErrors()) return;
		if (node.is(Tag.Instance)) addInstanceAnnotation(node);
	}

	private void asModel(TaraModel model) {
		TaraAnalyzer analyzer = new ModelAnalyzer(model);
		analyzeAndAnnotate(analyzer);
	}

	private void asNodeReference(TaraNodeReference nodeReference) {
		TaraAnalyzer analyzer = new NodeReferenceAnalyzer(nodeReference);
		analyzeAndAnnotate(analyzer);
	}

	@SuppressWarnings("deprecation")
	private void addInstanceAnnotation(Node node) {
		TextAttributesKey root = createTextAttributesKey("node_instance", new TextAttributes(null, null, null, null, Font.ITALIC));
		final TaraIdentifier identifier = ((TaraNode) node).getSignature().getIdentifier();
		if (identifier != null) holder.createInfoAnnotation(identifier, "node").setTextAttributes(root);
	}
}
