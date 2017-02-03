package io.intino.plugin.annotators;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.psi.PsiElement;
import io.intino.legio.Project;
import io.intino.plugin.file.legio.LegioFileType;
import org.jetbrains.annotations.NotNull;
import io.intino.tara.plugin.annotator.TaraAnnotator;
import io.intino.tara.plugin.project.module.ModuleProvider;
import io.intino.tara.lang.model.Node;

public class LanguageDeclarationAnnotator extends TaraAnnotator {

	@Override
	public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
		this.holder = holder;
		if (element instanceof Node && element.getContainingFile().getName().endsWith("." + LegioFileType.instance().getDefaultExtension()) &&
				isFactory((Node) element))
			analyzeAndAnnotate(new LanguageDeclarationAnalyzer((Node) element, ModuleProvider.moduleOf(element)));
	}

	private boolean isFactory(Node element) {
		return element.type().equals("FactoryLevel.Language") || element.simpleType().equals(factoryCanonicalName());
	}

	private String factoryCanonicalName() {
		final Class<Project.Factory> factoryClass = Project.Factory.class;
		return factoryClass.getCanonicalName().replace(factoryClass.getPackage().getName() + ".", "");
	}
}