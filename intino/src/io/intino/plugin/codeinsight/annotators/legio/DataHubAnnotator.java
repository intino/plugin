package io.intino.plugin.codeinsight.annotators.legio;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.psi.PsiElement;
import io.intino.Configuration;
import io.intino.plugin.codeinsight.annotators.TaraAnnotator;
import io.intino.plugin.codeinsight.annotators.legio.analyzers.DataHubAnalyzer;
import io.intino.plugin.file.LegioFileType;
import io.intino.plugin.lang.psi.impl.IntinoUtil;
import io.intino.plugin.project.configuration.ArtifactLegioConfiguration;
import io.intino.tara.language.model.Mogram;
import org.jetbrains.annotations.NotNull;

public class DataHubAnnotator extends TaraAnnotator {
	@Override
	public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
		if (element instanceof Mogram && element.getContainingFile().getName().endsWith("." + LegioFileType.instance().getDefaultExtension()) &&
				isDataHub((Mogram) element)) {
			Configuration configuration = IntinoUtil.configurationOf(element);
			if (!(configuration instanceof ArtifactLegioConfiguration)) return;
			analyzeAndAnnotate(holder, new DataHubAnalyzer((Mogram) element, (ArtifactLegioConfiguration) configuration));
		}
	}

	private boolean isDataHub(Mogram mogram) {
		return mogram != null && (mogram.type().equals("DataHub") || mogram.type().equals("Artifact.DataHub"));
	}
}