package io.intino.plugin.annotator;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.psi.PsiElement;
import io.intino.magritte.lang.model.Variable;
import io.intino.plugin.annotator.semanticanalizer.NodeRuleAnalyzer;
import io.intino.plugin.annotator.semanticanalizer.VariableRuleClassAnalyzer;
import io.intino.plugin.lang.psi.TaraRuleContainer;
import io.intino.plugin.lang.psi.impl.TaraPsiUtil;
import org.jetbrains.annotations.NotNull;

import static io.intino.magritte.lang.model.Primitive.OBJECT;

public class RuleAnnotator extends TaraAnnotator {

	@Override
	public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
		this.holder = holder;
		final Variable variable = TaraPsiUtil.getContainerByType(element, Variable.class);
		if (!(element instanceof TaraRuleContainer) || (variable != null && OBJECT.equals(variable.type()))) return;
		TaraRuleContainer ruleContainer = (TaraRuleContainer) element;
		analyzeAndAnnotate(TaraPsiUtil.getContainerByType(ruleContainer, Variable.class) != null ?
				new VariableRuleClassAnalyzer(ruleContainer) : new NodeRuleAnalyzer(ruleContainer));
	}
}