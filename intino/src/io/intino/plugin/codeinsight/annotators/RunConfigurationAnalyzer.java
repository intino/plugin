package io.intino.plugin.codeinsight.annotators;

import com.intellij.psi.PsiElement;
import io.intino.Configuration;
import io.intino.plugin.annotator.TaraAnnotator.AnnotateAndFix;
import io.intino.plugin.annotator.semanticanalizer.TaraAnalyzer;
import io.intino.plugin.codeinsight.annotators.fix.AddArgumentFix;
import io.intino.plugin.lang.psi.TaraNode;
import io.intino.plugin.lang.psi.impl.TaraUtil;
import io.intino.plugin.project.LegioConfiguration;
import io.intino.tara.lang.model.Node;
import io.intino.tara.lang.model.Parameter;
import io.intino.tara.lang.semantics.errorcollector.SemanticNotification.Level;
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
		if (configuration == null) return;
		List<String> parameters = collectRequiredParameters();
		List<String> notFoundParameters = notFoundArguments(parameters);
		if (!notFoundParameters.isEmpty()) {
			Level level = level();
			results.put(((TaraNode) runConfigurationNode).getSignature(), new AnnotateAndFix(level, message(level == Level.ERROR ?
					"error.parameters.missed" :
					"parameters.missed", String.join(", ", notFoundParameters)), new AddArgumentFix((PsiElement) runConfigurationNode, notFoundParameters)));
		}
	}

	private List<String> notFoundArguments(List<String> parameters) {
		return parameters.stream().filter(parameter -> !isDeclared(parameter)).collect(Collectors.toList());
	}

	private List<String> collectRequiredParameters() {
		return configuration.artifact().parameters().stream().filter(p -> p.value() == null).map(Configuration.Parameter::name).collect(Collectors.toList());
	}

	@NotNull
	private Level level() {
		return configuration.artifact().deployments().stream().
				anyMatch(destination -> destination != null && destination.runConfiguration() != null && runConfigurationNode.name().equals(destination.runConfiguration().name())) ?
				Level.ERROR : Level.WARNING;
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
