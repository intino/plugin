package io.intino.plugin.codeinsight.annotators;

import io.intino.Configuration.Artifact;
import io.intino.magritte.lang.model.Node;
import io.intino.magritte.lang.semantics.errorcollector.SemanticNotification.Level;
import io.intino.plugin.annotator.TaraAnnotator;
import io.intino.plugin.annotator.semanticanalizer.TaraAnalyzer;
import io.intino.plugin.lang.psi.TaraNode;
import io.intino.plugin.project.configuration.LegioConfiguration;

import static io.intino.plugin.MessageProvider.message;
import static io.intino.plugin.project.Safe.safe;

class ArchetypeAnalyzer extends TaraAnalyzer {
	private final Node dependencyNode;
	private final LegioConfiguration configuration;

	ArchetypeAnalyzer(Node node, LegioConfiguration configuration) {
		this.dependencyNode = node;
		this.configuration = configuration;
	}

	@Override
	public void analyze() {
		if (configuration == null || !configuration.inited()) return;
		final Artifact.Dependency.Archetype dependency = findArchetype();
		if (dependency == null || !dependency.resolved())
			results.put(((TaraNode) dependencyNode).getSignature(), new TaraAnnotator.AnnotateAndFix(Level.ERROR, message("reject.dependency.not.found")));
	}


	private Artifact.Dependency.Archetype findArchetype() {
		if (configuration == null) return null;
		return safe(() -> configuration.artifact().archetype());
	}

}