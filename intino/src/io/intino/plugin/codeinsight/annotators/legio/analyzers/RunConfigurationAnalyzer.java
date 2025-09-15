package io.intino.plugin.codeinsight.annotators.legio.analyzers;

import com.intellij.psi.PsiElement;
import io.intino.Configuration;
import io.intino.plugin.codeinsight.annotators.TaraAnnotator.AnnotateAndFix;
import io.intino.plugin.codeinsight.annotators.legio.fix.AddArgumentFix;
import io.intino.plugin.codeinsight.annotators.semanticanalizer.TaraAnalyzer;
import io.intino.plugin.lang.psi.TaraMogram;
import io.intino.plugin.lang.psi.impl.IntinoUtil;
import io.intino.plugin.project.configuration.ArtifactLegioConfiguration;
import io.intino.tara.language.model.Mogram;
import io.intino.tara.language.model.Parameter;
import io.intino.tara.language.semantics.errorcollector.SemanticNotification.Level;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

import static io.intino.plugin.MessageProvider.message;

public class RunConfigurationAnalyzer extends TaraAnalyzer {
	private final Mogram runConfigurationNode;
	private final ArtifactLegioConfiguration configuration;

	public RunConfigurationAnalyzer(Mogram node) {
		this.runConfigurationNode = node;
		this.configuration = (ArtifactLegioConfiguration) IntinoUtil.configurationOf((PsiElement) runConfigurationNode);
	}

	@Override
	public void analyze() {
		if (configuration == null) return;
		List<String> parameters = collectRequiredParameters();
		List<String> notFoundParameters = notFoundArguments(parameters);
		if (!notFoundParameters.isEmpty()) {
			Level level = level();
			results.put(((TaraMogram) runConfigurationNode).getSignature(), new AnnotateAndFix(level, message(level == Level.ERROR ?
					"error.parameters.missed" :
					"parameters.missed", String.join(", ", notFoundParameters)), new AddArgumentFix((PsiElement) runConfigurationNode, notFoundParameters)));
		}
	}

	private List<String> notFoundArguments(List<String> parameters) {
		List<String> arguments = runConfigurationNode.components().stream()
				.filter(p -> p.type().endsWith("Argument"))
				.map(this::nameParameterNode)
				.filter(Objects::nonNull)
				.filter(a -> !a.values().isEmpty())
				.map(a -> a.values().get(0).toString())
				.toList();
		return parameters.stream().filter(arguments::contains).toList();
	}

	private List<String> collectRequiredParameters() {
		return configuration.artifact().parameters().stream()
				.filter(p -> p.value() == null)
				.map(Configuration.Parameter::name)
				.filter(Objects::nonNull)
				.toList();
	}

	@NotNull
	private Level level() {
		return configuration.artifact().deployments().stream().
				anyMatch(destination -> destination != null && destination.runConfiguration() != null && runConfigurationNode.name().equals(destination.runConfiguration().name())) ?
				Level.ERROR : Level.WARNING;
	}

	private Parameter nameParameterNode(Mogram node) {
		for (Parameter parameter : node.parameters())
			if (parameter.name().equals("name") || parameter.position() == 0) return parameter;
		return null;
	}
}
