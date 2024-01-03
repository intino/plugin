package io.intino.plugin.codeinsight.annotators.legio.analyzers;

import com.intellij.psi.PsiElement;
import io.intino.Configuration;
import io.intino.Configuration.Artifact.Model.Level;
import io.intino.plugin.codeinsight.annotators.TaraAnnotator.AnnotateAndFix;
import io.intino.plugin.codeinsight.annotators.legio.fix.AddParameterFix;
import io.intino.plugin.codeinsight.annotators.semanticanalizer.TaraAnalyzer;
import io.intino.plugin.lang.LanguageManager;
import io.intino.plugin.lang.psi.TaraMogram;
import io.intino.plugin.lang.psi.impl.IntinoUtil;
import io.intino.plugin.project.configuration.ArtifactLegioConfiguration;
import io.intino.plugin.project.configuration.model.LegioLanguage;
import io.intino.tara.language.model.Mogram;
import io.intino.tara.language.model.Parameter;

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
import static io.intino.plugin.project.Safe.safe;
import static io.intino.tara.language.semantics.errorcollector.SemanticNotification.Level.ERROR;

public class ArtifactParametersAnalyzer extends TaraAnalyzer {
	private final Mogram artifactNode;
	private final ArtifactLegioConfiguration configuration;

	public ArtifactParametersAnalyzer(Mogram node) {
		this.artifactNode = node;
		this.configuration = (ArtifactLegioConfiguration) IntinoUtil.configurationOf((PsiElement) artifactNode);
	}

	@Override
	public void analyze() {
		Configuration.Artifact.Model model = safe(() -> configuration.artifact().model());
		if (model == null) return;
		Map<String, String> languageParameters = collectLanguageParameters();
		Map<String, String> notFoundParameters = languageParameters.keySet().stream().filter(parameter -> !isDeclared(parameter)).collect(Collectors.toMap(parameter -> parameter, languageParameters::get, (a, b) -> b, LinkedHashMap::new));
		if (!notFoundParameters.isEmpty())
			results.put(((TaraMogram) artifactNode).getSignature(), new AnnotateAndFix(ERROR, message("language.parameters.missing", Level.values()[model.level().ordinal() + 1].name()), new AddParameterFix((PsiElement) artifactNode, notFoundParameters)));
	}

	private boolean isDeclared(String parameter) {
		for (Mogram mogram : artifactNode.components()) {
			final Parameter parameterNode = nameParameter(mogram);
			if (mogram.type().endsWith("Parameter") && parameterNode != null && !parameterNode.values().isEmpty() && parameter.equals(parameterNode.values().get(0).toString()))
				return true;
		}
		return false;
	}

	private Parameter nameParameter(Mogram node) {
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
