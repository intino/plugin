package io.intino.plugin.codeinsight.annotators;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.psi.PsiElement;
import io.intino.Configuration;
import io.intino.magritte.lang.model.Node;
import io.intino.plugin.annotator.TaraAnnotator;
import io.intino.plugin.file.legio.LegioFileType;
import io.intino.plugin.lang.psi.impl.IntinoUtil;
import io.intino.plugin.project.LegioConfiguration;
import org.jetbrains.annotations.NotNull;

public class DataHubAnnotator extends TaraAnnotator {
	@Override
	public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
		this.holder = holder;
		if (element instanceof Node && element.getContainingFile().getName().endsWith("." + LegioFileType.instance().getDefaultExtension()) &&
				isDataHub((Node) element)) {
			Configuration configuration = IntinoUtil.configurationOf(element);
			if (!(configuration instanceof LegioConfiguration)) return;
			analyzeAndAnnotate(new DataHubAnalyzer((Node) element, (LegioConfiguration) configuration));
		}
	}

	private boolean isDataHub(Node node) {
		return node != null && (node.type().equals("DataHub") || node.type().equals("Artifact.DataHub"));
	}
}