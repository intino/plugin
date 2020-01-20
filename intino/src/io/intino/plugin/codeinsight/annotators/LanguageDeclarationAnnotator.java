package io.intino.plugin.codeinsight.annotators;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.psi.PsiElement;
import io.intino.plugin.annotator.TaraAnnotator;
import io.intino.plugin.file.legio.LegioFileType;
import io.intino.plugin.project.module.ModuleProvider;
import io.intino.tara.compiler.shared.Configuration.Artifact.Model;
import io.intino.tara.lang.model.Node;
import org.jetbrains.annotations.NotNull;

public class LanguageDeclarationAnnotator extends TaraAnnotator {

	@Override
	public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
		this.holder = holder;
		if (element instanceof Node && element.getContainingFile().getName().endsWith("." + LegioFileType.instance().getDefaultExtension()) &&
				isModelNode((Node) element))
			analyzeAndAnnotate(new LanguageDeclarationAnalyzer((Node) element, ModuleProvider.moduleOf(element)));
	}

	private boolean isModelNode(Node element) {
		//TODO check
		return element.type().replace(":", "").equals(Model.class.getSimpleName());
	}
}