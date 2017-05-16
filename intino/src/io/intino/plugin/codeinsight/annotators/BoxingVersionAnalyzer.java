package io.intino.plugin.codeinsight.annotators;

import io.intino.plugin.project.builders.InterfaceBuilderLoader;
import io.intino.tara.lang.model.Node;
import io.intino.tara.lang.model.Parameter;
import io.intino.tara.lang.semantics.errorcollector.SemanticNotification;
import io.intino.tara.plugin.annotator.TaraAnnotator;
import io.intino.tara.plugin.annotator.semanticanalizer.TaraAnalyzer;
import io.intino.tara.plugin.lang.psi.TaraNode;

import static io.intino.plugin.MessageProvider.message;

public class BoxingVersionAnalyzer extends TaraAnalyzer {
	private final Node interfaceNode;

	BoxingVersionAnalyzer(Node node) {
		this.interfaceNode = node;
	}

	@Override
	public void analyze() {
		if (interfaceNode.parameters().isEmpty()) return;
		Parameter parameter = interfaceNode.parameters().stream().filter(p -> p.name().equals("version")).findFirst().orElse(null);
		if (parameter == null) {
			if (interfaceNode.parameters().size() > 1)
				parameter = interfaceNode.parameters().get(1);
			else return;
		}
		final String version = parameter.values().get(0).toString();
		if (!InterfaceBuilderLoader.exists(version))
			results.put(((TaraNode) interfaceNode).getSignature(),
					new TaraAnnotator.AnnotateAndFix(SemanticNotification.Level.ERROR, message("error.interface.version.not.found", version)));
	}
}
