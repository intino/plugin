package io.intino.legio.plugin.annotators;

import com.intellij.openapi.module.Module;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import tara.intellij.annotator.TaraAnnotator;
import tara.intellij.annotator.semanticanalizer.TaraAnalyzer;
import tara.lang.model.Node;
import tara.lang.model.Parameter;
import tara.lang.semantics.errorcollector.SemanticNotification;

import static com.intellij.psi.search.GlobalSearchScope.allScope;
import static io.intino.legio.plugin.MessageProvider.message;

class MainClassAnalyzer extends TaraAnalyzer {
	private final Node packageNode;
	private Module module;

	MainClassAnalyzer(Node node, Module module) {
		this.packageNode = node;
		this.module = module;
	}

	@Override
	public void analyze() {
		final Parameter parameter = mainClassParameter();
		if (parameter == null) return;
		final PsiClass aClass = JavaPsiFacade.getInstance(module.getProject()).findClass(parameter.values().get(0).toString(), allScope(module.getProject()));
		if (aClass == null)
			results.put((PsiElement) parameter, new TaraAnnotator.AnnotateAndFix(SemanticNotification.Level.ERROR, message("class.not.found")));
	}

	private Parameter mainClassParameter() {
		for (Parameter parameter : packageNode.parameters()) if (parameter.name().equals("mainClass")) return parameter;
		return null;
	}
}