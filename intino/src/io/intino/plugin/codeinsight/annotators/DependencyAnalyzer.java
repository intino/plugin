package io.intino.plugin.codeinsight.annotators;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.roots.ModuleRootManager;
import io.intino.Configuration;
import io.intino.Configuration.Artifact;
import io.intino.magritte.lang.model.Node;
import io.intino.magritte.lang.semantics.errorcollector.SemanticNotification.Level;
import io.intino.plugin.annotator.TaraAnnotator;
import io.intino.plugin.annotator.semanticanalizer.TaraAnalyzer;
import io.intino.plugin.lang.psi.TaraNode;
import io.intino.plugin.lang.psi.impl.IntinoUtil;
import io.intino.plugin.project.LegioConfiguration;
import io.intino.plugin.project.configuration.model.LegioDependency;

import static io.intino.plugin.MessageProvider.message;
import static io.intino.plugin.project.LibraryConflictResolver.VersionRange.*;
import static io.intino.plugin.project.Safe.safeList;

class DependencyAnalyzer extends TaraAnalyzer {
	private Module module;
	private final Node dependencyNode;
	private final LegioConfiguration configuration;

	DependencyAnalyzer(Module module, Node node, LegioConfiguration configuration) {
		this.module = module;
		this.dependencyNode = node;
		this.configuration = configuration;
	}

	@Override
	public void analyze() {
		if (configuration == null || !configuration.inited()) return;
		final Artifact.Dependency dependency = findDependencyNode();
		if (dependency == null || !dependency.resolved()) {
			results.put(((TaraNode) dependencyNode).getSignature(), new TaraAnnotator.AnnotateAndFix(Level.ERROR, message("reject.dependency.not.found")));
		} else if (dependency.toModule() && !hasSameVersion(findModule(dependency), dependency.version()))
			results.put(((TaraNode) dependencyNode).getSignature(), new TaraAnnotator.AnnotateAndFix(Level.WARNING, message("warning.module.dependency.with.different.version")));
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
			if (configuration != null && configuration.artifact().groupId().equals(dependency.groupId()) && configuration.artifact().name().equals(dependency.artifactId()))
				return m;
		}
		return null;
	}

	private Artifact.Dependency findDependencyNode() {
		if (configuration == null) return null;
		return safeList(() -> configuration.artifact().dependencies()).stream().filter(d -> dependencyNode.equals(((LegioDependency) d).node())).findFirst().orElse(null);
	}
}
