package io.intino.plugin.codeinsight.annotators.legio;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.psi.PsiElement;
import io.intino.plugin.codeinsight.annotators.TaraAnnotator;
import io.intino.plugin.codeinsight.annotators.legio.analyzers.ArtifactParameterAnalyzer;
import io.intino.plugin.file.LegioFileType;
import io.intino.tara.language.model.Mogram;
import org.jetbrains.annotations.NotNull;

public class ArtifactParameterAnnotator extends TaraAnnotator {

	@Override
	public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
		if (element instanceof Mogram && element.getContainingFile().getName().endsWith("." + LegioFileType.instance().getDefaultExtension()) && isArtifact((Mogram) element))
			analyzeAndAnnotate(holder, new ArtifactParameterAnalyzer((Mogram) element));
	}

	private boolean isArtifact(Mogram element) {
		return element.type().equals("Parameter");
	}
}