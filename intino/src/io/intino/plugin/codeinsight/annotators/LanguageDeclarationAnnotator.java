package io.intino.plugin.codeinsight.annotators;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.psi.PsiElement;
import io.intino.legio.graph.Artifact.Level.Model;
import io.intino.plugin.file.legio.LegioFileType;
import io.intino.tara.lang.model.Node;
import io.intino.tara.plugin.annotator.TaraAnnotator;
import io.intino.tara.plugin.project.module.ModuleProvider;
import org.jetbrains.annotations.NotNull;

public class LanguageDeclarationAnnotator extends TaraAnnotator {

	@Override
	public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
		this.holder = holder;
		if (element instanceof Node && element.getContainingFile().getName().endsWith("." + LegioFileType.instance().getDefaultExtension()) &&
				isModeling((Node) element))
			analyzeAndAnnotate(new LanguageDeclarationAnalyzer((Node) element, ModuleProvider.moduleOf(element)));
	}

	private boolean isModeling(Node element) {
		final String type = element.type().replace(":", "");
		return type.equals("Level.Model") || type.equals(modelCanonicalName());
	}

	private String modelCanonicalName() {
		return Model.class.getCanonicalName().replace(Model.class.getPackage().getName() + ".", "");
	}
}