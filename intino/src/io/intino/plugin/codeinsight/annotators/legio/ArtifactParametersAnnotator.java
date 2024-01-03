package io.intino.plugin.codeinsight.annotators.legio;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.psi.PsiElement;
import io.intino.plugin.codeinsight.annotators.TaraAnnotator;
import io.intino.plugin.codeinsight.annotators.legio.analyzers.ArtifactParametersAnalyzer;
import io.intino.plugin.file.LegioFileType;
import io.intino.tara.language.model.Mogram;
import org.jetbrains.annotations.NotNull;

public class ArtifactParametersAnnotator extends TaraAnnotator {

	@Override
	public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
		this.holder = holder;
		if (element instanceof Mogram && element.getContainingFile().getName().endsWith("." + LegioFileType.instance().getDefaultExtension()) && isArtifact((Mogram) element))
			analyzeAndAnnotate(new ArtifactParametersAnalyzer((Mogram) element));
	}

	private boolean isArtifact(Mogram element) {
		return element.type().equals("Artifact");
	}
}
