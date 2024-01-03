package io.intino.plugin.codeinsight.annotators.legio;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.psi.PsiElement;
import io.intino.Configuration;
import io.intino.plugin.codeinsight.annotators.TaraAnnotator;
import io.intino.plugin.codeinsight.annotators.legio.analyzers.DependencyAnalyzer;
import io.intino.plugin.file.LegioFileType;
import io.intino.plugin.lang.psi.impl.IntinoUtil;
import io.intino.plugin.project.configuration.ArtifactLegioConfiguration;
import io.intino.plugin.project.module.ModuleProvider;
import io.intino.tara.language.model.Mogram;
import org.jetbrains.annotations.NotNull;

public class DependencyAnnotator extends TaraAnnotator {
	@Override
	public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
		if (element instanceof Mogram && element.getContainingFile().getName().endsWith("." + LegioFileType.instance().getDefaultExtension()) &&
				isDependencies((Mogram) element)) {
			Configuration configuration = IntinoUtil.configurationOf(element);
			if (!(configuration instanceof ArtifactLegioConfiguration)) return;
			analyzeAndAnnotate(holder, new DependencyAnalyzer(ModuleProvider.moduleOf(element), (Mogram) element, (ArtifactLegioConfiguration) configuration));
		}
	}

	private boolean isDependencies(Mogram mogram) {
		return mogram != null &&
				("Compile".equals(mogram.type())
						|| "Test".equals(mogram.type())
						|| "Provided".equals(mogram.type())
						|| "Runtime".equals(mogram.type())
						|| "Artifact.Imports.Compile".equals(mogram.type())
						|| "Artifact.Imports.Test".equals(mogram.type())
						|| "Artifact.Imports.Provided".equals(mogram.type())
						|| "Artifact.Imports.Runtime".equals(mogram.type()));
	}
}