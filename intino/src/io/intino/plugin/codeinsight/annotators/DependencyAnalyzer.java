package io.intino.plugin.codeinsight.annotators;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.roots.ModuleRootManager;
import io.intino.legio.graph.Artifact;
import io.intino.plugin.project.LegioConfiguration;
import io.intino.tara.compiler.shared.Configuration;
import io.intino.tara.lang.model.Node;
import io.intino.tara.lang.model.Parameter;
import io.intino.tara.lang.semantics.errorcollector.SemanticNotification.Level;
import io.intino.tara.plugin.annotator.TaraAnnotator;
import io.intino.tara.plugin.annotator.semanticanalizer.TaraAnalyzer;
import io.intino.tara.plugin.lang.psi.TaraNode;
import io.intino.tara.plugin.lang.psi.impl.TaraUtil;

import java.util.List;

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
		final Artifact.Imports.Dependency dependency = findDependencyNode();
		if (dependency == null || !dependency.resolved()) {
			results.put(((TaraNode) dependencyNode).getSignature(), new TaraAnnotator.AnnotateAndFix(Level.ERROR, message("reject.dependency.not.found")));
		} else if (dependency.toModule() && !hasSameVersion(findModule(dependency), dependency.version()))
			results.put(((TaraNode) dependencyNode).getSignature(), new TaraAnnotator.AnnotateAndFix(Level.WARNING, message("warning.module.dependency.with.different.version")));
//		LibraryManager manager = new LibraryManager(ModuleProvider.moduleOf((PsiElement) dependencyNode));
//		else for (String artifact : dependencyForNode.artifacts()) {
//			final Library library = manager.findLibrary(artifact);
//			if (library == null)
//				results.put(((TaraNode) dependencyNode).getSignature(), new TaraAnnotator.AnnotateAndFix(SemanticNotification.Level.ERROR, message("reject.dependency.not.found")));
//		}
	}

	private boolean hasSameVersion(Module module, String version) {
		final Configuration configuration = TaraUtil.configurationOf(module);
		return configuration == null || version.equals(configuration.version()) || isRange(version) && versionIsUnderRange(configuration.version(), version);
	}

	private boolean versionIsUnderRange(String moduleVersion, String version) {
		return isInRange(moduleVersion, rangeValuesOf(version));
	}

	private Module findModule(Artifact.Imports.Dependency dependency) {
		for (Module m : ModuleRootManager.getInstance(module).getDependencies()) {
			final Configuration configuration = TaraUtil.configurationOf(m);
			if (configuration != null && configuration.groupId().equals(dependency.groupId()) && configuration.artifactId().equals(dependency.artifactId()))
				return m;
		}
		return null;
	}

	private Artifact.Imports.Dependency findDependencyNode() {
		if (configuration == null) return null;
		for (Artifact.Imports.Dependency d : safeList(() -> configuration.graph().artifact().imports().dependencyList()))
			if (dependencyNode.type().equals(d.core$().graph().concept(d.getClass()).id().replace("$", ".")) && equalParameters(dependencyNode.parameters(), d))
				return d;
		return null;
	}

	private boolean equalParameters(List<Parameter> parameters, Artifact.Imports.Dependency dependency) {
		return groupId(parameters, dependency.groupId()) &&
				artifactId(parameters, dependency.artifactId()) &&
				version(parameters, dependency.version());
	}

	private boolean groupId(List<Parameter> parameters, String groupId) {
		for (Parameter parameter : parameters) {
			if (parameter.values() == null || parameter.values().isEmpty()) continue;
			if (isGroupId(groupId, parameter)) return true;
		}
		return false;
	}

	private boolean artifactId(List<Parameter> parameters, String artifactId) {
		for (Parameter parameter : parameters) {
			if (parameter.values() == null || parameter.values().isEmpty()) continue;
			if (isArtifactId(artifactId, parameter)) return true;
		}
		return false;
	}

	private boolean version(List<Parameter> parameters, String version) {
		for (Parameter parameter : parameters) {
			if (parameter.values() == null || parameter.values().isEmpty()) continue;
			if (isVersion(version, parameter)) return true;
		}
		return false;
	}

	private boolean isArtifactId(String artifactId, Parameter parameter) {
		return parameter.name().equals("artifactId") && parameter.values().get(0).equals(artifactId);
	}

	private boolean isGroupId(String groupId, Parameter parameter) {
		return parameter.name().equals("groupId") && parameter.values().get(0).equals(groupId);
	}

	private boolean isVersion(String version, Parameter parameter) {
		return parameter.name().equals("version") && parameter.values().get(0).equals(version);
	}
}
