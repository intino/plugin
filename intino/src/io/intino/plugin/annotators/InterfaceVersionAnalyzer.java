package io.intino.plugin.annotators;

import io.intino.plugin.project.LegioConfiguration;
import io.intino.plugin.project.builders.InterfaceBuilderLoader;
import io.intino.tara.lang.model.Node;
import io.intino.tara.lang.model.Parameter;
import io.intino.tara.lang.semantics.errorcollector.SemanticNotification;
import io.intino.tara.plugin.annotator.TaraAnnotator;
import io.intino.tara.plugin.annotator.semanticanalizer.TaraAnalyzer;
import io.intino.tara.plugin.lang.psi.TaraNode;

import static io.intino.plugin.MessageProvider.message;

public class InterfaceVersionAnalyzer extends TaraAnalyzer {
	private final Node interfaceNode;
	private final LegioConfiguration configuration;

	InterfaceVersionAnalyzer(Node node, LegioConfiguration configuration) {
		this.interfaceNode = node;
		this.configuration = configuration;
	}

	@Override
	public void analyze() {
		if (interfaceNode.parameters().isEmpty()) return;
		final Parameter parameter = interfaceNode.parameters().get(0);
		final String version = parameter.values().get(0).toString();
		if (!InterfaceBuilderLoader.exists(version))
			results.put(((TaraNode) interfaceNode).getSignature(), new TaraAnnotator.AnnotateAndFix(SemanticNotification.Level.ERROR, message("error.interface.version.not.found", version)));
	}
}
