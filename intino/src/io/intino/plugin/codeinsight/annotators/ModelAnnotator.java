package io.intino.plugin.codeinsight.annotators;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.psi.PsiElement;
import io.intino.Configuration;
import io.intino.magritte.lang.model.Node;
import io.intino.plugin.annotator.TaraAnnotator;
import io.intino.plugin.file.LegioFileType;
import io.intino.plugin.lang.psi.impl.IntinoUtil;
import io.intino.plugin.project.configuration.LegioConfiguration;
import org.jetbrains.annotations.NotNull;

public class ModelAnnotator extends TaraAnnotator {
	@Override
	public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
		this.holder = holder;
		if (element instanceof Node && isLegioFile(element) && isModel((Node) element)) {
			Configuration configuration = IntinoUtil.configurationOf(element);
			if (!(configuration instanceof LegioConfiguration)) return;
			analyzeAndAnnotate(new ModelAnalyzer((Node) element, (LegioConfiguration) configuration));
		}
	}

	private boolean isLegioFile(@NotNull PsiElement element) {
		return element.getContainingFile().getName().endsWith("." + LegioFileType.instance().getDefaultExtension());
	}

	private boolean isModel(Node node) {
		return node != null && (node.type().equals("Model") || node.type().equals("Artifact.Level.Model"));
	}
}