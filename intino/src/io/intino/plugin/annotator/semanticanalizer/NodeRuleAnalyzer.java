package io.intino.plugin.annotator.semanticanalizer;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.openapi.module.Module;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import io.intino.magritte.dsl.ProteoConstants;
import io.intino.magritte.lang.model.Node;
import io.intino.magritte.lang.model.Tag;
import io.intino.magritte.lang.model.rules.custom.Url;
import io.intino.plugin.annotator.TaraAnnotator;
import io.intino.plugin.annotator.fix.CreateNodeRuleClassIntention;
import io.intino.plugin.codeinsight.languageinjection.helpers.Format;
import io.intino.plugin.lang.psi.Rule;
import io.intino.plugin.lang.psi.TaraRuleContainer;
import io.intino.plugin.lang.psi.impl.IntinoUtil;
import io.intino.plugin.lang.psi.impl.TaraPsiUtil;
import io.intino.plugin.messages.MessageProvider;
import io.intino.plugin.project.module.ModuleProvider;

import static com.intellij.psi.search.GlobalSearchScope.moduleScope;
import static io.intino.magritte.lang.semantics.errorcollector.SemanticNotification.Level.ERROR;
import static io.intino.plugin.highlighting.TaraSyntaxHighlighter.UNRESOLVED_ACCESS;

public class NodeRuleAnalyzer extends TaraAnalyzer {

	private static final String RULES_PACKAGE = ".rules.";
	private final String rulesPackage;
	private final Rule rule;
	private final Node node;

	public NodeRuleAnalyzer(TaraRuleContainer ruleContainer) {
		this.node = TaraPsiUtil.getContainerByType(ruleContainer, Node.class);
		this.rule = ruleContainer.getRule();
		this.rulesPackage = IntinoUtil.modelPackage(ruleContainer).toLowerCase() + RULES_PACKAGE;
	}

	@Override
	public void analyze() {
		if (rule == null) {
			error();
			return;
		} else if (node.is(Tag.Instance)) {
			instanceError();
			return;
		}
		final Module module = module();
		if (rule.isLambda()) {
			if (node.type().equalsIgnoreCase(ProteoConstants.ASPECT) || node.type().equalsIgnoreCase(ProteoConstants.META_ASPECT))
				aspectError();
			return;
		} else if (module == null) return;

		PsiClass aClass = JavaPsiFacade.getInstance(rule.getProject()).findClass(rulesPackage + rule.getText(), moduleScope(module));
		if (aClass == null && !isProvided()) error();
	}

	private void aspectError() {
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
