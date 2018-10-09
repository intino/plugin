package io.intino.plugin.codeinsight.annotators;

import com.intellij.psi.PsiElement;
import io.intino.plugin.codeinsight.annotators.fix.AddArgumentFix;
import io.intino.plugin.project.LegioConfiguration;
import io.intino.tara.lang.model.Node;
import io.intino.tara.lang.model.Parameter;
import io.intino.tara.lang.semantics.errorcollector.SemanticNotification;
import io.intino.tara.plugin.annotator.TaraAnnotator;
import io.intino.tara.plugin.annotator.semanticanalizer.TaraAnalyzer;
import io.intino.tara.plugin.lang.psi.TaraNode;
import io.intino.tara.plugin.lang.psi.impl.TaraUtil;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;

import static io.intino.plugin.MessageProvider.message;

public class RunConfigurationAnalyzer extends TaraAnalyzer {
	private final Node runConfigurationNode;
	private final LegioConfiguration configuration;

	RunConfigurationAnalyzer(Node node) {
		this.runConfigurationNode = node;
		this.configuration = (LegioConfiguration) TaraUtil.configurationOf((PsiElement) runConfigurationNode);
	}

	@Override
	public void analyze() {
		if (configuration == null || configuration.graph() == null) return;
		List<String> parameters = collectRequiredParameters();
		List<String> notFoundParameters = notFoundArguments(parameters);
		if (!notFoundParameters.isEmpty()) {
			SemanticNotification.Level level = level();
			results.put(((TaraNode) runConfigurationNode).getSignature(), new TaraAnnotator.AnnotateAndFix(level, message(level == SemanticNotification.Level.ERROR ? "error.parameters.missed" : "parameters.missed", String.join(", ", notFoundParameters)), new AddArgumentFix((PsiElement) runConfigurationNode, notFoundParameters)));
		}
	}

	private List<String> notFoundArguments(List<String> parameters) {
		return parameters.stream().filter(parameter -> !isDeclared(parameter)).collect(Collectors.toList());
	}

	private List<String> collectRequiredParameters() {
		return configuration.graph().artifact().parameterList().stream().filter(p -> p.defaultValue() == null).map(io.intino.legio.graph.Parameter::name).collect(Collectors.toList());
	}

	@NotNull
	private SemanticNotification.Level level() {
		return configuration.graph().artifact().deploymentList().stream().flatMap(deploy -> deploy.destinations().stream()).anyMatch(destination -> destination.runConfiguration().name$().equals(runConfigurationNode.name())) ?
				SemanticNotification.Level.ERROR : SemanticNotification.Level.WARNING;
	}

	private boolean isDeclared(String parameter) {
		for (Node component : runConfigurationNode.components()) {
			final Parameter nameParameterNode = nameParameterNode(component);
			if (component.type().endsWith("Argument") && nameParameterNode != null && !nameParameterNode.values().isEmpty() && parameter.equals(nameParameterNode.values().get(0).toString()))
				return true;
		}
		return false;
	}

	private Parameter nameParameterNode(Node node) {
		for (Parameter parameter : node.parameters())
			if (parameter.name().equals("name") || parameter.position() == 0) return parameter;
		return null;
	}
}
