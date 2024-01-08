package io.intino.plugin.codeinsight.annotators.legio.analyzers;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.roots.LibraryOrderEntry;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.psi.PsiElement;
import io.intino.Configuration.Artifact.Model;
import io.intino.plugin.IntinoException;
import io.intino.plugin.codeinsight.annotators.TaraAnnotator.AnnotateAndFix;
import io.intino.plugin.codeinsight.annotators.semanticanalizer.TaraAnalyzer;
import io.intino.plugin.lang.LanguageManager;
import io.intino.plugin.lang.psi.TaraMogram;
import io.intino.plugin.lang.psi.impl.IntinoUtil;
import io.intino.plugin.project.builders.ModelBuilderManager;
import io.intino.plugin.project.configuration.ArtifactLegioConfiguration;
import io.intino.plugin.project.configuration.Version;
import io.intino.plugin.project.configuration.model.LegioLanguage;
import io.intino.tara.Language;
import io.intino.tara.dsls.Meta;
import io.intino.tara.dsls.Proteo;
import io.intino.tara.language.model.Mogram;
import io.intino.tara.language.model.Parameter;
import org.apache.commons.io.IOUtils;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Objects;

import static io.intino.plugin.MessageProvider.message;
import static io.intino.plugin.project.Safe.safe;
import static io.intino.tara.language.semantics.errorcollector.SemanticNotification.Level.ERROR;

public class ArtifactModelAnalyzer extends TaraAnalyzer {
	private final Mogram modelNode;
	private final ArtifactLegioConfiguration configuration;
	private final Module module;

	public ArtifactModelAnalyzer(Mogram node, Module module) {
		this.modelNode = node;
		this.module = module;
		this.configuration = (ArtifactLegioConfiguration) IntinoUtil.configurationOf(module);
	}

	@Override
	public void analyze() {
		if (configuration == null || modelNode == null) return;
		final Parameter languageNameParameter = modelNode.parameters().stream().filter(p -> p.name().equals("language")).findFirst().orElse(null);
		if (languageNameParameter == null) return;
		final String languageName = languageNameParameter.values().get(0).toString();
		if (languageName == null) return;
		String version = version();
		Model.Language language = safe(() -> configuration.artifact().model().language());
		if (language == null) {
			results.put((PsiElement) this.modelNode, new AnnotateAndFix(ERROR, message("language.not.found")));
			return;
		}
		if ("LATEST".equals(version)) version = language.effectiveVersion();
		if (version == null || version.isEmpty()) return;
		if (((LegioLanguage) language).parameters() == null) {
			results.put((PsiElement) this.modelNode, new AnnotateAndFix(ERROR, message("language.not.found")));
			return;
		}
		checkLanguage(languageName, version, LanguageManager.getLanguage(module.getProject(), languageName, version));
		checkSdk(safe(() -> configuration.artifact().model().sdkVersion()));
	}

	private void checkSdk(String sdkVersion) {
		try {
			if (sdkVersion == null) return;
			new Version(sdkVersion);
			String version = IOUtils.readLines(Objects.requireNonNull(this.getClass().getResourceAsStream("/minimum_model_sdk.info")), Charset.defaultCharset()).get(0);
			if (sdkVersion.compareTo(version) < 0)
				results.put(((TaraMogram) this.modelNode).getSignature(), new AnnotateAndFix(ERROR, message("sdk.minimum.version", version)));
			if (!ModelBuilderManager.exists(sdkVersion))
				results.put(((TaraMogram) this.modelNode).getSignature(), new AnnotateAndFix(ERROR, message("sdk.version.not.found")));
		} catch (IntinoException e) {
			results.put(((TaraMogram) this.modelNode).getSignature(), new AnnotateAndFix(ERROR, message("sdk.version.not.found")));
		}
	}

	private void checkLanguage(String languageName, String version, Language language) {
		Model model = configuration.artifact().model();
		if (model == null) return;
		if (language == null && !LanguageManager.silentReload(module.getProject(), languageName, version))
			results.put((PsiElement) this.modelNode, new AnnotateAndFix(ERROR, message("language.not.found")));
		else if ((language instanceof Meta || language instanceof Proteo) && !existFramework(Proteo.GROUP_ID + ":" + Proteo.ARTIFACT_ID + ":" + version))
			results.put(((TaraMogram) this.modelNode).getSignature(), new AnnotateAndFix(ERROR, message("framework.cannot.downloaded")));
	}

	private boolean existFramework(String framework) {
		return Arrays.stream(ModuleRootManager.getInstance(module).getOrderEntries())
				.filter(e -> e instanceof LibraryOrderEntry)
				.map(e -> ((LibraryOrderEntry) e).getLibrary())
				.anyMatch(library -> library != null && library.getName() != null && library.getName().endsWith(framework));
	}

	private String version() {
		return modelNode.parameters().stream()
				.filter(parameter -> parameter.name().equals("version"))
				.findFirst()
				.map(parameter -> parameter.values().get(0).toString())
				.orElse(null);
	}
}