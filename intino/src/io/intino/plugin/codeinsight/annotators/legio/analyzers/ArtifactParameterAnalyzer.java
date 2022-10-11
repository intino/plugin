package io.intino.plugin.codeinsight.annotators.legio.analyzers;

import com.intellij.psi.PsiElement;
import io.intino.magritte.lang.model.Node;
import io.intino.magritte.lang.model.Parameter;
import io.intino.plugin.codeinsight.annotators.TaraAnnotator;
import io.intino.plugin.codeinsight.annotators.fix.RemoveElementFix;
import io.intino.plugin.codeinsight.annotators.semanticanalizer.TaraAnalyzer;
import io.intino.plugin.lang.psi.TaraNode;
import io.intino.plugin.lang.psi.impl.IntinoUtil;
import io.intino.plugin.project.configuration.LegioConfiguration;

import java.util.List;

import static io.intino.magritte.lang.semantics.errorcollector.SemanticNotification.Level.ERROR;
import static io.intino.plugin.MessageProvider.message;
import static io.intino.plugin.project.Safe.safeList;

public class ArtifactParameterAnalyzer extends TaraAnalyzer {
	private final Node parameterNode;
	private final LegioConfiguration configuration;
	private final String name;

	public ArtifactParameterAnalyzer(Node node) {
		this.parameterNode = node;
		this.configuration = (LegioConfiguration) IntinoUtil.configurationOf((PsiElement) parameterNode);
		this.name = parameterName();
	}

	@Override
	public void analyze() {
		if (name == null || name.isEmpty())
			results.put(((TaraNode) parameterNode).getSignature(), new TaraAnnotator.AnnotateAndFix(ERROR, message("parameter.name.not.found")));
		if (isDuplicated())
			results.put(((TaraNode) parameterNode).getSignature(), new TaraAnnotator.AnnotateAndFix(ERROR, message("duplicated.parameter"), new RemoveElementFix((PsiElement) parameterNode)));
		if (contains("."))
			results.put(((TaraNode) parameterNode).getSignature(), new TaraAnnotator.AnnotateAndFix(ERROR, message("dot.not.allowed.parameter"), new RemoveElementFix((PsiElement) parameterNode)));
	}

	private boolean contains(String value) {
		return name != null && name.contains(value);
	}

	private boolean isDuplicated() {
		if (name == null || name.isEmpty()) return false;
		return (int) safeList(() -> configuration.artifact().parameters()).stream().filter(parameter -> name.equals(parameter.name())).count() > 1;
	}

	private String parameterName() {
		final List<io.intino.magritte.lang.model.Parameter> parameters = parameterNode.parameters();
		for (Parameter parameter : parameters)
			if (parameter.name().equals("name")) return parameter.values().get(0).toString();
		return null;
	}
}
