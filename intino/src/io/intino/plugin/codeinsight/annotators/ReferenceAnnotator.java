package io.intino.plugin.codeinsight.annotators;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.psi.PsiElement;
import io.intino.magritte.lang.model.Variable;
import io.intino.plugin.codeinsight.annotators.semanticanalizer.HeaderReferenceAnalyzer;
import io.intino.plugin.codeinsight.annotators.semanticanalizer.ReferenceAnalyzer;
import io.intino.plugin.lang.psi.HeaderReference;
import io.intino.plugin.lang.psi.IdentifierReference;
import io.intino.plugin.lang.psi.Rule;
import io.intino.plugin.lang.psi.TaraMethodReference;
import io.intino.plugin.lang.psi.impl.TaraPsiUtil;
import org.jetbrains.annotations.NotNull;

import static io.intino.magritte.lang.model.Primitive.*;

public class ReferenceAnnotator extends TaraAnnotator {

	@Override
	public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
		if (!IdentifierReference.class.isInstance(element) && !HeaderReference.class.isInstance(element)) return;
		this.holder = holder;
		if (IdentifierReference.class.isInstance(element)) asIdentifierReference((IdentifierReference) element);
		else if (IdentifierReference.class.isInstance(element) && element.getContext() instanceof TaraMethodReference)
			asMethodReference((IdentifierReference) element);
		else if (HeaderReference.class.isInstance(element)) asHeaderReference((HeaderReference) element);
	}

	private void asHeaderReference(HeaderReference reference) {
		if (!reference.getIdentifierList().isEmpty()) analyzeAndAnnotate(new HeaderReferenceAnalyzer(reference));
	}

	private void asIdentifierReference(IdentifierReference reference) {
		if (!reference.getIdentifierList().isEmpty() && reference.getIdentifierList().get(0).getReference() != null && !isRule(reference))
			analyzeAndAnnotate(new ReferenceAnalyzer(reference));
	}

	private void asMethodReference(IdentifierReference reference) {
		if (!reference.getIdentifierList().isEmpty()) analyzeAndAnnotate(new ReferenceAnalyzer(reference));
	}

	private boolean isRule(IdentifierReference reference) {
		final Variable variable = TaraPsiUtil.getContainerByType(reference, Variable.class);
		return reference.getParent() instanceof Rule && variable != null &&
				!WORD.equals(variable.type()) &&
				!FUNCTION.equals(variable.type()) &&
				!OBJECT.equals(variable.type());
	}
}
