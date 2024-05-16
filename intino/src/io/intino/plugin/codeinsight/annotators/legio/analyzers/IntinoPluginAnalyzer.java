package io.intino.plugin.codeinsight.annotators.legio.analyzers;

import io.intino.Configuration.Artifact;
import io.intino.plugin.codeinsight.annotators.TaraAnnotator;
import io.intino.plugin.codeinsight.annotators.semanticanalizer.TaraAnalyzer;
import io.intino.plugin.dependencyresolution.MavenDependencyResolver;
import io.intino.plugin.dependencyresolution.Repositories;
import io.intino.plugin.lang.psi.TaraMogram;
import io.intino.plugin.project.configuration.ArtifactLegioConfiguration;
import io.intino.plugin.project.configuration.model.LegioArtifact;
import io.intino.tara.language.model.Mogram;
import io.intino.tara.language.semantics.errorcollector.SemanticNotification.Level;
import org.eclipse.aether.artifact.DefaultArtifact;

import static io.intino.plugin.MessageProvider.message;
import static io.intino.plugin.project.Safe.safe;

public class IntinoPluginAnalyzer extends TaraAnalyzer {
	private final Mogram mogram;
	private final ArtifactLegioConfiguration configuration;

	public IntinoPluginAnalyzer(Mogram mogram, ArtifactLegioConfiguration configuration) {
		this.mogram = mogram;
		this.configuration = configuration;
	}

	@Override
	public void analyze() {
		if (configuration == null || !configuration.inited()) return;
		var plugin = findIntinoPluginNode();
		if (plugin == null) return;
		if (!new MavenDependencyResolver(Repositories.of(configuration.module())).isDownloaded(new DefaultArtifact(plugin.groupId(), plugin.artifactId(), "jar", plugin.version())))
			results.put(((TaraMogram) mogram).getSignature(), new TaraAnnotator.AnnotateAndFix(Level.ERROR, message("reject.plugin.not.found")));
	}


	private Artifact.Plugin findIntinoPluginNode() {
		return safe(() -> configuration.artifact().plugins().stream()
				.filter(p -> ((LegioArtifact.LegioPlugin) p).mogram().equals(mogram)).findFirst().orElse(null));
	}

}