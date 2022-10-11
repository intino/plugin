package io.intino.plugin.codeinsight.annotators.legio;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.psi.PsiElement;
import io.intino.magritte.lang.model.Node;
import io.intino.plugin.codeinsight.annotators.TaraAnnotator;
import io.intino.plugin.codeinsight.annotators.legio.analyzers.BoxVersionAnalyzer;
import io.intino.plugin.file.LegioFileType;
import io.intino.plugin.project.module.ModuleProvider;
import org.jetbrains.annotations.NotNull;

public class BoxVersionAnnotator extends TaraAnnotator {
	@Override
	public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
		this.holder = holder;
		if (element instanceof Node && element.getContainingFile().getName().endsWith("." + LegioFileType.instance().getDefaultExtension()) && isBox((Node) element))
			analyzeAndAnnotate(new BoxVersionAnalyzer(ModuleProvider.moduleOf(element), (Node) element));
	}

	private boolean isBox(Node element) {
		return element.type().equals("Box") || element.type().equals("Artifact.Box");
	}
}