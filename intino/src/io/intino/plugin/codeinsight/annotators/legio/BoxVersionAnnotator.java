package io.intino.plugin.codeinsight.annotators.legio;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.psi.PsiElement;
import io.intino.plugin.codeinsight.annotators.TaraAnnotator;
import io.intino.plugin.codeinsight.annotators.legio.analyzers.BoxVersionAnalyzer;
import io.intino.plugin.file.LegioFileType;
import io.intino.plugin.project.module.ModuleProvider;
import io.intino.tara.language.model.Mogram;
import org.jetbrains.annotations.NotNull;

public class BoxVersionAnnotator extends TaraAnnotator {
	@Override
	public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
		this.holder = holder;
		if (element instanceof Mogram && element.getContainingFile().getName().endsWith("." + LegioFileType.instance().getDefaultExtension()) && isBox((Mogram) element))
			analyzeAndAnnotate(new BoxVersionAnalyzer(ModuleProvider.moduleOf(element), (Mogram) element));
	}

	private boolean isBox(Mogram element) {
		return element.type().equals("Box") || element.type().equals("Artifact.Box");
	}
}