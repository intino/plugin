package io.intino.plugin.codeinsight.annotators.legio.analyzers;

import com.intellij.psi.PsiElement;
import io.intino.Configuration;
import io.intino.plugin.codeinsight.annotators.TaraAnnotator.AnnotateAndFix;
import io.intino.plugin.codeinsight.annotators.legio.fix.AddParameterFix;
import io.intino.plugin.codeinsight.annotators.semanticanalizer.TaraAnalyzer;
import io.intino.plugin.lang.LanguageManager;
import io.intino.plugin.lang.psi.TaraMogram;
import io.intino.plugin.lang.psi.impl.IntinoUtil;
import io.intino.plugin.project.configuration.ArtifactLegioConfiguration;
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
import static io.intino.tara.language.semantics.errorcollector.SemanticNotification.Level.ERROR;

public class ArtifactParametersAnalyzer extends TaraAnalyzer {
	private final Mogram artifactMogram;
	private final ArtifactLegioConfiguration configuration;

	public ArtifactParametersAnalyzer(Mogram mogram) {
		this.artifactMogram = mogram;
		this.configuration = (ArtifactLegioConfiguration) IntinoUtil.configurationOf((PsiElement) artifactMogram);
	}

	@Override
	public void analyze() {
		if (configuration == null || configuration.artifact().dsls().isEmpty()) return;
		Map<String, String> dslParameters = collectDslParameters();
		Map<String, String> notFoundParameters = dslParameters.keySet().stream().filter(parameter -> !isDeclared(parameter)).collect(Collectors.toMap(parameter -> parameter, dslParameters::get, (a, b) -> b, LinkedHashMap::new));
		if (!notFoundParameters.isEmpty())
			results.put(((TaraMogram) artifactMogram).getSignature(),
					new AnnotateAndFix(ERROR, message("dsl.parameters.missing", "Dsl", new AddParameterFix((PsiElement) artifactMogram, notFoundParameters))));
	}

	private boolean isDeclared(String parameter) {
		for (Mogram mogram : artifactMogram.components()) {
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

	private Map<String, String> collectDslParameters() {
		Map<String, String> map = new LinkedHashMap<>();
		for (Configuration.Artifact.Dsl dsl : configuration.artifact().dsls()) {
			if (dsl.name() == null) continue;
			final File languageFile = LanguageManager.getLanguageFile(dsl.name(), dsl.effectiveVersion());
			if (languageFile != null && languageFile.exists()) map.putAll(parameters(languageFile));
		}
		return map;
	}

	private Map<String, String> parameters(File languageFile) {
		Map<String, String> parameters = new HashMap<>();
		try (final JarFile jarFile = new JarFile(languageFile)) {
			final Manifest manifest = jarFile.getManifest();
			add(manifest, "framework", parameters);
			add(manifest, "runtime", parameters);
		} catch (IOException ignored) {
		}
		return parameters;
	}

	private static void add(Manifest manifest, String name, Map<String, String> map) {
		final Attributes attributes = manifest.getAttributes(name);
		if (attributes != null)
			for (Map.Entry<Object, Object> entry : attributes.entrySet())
				map.put(entry.getKey().toString(), entry.getValue().toString());
	}
}
