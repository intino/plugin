package io.intino.plugin.codeinsight.annotators;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.psi.PsiElement;
import io.intino.plugin.file.legio.LegioFileType;
import io.intino.tara.lang.model.Node;
import io.intino.tara.plugin.annotator.TaraAnnotator;
import org.jetbrains.annotations.NotNull;

public class BoxVersionAnnotator extends TaraAnnotator {

	@Override
	public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
		this.holder = holder;
		if (element instanceof Node && element.getContainingFile().getName().endsWith("." + LegioFileType.instance().getDefaultExtension()) && isInterface((Node) element))
			analyzeAndAnnotate(new BoxVersionAnalyzer((Node) element));
	}

	private boolean isInterface(Node element) {
		return element.simpleType().equals("Box") || element.simpleType().equals("Artifact.Box");
	}
}