package io.intino.plugin.codeinsight.annotators.legio.analyzers;

import com.intellij.psi.PsiElement;
import io.intino.Configuration;
import io.intino.magritte.lang.model.Node;
import io.intino.magritte.lang.model.Parameter;
import io.intino.plugin.codeinsight.annotators.TaraAnnotator.AnnotateAndFix;
import io.intino.plugin.codeinsight.annotators.legio.fix.AddParameterFix;
import io.intino.plugin.codeinsight.annotators.semanticanalizer.TaraAnalyzer;
import io.intino.plugin.lang.LanguageManager;
import io.intino.plugin.lang.psi.TaraNode;
import io.intino.plugin.lang.psi.impl.IntinoUtil;
import io.intino.plugin.project.configuration.LegioConfiguration;
import io.intino.plugin.project.configuration.model.LegioLanguage;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.stream.Collectors;

import static io.intino.magritte.lang.semantics.errorcollector.SemanticNotification.Level.ERROR;
import static io.intino.plugin.MessageProvider.message;
import static io.intino.plugin.project.Safe.safe;

public class ArtifactParametersAnalyzer extends TaraAnalyzer {
	private final Node artifactNode;
	private final LegioConfiguration configuration;

	public ArtifactParametersAnalyzer(Node node) {
		this.artifactNode = node;
		this.configuration = (LegioConfiguration) IntinoUtil.configurationOf((PsiElement) artifactNode);
	}

	@Override
	public void analyze() {
		Configuration.Artifact.Model model = safe(() -> configuration.artifact().model());
		if (model == null) return;
		Map<String, String> languageParameters = collectLanguageParameters();
		Map<String, String> notFoundParameters = languageParameters.keySet().stream().filter(parameter -> !isDeclared(parameter)).collect(Collectors.toMap(parameter -> parameter, languageParameters::get, (a, b) -> b, LinkedHashMap::new));
		if (!notFoundParameters.isEmpty())
			results.put(((TaraNode) artifactNode).getSignature(), new AnnotateAndFix(ERROR, message("language.parameters.missing", Configuration.Artifact.Model.Level.values()[model.level().ordinal() + 1].name()), new AddParameterFix((PsiElement) artifactNode, notFoundParameters)));
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
		LegioLanguage language = (LegioLanguage) configuration.artifact().model().language();
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