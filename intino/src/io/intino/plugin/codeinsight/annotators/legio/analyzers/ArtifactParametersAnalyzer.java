package io.intino.plugin.codeinsight.annotators.legio.analyzers;

import com.intellij.psi.PsiElement;
import io.intino.Configuration;
import io.intino.plugin.codeinsight.annotators.TaraAnnotator.AnnotateAndFix;
import io.intino.plugin.codeinsight.annotators.legio.fix.AddParameterFix;
import io.intino.plugin.codeinsight.annotators.semanticanalizer.TaraAnalyzer;
import io.intino.plugin.dependencyresolution.LanguageResolver;
import io.intino.plugin.dependencyresolution.Repositories;
import io.intino.plugin.lang.psi.TaraMogram;
import io.intino.plugin.lang.psi.impl.IntinoUtil;
import io.intino.plugin.project.configuration.ArtifactLegioConfiguration;
import io.intino.tara.language.model.Mogram;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
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
		Set<String> declared = configuration.artifact().parameters().stream().map(Configuration.Parameter::name).collect(Collectors.toSet());
		Map<String, String> notFoundParameters = dslParameters.keySet().stream().filter(p -> !declared.contains(p)).collect(Collectors.toMap(parameter -> parameter, dslParameters::get));
		if (!notFoundParameters.isEmpty())
			results.put(((TaraMogram) artifactMogram).getSignature(),
					new AnnotateAndFix(ERROR, message("dsl.parameters.missing", "Dsl"), new AddParameterFix((PsiElement) artifactMogram, notFoundParameters)));
	}

	private Map<String, String> collectDslParameters() {
		for (Configuration.Artifact.Dsl dsl : configuration.artifact().dsls()) {
			if (dsl.name() == null) continue;
			String runtimeCoors = new LanguageResolver(configuration.module(), Collections.emptyList()).runtimeCoors(dsl);
			String[] parts = runtimeCoors.split(":");
			File file = new File(Repositories.LOCAL, String.join(File.separator, parts[0].replace(".", File.separator), parts[1], parts[2]));
			return parameters(new File(file, parts[1] + "-" + parts[2] + ".jar"));
		}
		return Map.of();
	}

	private Map<String, String> parameters(File languageFile) {
		try (final JarFile jarFile = new JarFile(languageFile)) {
			final Manifest manifest = jarFile.getManifest();
			Attributes parameters = manifest.getAttributes("parameters");
			Map<String, String> keys = parameters.entrySet().stream().collect(Collectors.toMap(e -> e.getKey().toString(), e -> e.getValue().toString(), (o, o2) -> o, LinkedHashMap::new));
			Set<String> names = keys.entrySet().stream().filter(e -> e.getKey().endsWith("_name")).map(Map.Entry::getValue).collect(Collectors.toSet());
			return names.stream().collect(Collectors.toMap(n -> n, n -> keys.getOrDefault(n + "_defaultValue", ""), (n1, n2) -> n1, LinkedHashMap::new));
		} catch (IOException ignored) {
			return Map.of();
		}
	}

}
