package io.intino.plugin.codeinsight.annotators;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.psi.PsiElement;
import io.intino.Configuration;
import io.intino.magritte.lang.model.Node;
import io.intino.plugin.annotator.TaraAnnotator;
import io.intino.plugin.file.LegioFileType;
import io.intino.plugin.lang.psi.impl.IntinoUtil;
import io.intino.plugin.project.configuration.LegioConfiguration;
import io.intino.plugin.project.module.ModuleProvider;
import org.jetbrains.annotations.NotNull;

public class DependencyAnnotator extends TaraAnnotator {
	@Override
	public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
		this.holder = holder;
		if (element instanceof Node && element.getContainingFile().getName().endsWith("." + LegioFileType.instance().getDefaultExtension()) &&
				isDependencies((Node) element)) {
			Configuration configuration = IntinoUtil.configurationOf(element);
			if (!(configuration instanceof LegioConfiguration)) return;
			analyzeAndAnnotate(new DependencyAnalyzer(ModuleProvider.moduleOf(element), (Node) element, (LegioConfiguration) configuration));
		}
	}

	private boolean isDependencies(Node node) {
		return node != null &&
				("Compile".equals(node.type())
						|| "Test".equals(node.type())
						|| "Provided".equals(node.type())
						|| "Runtime".equals(node.type())
						|| "Artifact.Imports.Compile".equals(node.type())
						|| "Artifact.Imports.Test".equals(node.type())
						|| "Artifact.Imports.Provided".equals(node.type())
						|| "Artifact.Imports.Runtime".equals(node.type()));
	}
}