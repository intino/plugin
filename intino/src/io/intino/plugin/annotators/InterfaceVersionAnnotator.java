package io.intino.plugin.annotators;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.psi.PsiElement;
import io.intino.plugin.file.legio.LegioFileType;
import io.intino.plugin.project.LegioConfiguration;
import io.intino.tara.lang.model.Node;
import io.intino.tara.plugin.annotator.TaraAnnotator;
import io.intino.tara.plugin.lang.psi.impl.TaraUtil;
import org.jetbrains.annotations.NotNull;

public class InterfaceVersionAnnotator extends TaraAnnotator {

	@Override
	public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
		this.holder = holder;
		if (element instanceof Node && element.getContainingFile().getName().endsWith("." + LegioFileType.instance().getDefaultExtension()) && isInterface((Node) element))
			analyzeAndAnnotate(new InterfaceVersionAnalyzer((Node) element, (LegioConfiguration) TaraUtil.configurationOf(element)));
	}

	private boolean isInterface(Node element) {
		return element.simpleType().equals("Interface") || element.simpleType().equals("Project.Factory.Interface");
	}
}
