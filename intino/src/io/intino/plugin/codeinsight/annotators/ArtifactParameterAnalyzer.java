package io.intino.plugin.codeinsight.annotators;

import com.intellij.psi.PsiElement;
import io.intino.plugin.project.LegioConfiguration;
import io.intino.tara.lang.model.Node;
import io.intino.tara.lang.model.Parameter;
import io.intino.tara.plugin.annotator.TaraAnnotator;
import io.intino.tara.plugin.annotator.fix.RemoveElementFix;
import io.intino.tara.plugin.annotator.semanticanalizer.TaraAnalyzer;
import io.intino.tara.plugin.lang.psi.TaraNode;
import io.intino.tara.plugin.lang.psi.impl.TaraUtil;

import java.util.List;

import static io.intino.plugin.MessageProvider.message;
import static io.intino.tara.lang.semantics.errorcollector.SemanticNotification.Level.ERROR;

public class ArtifactParameterAnalyzer extends TaraAnalyzer {
	private final Node parameterNode;
	private final LegioConfiguration configuration;
	private final String name;

	public ArtifactParameterAnalyzer(Node node) {
		this.parameterNode = node;
		this.configuration = (LegioConfiguration) TaraUtil.configurationOf((PsiElement) parameterNode);
		this.name = parameterName();

	}

	@Override
	public void analyze() {
		if (configuration == null || configuration.model() == null || configuration.model().language() == null) return;
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
		return (int) configuration.graph().artifact().parameterList().stream().filter(parameter -> parameter.name().equals(name)).count() > 1;
	}

	private String parameterName() {
		final List<io.intino.tara.lang.model.Parameter> parameters = parameterNode.parameters();
		for (Parameter parameter : parameters)
			if (parameter.name().equals("name")) return parameter.values().get(0).toString();
		return null;
	}
}
