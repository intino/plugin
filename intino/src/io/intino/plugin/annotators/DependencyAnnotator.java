package io.intino.plugin.annotators;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.psi.PsiElement;
import io.intino.plugin.file.legio.LegioFileType;
import io.intino.plugin.project.LegioConfiguration;
import org.jetbrains.annotations.NotNull;
import tara.intellij.annotator.TaraAnnotator;
import tara.intellij.lang.psi.impl.TaraUtil;
import tara.lang.model.Node;

public class DependencyAnnotator extends TaraAnnotator {

	@Override
	public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
		this.holder = holder;
		if (element instanceof Node && element.getContainingFile().getName().endsWith("." + LegioFileType.instance().getDefaultExtension()) &&
				isDependencies((Node) element))
			analyzeAndAnnotate(new DependencyAnalyzer((Node) element, (LegioConfiguration) TaraUtil.configurationOf(element)));
	}

	private boolean isDependencies(Node element) {
		return element.simpleType().equals("Compile")
				|| element.simpleType().equals("Test")
				|| element.simpleType().equals("Provided")
				|| element.simpleType().equals("Runtime")
				|| element.simpleType().equals("Project.Dependencies.Compile")
				|| element.simpleType().equals("Project.Dependencies.Test")
				|| element.simpleType().equals("Project.Dependencies.Provided")
				|| element.simpleType().equals("Project.Dependencies.Runtime");
	}
}