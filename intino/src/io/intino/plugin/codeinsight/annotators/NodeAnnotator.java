package io.intino.plugin.codeinsight.annotators;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.psi.PsiElement;
import io.intino.plugin.codeinsight.annotators.semanticanalizer.ModelAnalyzer;
import io.intino.plugin.codeinsight.annotators.semanticanalizer.NodeAnalyzer;
import io.intino.plugin.codeinsight.annotators.semanticanalizer.NodeReferenceAnalyzer;
import io.intino.plugin.codeinsight.annotators.semanticanalizer.TaraAnalyzer;
import io.intino.plugin.lang.psi.TaraIdentifier;
import io.intino.plugin.lang.psi.TaraModel;
import io.intino.plugin.lang.psi.TaraMogram;
import io.intino.plugin.lang.psi.TaraMogramReference;
import io.intino.tara.language.model.Mogram;
import io.intino.tara.language.model.Tag;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

import static com.intellij.openapi.editor.colors.TextAttributesKey.createTextAttributesKey;

public class NodeAnnotator extends TaraAnnotator {

	@Override
	public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
		this.holder = holder;
		if (element instanceof TaraModel) asModel((TaraModel) element);
		else if (element instanceof Mogram && ((Mogram) element).isReference())
			asNodeReference((TaraMogramReference) element);
		else if (element instanceof Mogram) asNode((Mogram) element);
	}

	private void asNode(Mogram node) {
		TaraAnalyzer analyzer = new NodeAnalyzer(node);
		analyzeAndAnnotate(analyzer);
		if (analyzer.hasErrors()) return;
		if (node.is(Tag.Instance)) addInstanceAnnotation(node);
	}

	private void asModel(TaraModel model) {
		TaraAnalyzer analyzer = new ModelAnalyzer(model);
		analyzeAndAnnotate(analyzer);
	}

	private void asNodeReference(TaraMogramReference nodeReference) {
		TaraAnalyzer analyzer = new NodeReferenceAnalyzer(nodeReference);
		analyzeAndAnnotate(analyzer);
	}

	@SuppressWarnings("deprecation")
	private void addInstanceAnnotation(Mogram node) {
		TextAttributesKey textAttributes = createTextAttributesKey("node_instance", new TextAttributes(null, null, null, null, Font.ITALIC));
		final TaraIdentifier identifier = ((TaraMogram) node).getSignature().getIdentifier();
		if (identifier != null)
			holder.newAnnotation(HighlightSeverity.INFORMATION, "Node").range(identifier).textAttributes(textAttributes).create();
	}
}
