package io.intino.plugin.codeinsight.annotators;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import io.intino.plugin.codeinsight.annotators.fix.AddParameterFix;
import io.intino.plugin.project.LegioConfiguration;
import io.intino.tara.compiler.shared.Configuration;
import io.intino.tara.lang.model.Node;
import io.intino.tara.lang.model.Parameter;
import io.intino.tara.lang.semantics.errorcollector.SemanticNotification;
import io.intino.tara.plugin.annotator.TaraAnnotator;
import io.intino.tara.plugin.annotator.semanticanalizer.TaraAnalyzer;
import io.intino.tara.plugin.lang.LanguageManager;
import io.intino.tara.plugin.lang.psi.TaraNode;
import io.intino.tara.plugin.lang.psi.impl.TaraUtil;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import static io.intino.plugin.MessageProvider.message;

public class PackageParametersAnalyzer extends TaraAnalyzer {
	private final Node packageNode;
	private final Project project;
	private final LegioConfiguration configuration;

	PackageParametersAnalyzer(Node node) {
		this.packageNode = node;
		this.project = ((PsiElement) node).getProject();
		this.configuration = (LegioConfiguration) TaraUtil.configurationOf((PsiElement) packageNode);
	}

	@Override
	public void analyze() {
		if (configuration == null || configuration.languages().isEmpty()) return;
		Map<String, String> languageParameters = collectLanguageParameters();
		Map<String, String> notFoundParameters = new LinkedHashMap<>();

		for (String parameter : languageParameters.keySet())
			if (!isDeclared(parameter)) notFoundParameters.put(parameter, languageParameters.get(parameter));

		if (!notFoundParameters.isEmpty())
			results.put(((TaraNode) packageNode).getSignature(), new TaraAnnotator.AnnotateAndFix(SemanticNotification.Level.WARNING, message("parameters.missing"), new AddParameterFix((PsiElement) packageNode, notFoundParameters)));
	}

	private boolean isDeclared(String parameter) {
		for (Node node : packageNode.components()) {
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

	private Parameter valueParameter(Node node) {
		for (Parameter parameter : node.parameters())
			if (parameter.name().equals("value") || parameter.position() == 1) return parameter;
		return null;
	}

	private Map<String, String> collectLanguageParameters() {
		Map<String, String> map = new LinkedHashMap<>();
		for (Configuration.LanguageLibrary library : configuration.languages()) {
			final File languageFile = LanguageManager.getLanguageFile(library.name(), library.effectiveVersion());
			if (!languageFile.exists()) continue;
			map.putAll(parameters(languageFile));
		}
		return map;
	}

	private Map<String, String> parameters(File languageFile) {
		Map<String, String> map = new HashMap<>();
		try {
			final JarFile jarFile = new JarFile(languageFile);
			final Manifest manifest = jarFile.getManifest();
			for (Map.Entry<Object, Object> entry : manifest.getAttributes("framework").entrySet()) {
				map.put(entry.getKey().toString(), entry.getValue().toString());
			}
		} catch (IOException ignored) {
		}
		return map;
	}
}
