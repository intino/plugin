package io.intino.plugin.codeinsight.annotators.legio;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.psi.PsiElement;
import io.intino.plugin.codeinsight.annotators.TaraAnnotator;
import io.intino.plugin.codeinsight.annotators.legio.analyzers.MainClassAnalyzer;
import io.intino.plugin.file.LegioFileType;
import io.intino.plugin.project.module.ModuleProvider;
import io.intino.tara.language.model.Mogram;
import org.jetbrains.annotations.NotNull;

public class MainClassAnnotator extends TaraAnnotator {
	@Override
	public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
		if (element instanceof Mogram && element.getContainingFile().getName().endsWith("." + LegioFileType.instance().getDefaultExtension()) &&
				isPackage((Mogram) element))
			analyzeAndAnnotate(holder, new MainClassAnalyzer((Mogram) element, ModuleProvider.moduleOf(element)));
	}

	private boolean isPackage(Mogram element) {
		return element.type().equals("Package") || element.type().equals("Artifact.Package");
	}
}