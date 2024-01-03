package io.intino.plugin.codeinsight.annotators.semanticanalizer;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.openapi.module.Module;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import io.intino.plugin.codeinsight.annotators.TaraAnnotator;
import io.intino.plugin.codeinsight.annotators.fix.CreateNodeRuleClassIntention;
import io.intino.plugin.codeinsight.languageinjection.helpers.Format;
import io.intino.plugin.lang.psi.Rule;
import io.intino.plugin.lang.psi.TaraRuleContainer;
import io.intino.plugin.lang.psi.impl.IntinoUtil;
import io.intino.plugin.lang.psi.impl.TaraPsiUtil;
import io.intino.plugin.messages.MessageProvider;
import io.intino.plugin.project.module.ModuleProvider;
import io.intino.tara.language.model.Mogram;
import io.intino.tara.language.model.Tag;
import io.intino.tara.language.model.rules.custom.Url;

import static com.intellij.psi.search.GlobalSearchScope.moduleScope;
import static io.intino.plugin.highlighting.TaraSyntaxHighlighter.UNRESOLVED_ACCESS;
import static io.intino.tara.dsls.MetaIdentifiers.FACET;
import static io.intino.tara.dsls.MetaIdentifiers.META_FACET;
import static io.intino.tara.language.semantics.errorcollector.SemanticNotification.Level.ERROR;

public class NodeRuleAnalyzer extends TaraAnalyzer {
	private static final String RULES_PACKAGE = ".rules.";
	private final String rulesPackage;
	private final Rule rule;
	private final Mogram node;

	public NodeRuleAnalyzer(TaraRuleContainer ruleContainer) {
		this.node = TaraPsiUtil.getContainerByType(ruleContainer, Mogram.class);
		this.rule = ruleContainer.getRule();
		this.rulesPackage = IntinoUtil.modelPackage(ruleContainer).toLowerCase() + RULES_PACKAGE;
	}

	@Override
	public void analyze() {
		if (rule == null) error();
		else if (node.is(Tag.Instance)) instanceError();
		else analyzeNodeRule();
	}

	private void analyzeNodeRule() {
		if (rule.isLambda()) {
			if (node.type().equalsIgnoreCase(FACET) || node.type().equalsIgnoreCase(META_FACET)) facetError();
		} else {
			final Module module = module();
			if (module != null) {
				PsiClass aClass = JavaPsiFacade.getInstance(rule.getProject()).findClass(rulesPackage + rule.getText(), moduleScope(module));
				if (aClass == null && !isProvided()) error();
			}
		}
	}

	private void facetError() {
		results.put(rule, new TaraAnnotator.AnnotateAndFix(ERROR, MessageProvider.message("reject.aspect.with.size.constraint")));
	}

	private void instanceError() {
		results.put(rule, new TaraAnnotator.AnnotateAndFix(ERROR, MessageProvider.message("reject.instance.node.with.rule"), UNRESOLVED_ACCESS));
	}

	private boolean isProvided() {
		try {
			return Class.forName(Url.class.getPackage().getName() + "." + Format.reference().format(rule.getText())) != null;
		} catch (ClassNotFoundException e) {
			return false;
		}
	}

	private Module module() {
		if (rule == null) return null;
		return ModuleProvider.moduleOf(rule.getContainingFile());
	}

	private void error() {
		if (rule == null)
			results.put((PsiElement) node, new TaraAnnotator.AnnotateAndFix(ERROR, MessageProvider.message("error.link.to.rule"), UNRESOLVED_ACCESS));
		else
			results.put(rule, new TaraAnnotator.AnnotateAndFix(ERROR, MessageProvider.message("error.link.to.rule"), UNRESOLVED_ACCESS, collectFixes()));
	}

	private IntentionAction[] collectFixes() {
		if (rule == null) return new IntentionAction[0];
		return new IntentionAction[]{new CreateNodeRuleClassIntention(rule)};
	}
}