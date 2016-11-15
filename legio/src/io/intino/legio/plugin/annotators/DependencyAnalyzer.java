package io.intino.legio.plugin.annotators;

import com.intellij.psi.PsiElement;
import io.intino.legio.Project;
import io.intino.legio.plugin.dependencyresolution.LibraryManager;
import io.intino.legio.plugin.project.LegioConfiguration;
import tara.intellij.annotator.TaraAnnotator;
import tara.intellij.annotator.semanticanalizer.TaraAnalyzer;
import tara.intellij.lang.psi.TaraNode;
import tara.intellij.project.module.ModuleProvider;
import tara.lang.model.Node;
import tara.lang.model.Parameter;
import tara.lang.semantics.errorcollector.SemanticNotification;

import java.util.List;

public class DependencyAnalyzer extends TaraAnalyzer {
	private final Node node;
	private final LegioConfiguration configuration;

	public DependencyAnalyzer(Node node, LegioConfiguration configuration) {
		this.node = node;
		this.configuration = configuration;
	}

	@Override
	public void analyze() {
		LibraryManager manager = new LibraryManager(ModuleProvider.moduleOf((PsiElement) node));
		configuration.dependencies().stream().filter(dependency -> manager.findLibrary(dependency) == null).forEach(d -> {
			PsiElement dependencyNode = findDependencyNodeFor(d);
			if (dependencyNode != null)
				results.put(((TaraNode) dependencyNode).getSignature(), new TaraAnnotator.AnnotateAndFix(SemanticNotification.Level.ERROR, "Dependency not found"));
		});
	}

	private PsiElement findDependencyNodeFor(Project.Dependencies.Dependency dependency) {
		for (Node node : this.node.components())
			if (node.simpleType().equals(dependency.concept().id().replace("$", ".")) && equalParameters(node.parameters(), dependency))
				return (PsiElement) node;
		return null;
	}

	private boolean equalParameters(List<Parameter> parameters, Project.Dependencies.Dependency dependency) {
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

	private boolean artifactId(List<Parameter> parameters, String groupId) {
		for (Parameter parameter : parameters) {
			if (parameter.values() == null || parameter.values().isEmpty()) continue;
			if (isArtifactId(groupId, parameter)) return true;
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
