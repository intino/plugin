package io.intino.plugin.codeinsight.annotators.legio;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.psi.PsiElement;
import io.intino.magritte.lang.model.Node;
import io.intino.plugin.codeinsight.annotators.TaraAnnotator;
import io.intino.plugin.codeinsight.annotators.legio.analyzers.ArtifactParametersAnalyzer;
import io.intino.plugin.file.LegioFileType;
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
