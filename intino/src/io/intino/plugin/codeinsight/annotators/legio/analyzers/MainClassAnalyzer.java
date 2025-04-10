package io.intino.plugin.codeinsight.annotators.legio.analyzers;

import com.intellij.openapi.module.Module;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import io.intino.plugin.codeinsight.annotators.TaraAnnotator;
import io.intino.plugin.codeinsight.annotators.semanticanalizer.TaraAnalyzer;
import io.intino.tara.language.model.Mogram;
import io.intino.tara.language.model.Parameter;
import io.intino.tara.language.semantics.errorcollector.SemanticNotification;

import static com.intellij.psi.search.GlobalSearchScope.allScope;
import static io.intino.plugin.MessageProvider.message;

public class MainClassAnalyzer extends TaraAnalyzer {
	private final Mogram packageNode;
	private final Module module;

	public MainClassAnalyzer(Mogram node, Module module) {
		this.packageNode = node;
		this.module = module;
	}

	@Override
	public void analyze() {
		final Parameter parameter = mainClassParameter();
		if (module == null || parameter == null) return;
		final PsiClass aClass = JavaPsiFacade.getInstance(module.getProject()).findClass(parameter.values().get(0).toString().replace("\"", ""), allScope(module.getProject()));
		if (aClass == null)
			results.put((PsiElement) parameter, new TaraAnnotator.AnnotateAndFix(SemanticNotification.Level.ERROR, message("class.not.found")));
	}

	private Parameter mainClassParameter() {
		for (Parameter parameter : packageNode.parameters()) if (parameter.name().equals("mainClass")) return parameter;
		return null;
	}
}