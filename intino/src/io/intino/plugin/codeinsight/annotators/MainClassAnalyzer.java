package io.intino.plugin.codeinsight.annotators;

import com.intellij.openapi.module.Module;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import io.intino.magritte.lang.model.Node;
import io.intino.magritte.lang.model.Parameter;
import io.intino.magritte.lang.semantics.errorcollector.SemanticNotification;
import io.intino.plugin.annotator.TaraAnnotator;
import io.intino.plugin.annotator.semanticanalizer.TaraAnalyzer;

import static com.intellij.psi.search.GlobalSearchScope.allScope;
import static io.intino.plugin.MessageProvider.message;

class MainClassAnalyzer extends TaraAnalyzer {
	private final Node packageNode;
	private final Module module;

	MainClassAnalyzer(Node node, Module module) {
		this.packageNode = node;
		this.module = module;
	}

	@Override
	public void analyze() {
		final Parameter parameter = mainClassParameter();
		if (module == null || parameter == null) return;
		final PsiClass aClass = JavaPsiFacade.getInstance(module.getProject()).findClass(parameter.values().get(0).toString(), allScope(module.getProject()));
		if (aClass == null)
			results.put((PsiElement) parameter, new TaraAnnotator.AnnotateAndFix(SemanticNotification.Level.ERROR, message("class.not.found")));
	}

	private Parameter mainClassParameter() {
		for (Parameter parameter : packageNode.parameters()) if (parameter.name().equals("mainClass")) return parameter;
		return null;
	}
}