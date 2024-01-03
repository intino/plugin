package io.intino.plugin.codeinsight.annotators;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.psi.PsiElement;
import io.intino.plugin.codeinsight.annotators.semanticanalizer.DecorableAnalyzer;
import io.intino.plugin.codeinsight.annotators.semanticanalizer.TaraAnalyzer;
import io.intino.plugin.lang.psi.TaraMogram;
import org.jetbrains.annotations.NotNull;

public class DecorableAnnotator extends TaraAnnotator {

	@Override
	public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
		this.holder = holder;
		if (element instanceof TaraMogram) asNode((TaraMogram) element);
	}

	private void asNode(TaraMogram node) {
		TaraAnalyzer analyzer = new DecorableAnalyzer(node);
		analyzeAndAnnotate(analyzer);
	}
}