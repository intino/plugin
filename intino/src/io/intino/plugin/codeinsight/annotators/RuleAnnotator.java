package io.intino.plugin.codeinsight.annotators;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.psi.PsiElement;
import io.intino.plugin.codeinsight.annotators.semanticanalizer.MogramRuleAnalyzer;
import io.intino.plugin.codeinsight.annotators.semanticanalizer.TaraAnalyzer;
import io.intino.plugin.codeinsight.annotators.semanticanalizer.VariableRuleClassAnalyzer;
import io.intino.plugin.lang.psi.TaraRuleContainer;
import io.intino.plugin.lang.psi.impl.TaraPsiUtil;
import io.intino.tara.language.model.Variable;
import org.jetbrains.annotations.NotNull;

import static io.intino.tara.language.model.Primitive.OBJECT;

public class RuleAnnotator extends TaraAnnotator {

	@Override
	public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
		final Variable variable = TaraPsiUtil.getContainerByType(element, Variable.class);
		if (element instanceof TaraRuleContainer ruleContainer && (variable == null || !OBJECT.equals(variable.type())))
			analyzeAndAnnotate(holder, analyzer(ruleContainer));
	}

	@NotNull
	private static TaraAnalyzer analyzer(TaraRuleContainer ruleContainer) {
		return TaraPsiUtil.getContainerByType(ruleContainer, Variable.class) != null ?
				new VariableRuleClassAnalyzer(ruleContainer) :
				new MogramRuleAnalyzer(ruleContainer);
	}
}