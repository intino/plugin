package io.intino.plugin.codeinsight.annotators.legio.analyzers;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.roots.ModuleRootManager;
import io.intino.Configuration;
import io.intino.Configuration.Artifact;
import io.intino.plugin.codeinsight.annotators.TaraAnnotator;
import io.intino.plugin.codeinsight.annotators.semanticanalizer.TaraAnalyzer;
import io.intino.plugin.dependencyresolution.MavenDependencyResolver;
import io.intino.plugin.dependencyresolution.ModuleLibrariesManager;
import io.intino.plugin.lang.psi.TaraMogram;
import io.intino.plugin.lang.psi.impl.IntinoUtil;
import io.intino.plugin.project.configuration.ArtifactLegioConfiguration;
import io.intino.plugin.project.configuration.model.LegioDependency;
import io.intino.tara.language.model.Mogram;
import io.intino.tara.language.semantics.errorcollector.SemanticNotification.Level;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.graph.Dependency;

import static io.intino.plugin.MessageProvider.message;
import static io.intino.plugin.project.LibraryConflictResolver.VersionRange.*;
import static io.intino.plugin.project.Safe.safeList;

public class DependencyAnalyzer extends TaraAnalyzer {
	private final Mogram dependencyNode;
	private final ArtifactLegioConfiguration configuration;
	private final Module module;

	public DependencyAnalyzer(Module module, Mogram node, ArtifactLegioConfiguration configuration) {
		this.module = module;
		this.dependencyNode = node;
		this.configuration = configuration;
	}

	@Override
	public void analyze() {
		if (configuration == null || !configuration.inited()) return;
		final Artifact.Dependency dependency = findDependencyNode();
		if (dependency == null || !isResolved(dependency))
			results.put(((TaraMogram) dependencyNode).getSignature(), new TaraAnnotator.AnnotateAndFix(Level.ERROR, message("reject.dependency.not.found")));
		else if (dependency.toModule() && !hasSameVersion(findModule(dependency), dependency.version()))
			results.put(((TaraMogram) dependencyNode).getSignature(), new TaraAnnotator.AnnotateAndFix(Level.WARNING, message("warning.module.dependency.with.different.version")));
	}

	public boolean isResolved(Artifact.Dependency dependency) {
		DefaultArtifact artifact = MavenDependencyResolver.artifactOf(dependency);
		ModuleLibrariesManager moduleLibrariesManager = new ModuleLibrariesManager(module);
		return moduleLibrariesManager.isAlreadyAdded(new Dependency(artifact, dependency.scope()));

	}

	private boolean hasSameVersion(Module module, String version) {
		final Configuration configuration = IntinoUtil.configurationOf(module);
		return configuration == null || version.equals(configuration.artifact().version()) || isRange(version) && versionIsUnderRange(configuration.artifact().version(), version);
	}

	private boolean versionIsUnderRange(String moduleVersion, String version) {
		return isInRange(moduleVersion, rangeValuesOf(version));
	}

	private Module findModule(Artifact.Dependency dependency) {
		for (Module m : ModuleRootManager.getInstance(module).getDependencies()) {
			final Configuration configuration = IntinoUtil.configurationOf(m);
			if (configuration instanceof ArtifactLegioConfiguration && configuration.artifact().groupId().equals(dependency.groupId()) && configuration.artifact().name().equals(dependency.artifactId()))
				return m;
		}
		return null;
	}

	private Artifact.Dependency findDependencyNode() {
		if (configuration == null) return null;
		return safeList(() -> configuration.artifact().dependencies()).stream().filter(d -> dependencyNode.equals(((LegioDependency) d).node())).findFirst().orElse(null);
	}
}
