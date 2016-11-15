package io.intino.legio.plugin.annotators;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.psi.PsiElement;
import io.intino.legio.plugin.file.LegioFileType;
import io.intino.legio.plugin.project.LegioConfiguration;
import org.jetbrains.annotations.NotNull;
import tara.compiler.model.Model;
import tara.intellij.annotator.TaraAnnotator;
import tara.intellij.lang.psi.TaraModel;
import tara.intellij.lang.psi.impl.TaraUtil;

public class DependencyAnnotator extends TaraAnnotator {

	@Override
	public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
		if (element instanceof TaraModel && !((TaraModel) element).getName().endsWith("." + LegioFileType.instance().getDefaultExtension()))
			analyzeAndAnnotate(new DependencyAnalyzer((Model) element, (LegioConfiguration) TaraUtil.configurationOf(element)));
	}
}
