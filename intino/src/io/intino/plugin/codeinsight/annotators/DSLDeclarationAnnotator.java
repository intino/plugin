package io.intino.plugin.codeinsight.annotators;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.psi.PsiElement;
import io.intino.plugin.codeinsight.annotators.semanticanalizer.DSLDeclarationAnalyzer;
import io.intino.plugin.lang.file.StashFileType;
import io.intino.plugin.lang.file.TaraFileType;
import io.intino.plugin.lang.psi.TaraModel;
import org.jetbrains.annotations.NotNull;

public class DSLDeclarationAnnotator extends TaraAnnotator {

	@Override
	public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
		this.holder = holder;
		if (element instanceof TaraModel && !((TaraModel) element).getName().endsWith("." + StashFileType.instance().getDefaultExtension() + "." + TaraFileType.instance().getDefaultExtension()))
			analyzeAndAnnotate(new DSLDeclarationAnalyzer((TaraModel) element));
	}
}
