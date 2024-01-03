package io.intino.plugin.codeinsight.annotators.legio;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.psi.PsiElement;
import io.intino.plugin.codeinsight.annotators.TaraAnnotator;
import io.intino.plugin.codeinsight.annotators.legio.analyzers.RunConfigurationAnalyzer;
import io.intino.plugin.file.LegioFileType;
import io.intino.tara.language.model.Mogram;
import org.jetbrains.annotations.NotNull;

public class RunConfigurationAnnotator extends TaraAnnotator {

	@Override
	public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
		this.holder = holder;
		if (element instanceof Mogram && element.getContainingFile().getName().endsWith("." + LegioFileType.instance().getDefaultExtension()) && isRunConfiguration((Mogram) element))
			analyzeAndAnnotate(new RunConfigurationAnalyzer((Mogram) element));
	}

	private boolean isRunConfiguration(Mogram element) {
		return element.type().equals("RunConfiguration");
	}
}
