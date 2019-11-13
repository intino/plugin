package io.intino.plugin.codeinsight.annotators;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.psi.PsiElement;
import io.intino.plugin.file.legio.LegioFileType;
import io.intino.plugin.project.LegioConfiguration;
import io.intino.tara.compiler.shared.Configuration;
import io.intino.tara.lang.model.Node;
import io.intino.tara.plugin.annotator.TaraAnnotator;
import io.intino.tara.plugin.lang.psi.impl.TaraUtil;
import io.intino.tara.plugin.project.module.ModuleProvider;
import org.jetbrains.annotations.NotNull;

public class DependencyAnnotator extends TaraAnnotator {
	@Override
	public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
		this.holder = holder;
		if (element instanceof Node && element.getContainingFile().getName().endsWith("." + LegioFileType.instance().getDefaultExtension()) &&
				isDependencies((Node) element)) {
			Configuration configuration = TaraUtil.configurationOf(element);
			if (!(configuration instanceof LegioConfiguration)) return;
			analyzeAndAnnotate(new DependencyAnalyzer(ModuleProvider.moduleOf(element), (Node) element, (LegioConfiguration) configuration));
		}
	}

	private boolean isDependencies(Node element) {
		return element.type().equals("Compile")
				|| element.type().equals("Test")
				|| element.type().equals("Provided")
				|| element.type().equals("Runtime")
				|| element.type().equals("Artifact.Imports.Compile")
				|| element.type().equals("Artifact.Imports.Test")
				|| element.type().equals("Artifact.Imports.Provided")
				|| element.type().equals("Artifact.Imports.Runtime");
	}
}