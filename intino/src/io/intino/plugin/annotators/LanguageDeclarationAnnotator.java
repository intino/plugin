package io.intino.plugin.annotators;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.psi.PsiElement;
import io.intino.legio.Artifact;
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
				isFactory((Node) element))
			analyzeAndAnnotate(new LanguageDeclarationAnalyzer((Node) element, ModuleProvider.moduleOf(element)));
	}

	private boolean isFactory(Node element) {
		return element.type().equals("Modeling.Language") || element.simpleType().equals(factoryCanonicalName());
	}

	private String factoryCanonicalName() {
		return Artifact.Modeling.class.getCanonicalName().replace(Artifact.Modeling.class.getPackage().getName() + ".", "");
	}
}