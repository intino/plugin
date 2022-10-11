package io.intino.plugin.codeinsight.annotators.legio;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.psi.PsiElement;
import io.intino.Configuration;
import io.intino.magritte.lang.model.Node;
import io.intino.plugin.codeinsight.annotators.TaraAnnotator;
import io.intino.plugin.codeinsight.annotators.legio.analyzers.ArtifactModelAnalyzer;
import io.intino.plugin.codeinsight.annotators.legio.analyzers.ArtifactModelPackageAnalyzer;
import io.intino.plugin.file.LegioFileType;
import io.intino.plugin.lang.psi.impl.IntinoUtil;
import io.intino.plugin.project.configuration.LegioConfiguration;
import io.intino.plugin.project.module.ModuleProvider;
import org.jetbrains.annotations.NotNull;

public class ArtifactModelAnnotator extends TaraAnnotator {

	@Override
	public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
		this.holder = holder;
		if (element instanceof Node && isLegioFile(element) && isModel((Node) element)) {
			Configuration configuration = IntinoUtil.configurationOf(element);
			if (!(configuration instanceof LegioConfiguration)) return;
			analyzeAndAnnotate(new ArtifactModelPackageAnalyzer((Node) element, (LegioConfiguration) configuration));
			analyzeAndAnnotate(new ArtifactModelAnalyzer((Node) element, ModuleProvider.moduleOf(element)));

		}
	}

	private boolean isLegioFile(@NotNull PsiElement element) {
		return element.getContainingFile().getName().endsWith("." + LegioFileType.instance().getDefaultExtension());
	}

	private boolean isModel(Node node) {
		return node != null && (node.type().equals("Model") || node.type().equals("Artifact.Level.Model"));
	}
}