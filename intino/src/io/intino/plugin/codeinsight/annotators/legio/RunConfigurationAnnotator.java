package io.intino.plugin.codeinsight.annotators.legio;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.psi.PsiElement;
import io.intino.magritte.lang.model.Node;
import io.intino.plugin.codeinsight.annotators.TaraAnnotator;
import io.intino.plugin.codeinsight.annotators.legio.analyzers.RunConfigurationAnalyzer;
import io.intino.plugin.file.LegioFileType;
import org.jetbrains.annotations.NotNull;

public class RunConfigurationAnnotator extends TaraAnnotator {

	@Override
	public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
		this.holder = holder;
		if (element instanceof Node && element.getContainingFile().getName().endsWith("." + LegioFileType.instance().getDefaultExtension()) && isRunConfiguration((Node) element))
			analyzeAndAnnotate(new RunConfigurationAnalyzer((Node) element));
	}

	private boolean isRunConfiguration(Node element) {
		return element.type().equals("RunConfiguration");
	}
}
