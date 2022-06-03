package io.intino.plugin.annotator.semanticanalizer;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.openapi.module.Module;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import io.intino.magritte.lang.model.Node;
import io.intino.magritte.lang.model.Tag;
import io.intino.plugin.annotator.TaraAnnotator;
import io.intino.plugin.annotator.fix.SyncDecorableClassIntention;
import io.intino.plugin.lang.psi.TaraNode;
import io.intino.plugin.lang.psi.impl.IntinoUtil;
import io.intino.plugin.messages.MessageProvider;
import io.intino.plugin.project.module.ModuleProvider;

import java.util.Arrays;
import java.util.Objects;

import static com.intellij.psi.search.GlobalSearchScope.moduleScope;
import static io.intino.magritte.lang.semantics.errorcollector.SemanticNotification.Level.ERROR;
import static io.intino.plugin.codeinsight.languageinjection.helpers.Format.firstUpperCase;
import static io.intino.plugin.codeinsight.languageinjection.helpers.Format.javaValidName;

public class DecorableAnalyzer extends TaraAnalyzer {
	private final TaraNode node;
	private final String modelPackage;

	public DecorableAnalyzer(TaraNode node) {
		this.node = node;
		modelPackage = IntinoUtil.modelPackage(node).toLowerCase();
	}

	@Override
	public void analyze() {
		if (node.isAnonymous()) return;
		if (!node.is(Tag.Decorable)) return;
		Module module = ModuleProvider.moduleOf(node);
		if (module == null) return;
		PsiClass aClass = JavaPsiFacade.getInstance(node.getProject()).findClass(modelPackage + "." + format(node.name()), moduleScope(module));
		if (aClass == null) {
			results.put(node.getSignature(), new TaraAnnotator.AnnotateAndFix(ERROR, MessageProvider.message("error.link.to.decorable"), collectFixes()));
			return;
		}
		checkTree(aClass, node);
	}

	private void checkTree(PsiClass aClass, Node node) {
		if (!results.isEmpty()) return;
		for (Node component : node.components()) {
			if (component.isReference()) continue;
			String name = format(component.name());
			PsiClass inner = Arrays.stream(aClass.getInnerClasses()).filter(cl -> Objects.equals(cl.getName(), name)).findFirst().orElse(null);
			if (inner == null) {
				results.put(((TaraNode) component).getSignature(), new TaraAnnotator.AnnotateAndFix(ERROR, MessageProvider.message("error.link.to.decorable"), collectFixes()));
				return;
			}
			checkTree(inner, component);
		}
	}

	private String format(String name) {
		return firstUpperCase().format(javaValidName().format(name)).toString();
	}

	private IntentionAction[] collectFixes() {
		if (node == null) return new IntentionAction[0];
		return new IntentionAction[]{new SyncDecorableClassIntention(node, modelPackage)};
	}
}
