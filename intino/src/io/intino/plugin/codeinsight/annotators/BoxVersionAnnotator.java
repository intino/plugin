package io.intino.plugin.codeinsight.annotators;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.psi.PsiElement;
import io.intino.plugin.file.legio.LegioFileType;
import io.intino.tara.lang.model.Node;
import io.intino.tara.plugin.annotator.TaraAnnotator;
import io.intino.tara.plugin.project.module.ModuleProvider;
import org.jetbrains.annotations.NotNull;

public class BoxVersionAnnotator extends TaraAnnotator {
	@Override
	public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
		this.holder = holder;
		if (element instanceof Node && element.getContainingFile().getName().endsWith("." + LegioFileType.instance().getDefaultExtension()) && isInterface((Node) element))
			analyzeAndAnnotate(new BoxVersionAnalyzer(ModuleProvider.moduleOf(element), (Node) element));
	}

	private boolean isInterface(Node element) {
		return element.type().equals("Box") || element.type().equals("Artifact.Box");
	}
}