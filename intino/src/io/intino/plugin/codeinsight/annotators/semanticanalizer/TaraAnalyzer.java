package io.intino.plugin.codeinsight.annotators.semanticanalizer;

import com.intellij.psi.PsiElement;
import io.intino.plugin.codeinsight.annotators.TaraAnnotator;

import java.util.HashMap;
import java.util.Map;

import static io.intino.tara.language.semantics.errorcollector.SemanticNotification.Level.ERROR;

public abstract class TaraAnalyzer {

	protected Map<PsiElement, TaraAnnotator.AnnotateAndFix> results = new HashMap<>();
	private boolean hasErrors = false;

	public abstract void analyze();

	public Map<PsiElement, TaraAnnotator.AnnotateAndFix> results() {
		return results;
	}

	public boolean hasErrors() {
		if (hasErrors) return true;
		for (TaraAnnotator.AnnotateAndFix annotateAndFix : results.values())
			if (hasErrors = annotateAndFix.level().equals(ERROR)) return true;
		return false;
	}

}
