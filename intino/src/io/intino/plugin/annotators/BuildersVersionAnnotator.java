package io.intino.plugin.annotators;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.psi.PsiElement;
import io.intino.plugin.file.legio.LegioFileType;
import io.intino.plugin.project.LegioConfiguration;
import org.jetbrains.annotations.NotNull;
import tara.intellij.annotator.TaraAnnotator;
import tara.intellij.lang.psi.impl.TaraUtil;
import tara.lang.model.Node;

public class BuildersVersionAnnotator extends TaraAnnotator {

	@Override
	public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
		this.holder = holder;
		if (element instanceof Node && isLegioFile(element) && isBuilder((Node) element))
			analyzeAndAnnotate(new BuildersVersionAnalyzer((Node) element, (LegioConfiguration) TaraUtil.configurationOf(element)));
	}

	private boolean isLegioFile(@NotNull PsiElement element) {
		return element.getContainingFile().getName().endsWith("." + LegioFileType.instance().getDefaultExtension());
	}

	private boolean isBuilder(Node element) {
		return element.simpleType().equals("Interface")
				|| element.simpleType().equals("Behavior")
				|| element.simpleType().equals("Project.Factory.Interface")
				|| element.simpleType().equals("Project.Factory.Behavior");
	}
}