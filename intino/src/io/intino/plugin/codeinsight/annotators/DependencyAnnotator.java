package io.intino.plugin.codeinsight.annotators;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.psi.PsiElement;
import io.intino.plugin.file.legio.LegioFileType;
import io.intino.plugin.project.LegioConfiguration;
import io.intino.tara.plugin.project.module.ModuleProvider;
import org.jetbrains.annotations.NotNull;
import io.intino.tara.plugin.annotator.TaraAnnotator;
import io.intino.tara.plugin.lang.psi.impl.TaraUtil;
import io.intino.tara.lang.model.Node;

public class DependencyAnnotator extends TaraAnnotator {

	@Override
	public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
		this.holder = holder;
		if (element instanceof Node && element.getContainingFile().getName().endsWith("." + LegioFileType.instance().getDefaultExtension()) &&
				isDependencies((Node) element))
			analyzeAndAnnotate(new DependencyAnalyzer(ModuleProvider.moduleOf(element),(Node) element, (LegioConfiguration) TaraUtil.configurationOf(element)));
	}

	private boolean isDependencies(Node element) {
		return element.simpleType().equals("Compile")
				|| element.simpleType().equals("Test")
				|| element.simpleType().equals("Provided")
				|| element.simpleType().equals("Runtime")
				|| element.simpleType().equals("Artifact.Imports.Compile")
				|| element.simpleType().equals("Artifact.Imports.Test")
				|| element.simpleType().equals("Artifact.Imports.Provided")
				|| element.simpleType().equals("Artifact.Imports.Runtime");
	}
}