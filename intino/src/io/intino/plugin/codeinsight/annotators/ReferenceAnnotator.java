package io.intino.plugin.codeinsight.annotators;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.psi.PsiElement;
import io.intino.plugin.codeinsight.annotators.semanticanalizer.HeaderReferenceAnalyzer;
import io.intino.plugin.codeinsight.annotators.semanticanalizer.ReferenceAnalyzer;
import io.intino.plugin.lang.psi.HeaderReference;
import io.intino.plugin.lang.psi.IdentifierReference;
import io.intino.plugin.lang.psi.Rule;
import io.intino.plugin.lang.psi.TaraMethodReference;
import io.intino.plugin.lang.psi.impl.TaraPsiUtil;
import io.intino.tara.language.model.Variable;
import org.jetbrains.annotations.NotNull;

import static io.intino.tara.language.model.Primitive.*;

public class ReferenceAnnotator extends TaraAnnotator {

	@Override
	public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
		if (!(element instanceof IdentifierReference) && !(element instanceof HeaderReference)) return;
		if (element instanceof IdentifierReference && element.getContext() instanceof TaraMethodReference)
			asMethodReference(holder, (IdentifierReference) element);
		else if (element instanceof HeaderReference) asHeaderReference(holder, (HeaderReference) element);
		else asIdentifierReference(holder, (IdentifierReference) element);

	}

	private void asHeaderReference(AnnotationHolder holder, HeaderReference reference) {
		if (!reference.getIdentifierList().isEmpty())
			analyzeAndAnnotate(holder, new HeaderReferenceAnalyzer(reference));
	}

	private void asIdentifierReference(AnnotationHolder holder, IdentifierReference reference) {
		if (!reference.getIdentifierList().isEmpty() && reference.getIdentifierList().get(0).getReference() != null && !isRule(reference))
			analyzeAndAnnotate(holder, new ReferenceAnalyzer(reference));
	}

	private void asMethodReference(AnnotationHolder holder, IdentifierReference reference) {
		if (!reference.getIdentifierList().isEmpty()) analyzeAndAnnotate(holder, new ReferenceAnalyzer(reference));
	}

	private boolean isRule(IdentifierReference reference) {
		final Variable variable = TaraPsiUtil.getContainerByType(reference, Variable.class);
		return reference.getParent() instanceof Rule && variable != null &&
				!WORD.equals(variable.type()) &&
				!FUNCTION.equals(variable.type()) &&
				!OBJECT.equals(variable.type());
	}
}
