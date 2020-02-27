package io.intino.plugin.codeinsight.annotators;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.roots.LibraryOrderEntry;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.OrderEntry;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.psi.PsiElement;
import io.intino.Configuration.Artifact.Model;
import io.intino.alexandria.logger.Logger;
import io.intino.magritte.Language;
import io.intino.magritte.dsl.Meta;
import io.intino.magritte.dsl.Proteo;
import io.intino.magritte.lang.model.Node;
import io.intino.magritte.lang.model.Parameter;
import io.intino.plugin.annotator.TaraAnnotator.AnnotateAndFix;
import io.intino.plugin.annotator.semanticanalizer.TaraAnalyzer;
import io.intino.plugin.lang.LanguageManager;
import io.intino.plugin.lang.psi.TaraNode;
import io.intino.plugin.lang.psi.impl.TaraUtil;
import io.intino.plugin.project.LegioConfiguration;
import io.intino.plugin.project.configuration.model.LegioLanguage;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.jar.Attributes;
import java.util.stream.Collectors;

import static io.intino.magritte.lang.semantics.errorcollector.SemanticNotification.Level.ERROR;
import static io.intino.plugin.MessageProvider.message;
import static io.intino.plugin.project.Safe.safe;

class LanguageDeclarationAnalyzer extends TaraAnalyzer {
	private final Node modelNode;
	private final LegioConfiguration configuration;
	private Module module;

	LanguageDeclarationAnalyzer(Node node, Module module) {
		this.modelNode = node;
		this.module = module;
		this.configuration = (LegioConfiguration) TaraUtil.configurationOf(module);
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
		if (language == null) return;
		if ("LATEST".equals(version)) version = language.effectiveVersion();
		if (version == null || version.isEmpty()) return;
		if (((LegioLanguage) language).parameters() == null) {
			results.put((PsiElement) this.modelNode, new AnnotateAndFix(ERROR, message("language.not.found")));
			return;
		}
		checkLanguage(languageName, version, LanguageManager.getLanguage(module.getProject(), languageName, version));
		checkSdk(configuration.artifact().model().sdkVersion());
	}

	private void checkSdk(String sdkVersion) {
		try {
			String version = IOUtils.readLines(this.getClass().getResourceAsStream("/minimum_sdk.info"), Charset.defaultCharset()).get(0);
			if (sdkVersion.compareTo(version) < 0)
				results.put(((TaraNode) this.modelNode).getSignature(), new AnnotateAndFix(ERROR, message("sdk.minimun.version", version)));
		} catch (IOException e) {
			Logger.error(e);
		}
	}

	private void checkLanguage(String languageName, String version, Language language) {
		Model model = configuration.artifact().model();
		final Model.Level languageLevel = languageLevel(model);
		if ((languageLevel == null && !model.level().isPlatform()) || (languageLevel != null && model.level() != null && model.level().compareLevelWith(model.level()) != 1))
			results.put((PsiElement) this.modelNode, new AnnotateAndFix(ERROR, message("language.does.not.match", model.level() == null ? "Meta or Proteo" : languageLevel.name())));
		if (language == null && !LanguageManager.silentReload(module.getProject(), languageName, version))
			results.put((PsiElement) this.modelNode, new AnnotateAndFix(ERROR, message("language.not.found")));
		else if ((language instanceof Meta || language instanceof Proteo) && !existMagritte(version))
			results.put(((TaraNode) this.modelNode).getSignature(), new AnnotateAndFix(ERROR, message("magritte.not.found")));
	}

	private Model.Level languageLevel(Model model) {
		LegioLanguage language = (LegioLanguage) model.language();
		if (language == null) return null;
		final String name = language.name();
		if (Meta.class.getSimpleName().equals(name)) return null;
		if (Proteo.class.getSimpleName().equals(name)) return Model.Level.Platform;
		final Attributes attributes = language.parameters();
		return Model.Level.valueOf(attributes.getValue("level"));
	}

	private boolean existMagritte(String version) {
		final List<OrderEntry> entries = Arrays.stream(ModuleRootManager.getInstance(module).getOrderEntries()).filter(e -> e instanceof LibraryOrderEntry).collect(Collectors.toList());
		for (OrderEntry entry : entries) {
			final Library library = ((LibraryOrderEntry) entry).getLibrary();
			if (library != null && library.getName() != null && library.getName().endsWith(Proteo.GROUP_ID + ":" + Proteo.ARTIFACT_ID + ":" + version))
				return true;
		}
		return false;
	}

	private String version() {
		for (Parameter parameter : modelNode.parameters())
			if (parameter.name().equals("version")) return parameter.values().get(0).toString();
		return null;
	}
}