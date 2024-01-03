package io.intino.plugin.codeinsight.annotators.legio.analyzers;

import io.intino.Configuration.Artifact;
import io.intino.plugin.codeinsight.annotators.TaraAnnotator;
import io.intino.plugin.codeinsight.annotators.semanticanalizer.TaraAnalyzer;
import io.intino.plugin.lang.psi.TaraMogram;
import io.intino.plugin.project.configuration.ArtifactLegioConfiguration;
import io.intino.tara.language.model.Mogram;
import io.intino.tara.language.semantics.errorcollector.SemanticNotification.Level;

import static io.intino.plugin.MessageProvider.message;
import static io.intino.plugin.project.Safe.safe;

public class ArchetypeAnalyzer extends TaraAnalyzer {
	private final Mogram dependencyNode;
	private final ArtifactLegioConfiguration configuration;

	public ArchetypeAnalyzer(Mogram node, ArtifactLegioConfiguration configuration) {
		this.dependencyNode = node;
		this.configuration = configuration;
	}

	@Override
	public void analyze() {
		if (configuration == null || !configuration.inited()) return;
		final Artifact.Dependency.Archetype dependency = findArchetype();
		if (dependency == null || !new DependencyAnalyzer(configuration.module(), dependencyNode, configuration).isResolved(dependency))
			results.put(((TaraMogram) dependencyNode).getSignature(), new TaraAnnotator.AnnotateAndFix(Level.ERROR, message("reject.dependency.not.found")));
	}


	private Artifact.Dependency.Archetype findArchetype() {
		if (configuration == null) return null;
		return safe(() -> configuration.artifact().archetype());
	}

}