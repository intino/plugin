package io.intino.plugin.codeinsight.annotators;

import com.intellij.psi.PsiElement;
import io.intino.plugin.codeinsight.annotators.fix.AddParameterFix;
import io.intino.plugin.project.LegioConfiguration;
import io.intino.plugin.project.configuration.LegioLanguage;
import io.intino.tara.compiler.shared.Configuration;
import io.intino.tara.lang.model.Node;
import io.intino.tara.lang.model.Parameter;
import io.intino.tara.plugin.annotator.TaraAnnotator.AnnotateAndFix;
import io.intino.tara.plugin.annotator.semanticanalizer.TaraAnalyzer;
import io.intino.tara.plugin.lang.LanguageManager;
import io.intino.tara.plugin.lang.psi.TaraNode;
import io.intino.tara.plugin.lang.psi.impl.TaraUtil;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.stream.Collectors;

import static io.intino.plugin.MessageProvider.message;
import static io.intino.tara.lang.semantics.errorcollector.SemanticNotification.Level.ERROR;

public class ArtifactParametersAnalyzer extends TaraAnalyzer {
	private final Node artifactNode;
	private final LegioConfiguration configuration;

	ArtifactParametersAnalyzer(Node node) {
		this.artifactNode = node;
		this.configuration = (LegioConfiguration) TaraUtil.configurationOf((PsiElement) artifactNode);
	}

	@Override
	public void analyze() {
		if (configuration == null || configuration.model() == null || configuration.model().language() != null) return;
		Map<String, String> languageParameters = collectLanguageParameters();
		Map<String, String> notFoundParameters = languageParameters.keySet().stream().filter(parameter -> !isDeclared(parameter)).collect(Collectors.toMap(parameter -> parameter, languageParameters::get, (a, b) -> b, LinkedHashMap::new));
		if (!notFoundParameters.isEmpty())
			results.put(((TaraNode) artifactNode).getSignature(), new AnnotateAndFix(ERROR, message("language.parameters.missing", Configuration.Model.Level.values()[configuration.model().level().ordinal() + 1].name()), new AddParameterFix((PsiElement) artifactNode, notFoundParameters)));
	}

	private boolean isDeclared(String parameter) {
		for (Node node : artifactNode.components()) {
			final Parameter parameterNode = nameParameter(node);
			if (node.type().endsWith("Parameter") && parameterNode != null && !parameterNode.values().isEmpty() && parameter.equals(parameterNode.values().get(0).toString()))
				return true;
		}
		return false;
	}

	private Parameter nameParameter(Node node) {
		for (Parameter parameter : node.parameters())
			if (parameter.name().equals("name") || parameter.position() == 0) return parameter;
		return null;
	}

	private Map<String, String> collectLanguageParameters() {
		Map<String, String> map = new LinkedHashMap<>();
		LegioLanguage language = configuration.model().language();
		final File languageFile = LanguageManager.getLanguageFile(language.name(), language.effectiveVersion());
		if (!languageFile.exists()) return map;
		return parameters(languageFile);
	}

	private Map<String, String> parameters(File languageFile) {
		Map<String, String> map = new HashMap<>();
		try {
			final JarFile jarFile = new JarFile(languageFile);
			final Manifest manifest = jarFile.getManifest();
			final Attributes framework = manifest.getAttributes("framework");
			if (framework == null) return map;
			for (Map.Entry<Object, Object> entry : framework.entrySet())
				map.put(entry.getKey().toString(), entry.getValue().toString());
		} catch (IOException ignored) {
		}
		return map;
	}
}
