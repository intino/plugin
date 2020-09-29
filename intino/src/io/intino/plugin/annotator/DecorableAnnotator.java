package io.intino.plugin.annotator;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.psi.PsiElement;
import io.intino.plugin.annotator.semanticanalizer.DecorableAnalyzer;
import io.intino.plugin.annotator.semanticanalizer.TaraAnalyzer;
import io.intino.plugin.lang.psi.TaraNode;
import org.jetbrains.annotations.NotNull;

public class DecorableAnnotator extends TaraAnnotator {

	@Override
	public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
		this.holder = holder;
		if (element instanceof TaraNode) asNode((TaraNode) element);
	}

	private void asNode(TaraNode node) {
		TaraAnalyzer analyzer = new DecorableAnalyzer(node);
		analyzeAndAnnotate(analyzer);
	}
}