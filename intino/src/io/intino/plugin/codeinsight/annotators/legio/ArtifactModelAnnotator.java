package io.intino.plugin.codeinsight.annotators.legio;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.psi.PsiElement;
import io.intino.Configuration;
import io.intino.plugin.codeinsight.annotators.TaraAnnotator;
import io.intino.plugin.codeinsight.annotators.legio.analyzers.ArtifactModelAnalyzer;
import io.intino.plugin.codeinsight.annotators.legio.analyzers.ArtifactModelPackageAnalyzer;
import io.intino.plugin.file.LegioFileType;
import io.intino.plugin.lang.psi.impl.IntinoUtil;
import io.intino.plugin.project.configuration.ArtifactLegioConfiguration;
import io.intino.plugin.project.module.ModuleProvider;
import io.intino.tara.language.model.Mogram;
import org.jetbrains.annotations.NotNull;

public class ArtifactModelAnnotator extends TaraAnnotator {

	@Override
	public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
		this.holder = holder;
		if (element instanceof Mogram && isLegioFile(element) && isModel((Mogram) element)) {
			Configuration configuration = IntinoUtil.configurationOf(element);
			if (!(configuration instanceof ArtifactLegioConfiguration)) return;
			analyzeAndAnnotate(new ArtifactModelPackageAnalyzer((Mogram) element, (ArtifactLegioConfiguration) configuration));
			analyzeAndAnnotate(new ArtifactModelAnalyzer((Mogram) element, ModuleProvider.moduleOf(element)));

		}
	}

	private boolean isLegioFile(@NotNull PsiElement element) {
		return element.getContainingFile().getName().endsWith("." + LegioFileType.instance().getDefaultExtension());
	}

	private boolean isModel(Mogram mogram) {
		return mogram != null && (mogram.type().equals("Model") || mogram.type().equals("Artifact.Model"));
	}
}