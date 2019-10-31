package io.intino.plugin.codeinsight.annotators;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.psi.PsiElement;
import io.intino.plugin.file.legio.LegioFileType;
import io.intino.tara.lang.model.Node;
import io.intino.tara.plugin.annotator.TaraAnnotator;
import org.jetbrains.annotations.NotNull;

public class ArtifactParametersAnnotator extends TaraAnnotator {

	@Override
	public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
		this.holder = holder;
		if (element instanceof Node && element.getContainingFile().getName().endsWith("." + LegioFileType.instance().getDefaultExtension()) && isArtifact((Node) element))
			analyzeAndAnnotate(new ArtifactParametersAnalyzer((Node) element));
	}

	private boolean isArtifact(Node element) {
		return element.type().equals("Artifact");
	}
}
