package io.intino.plugin.annotator;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.psi.PsiElement;
import io.intino.plugin.annotator.semanticanalizer.HeaderReferenceAnalyzer;
import io.intino.plugin.annotator.semanticanalizer.ReferenceAnalyzer;
import io.intino.plugin.lang.psi.HeaderReference;
import io.intino.plugin.lang.psi.IdentifierReference;
import io.intino.plugin.lang.psi.Rule;
import io.intino.plugin.lang.psi.TaraMethodReference;
import io.intino.plugin.lang.psi.impl.TaraPsiUtil;
import io.intino.tara.lang.model.Variable;
import org.jetbrains.annotations.NotNull;

import static io.intino.tara.lang.model.Primitive.*;

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
