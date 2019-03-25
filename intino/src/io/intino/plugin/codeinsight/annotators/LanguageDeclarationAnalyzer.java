package io.intino.plugin.codeinsight.annotators;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.roots.LibraryOrderEntry;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.OrderEntry;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.psi.PsiElement;
import io.intino.plugin.project.LegioConfiguration;
import io.intino.tara.Language;
import io.intino.tara.compiler.shared.Configuration;
import io.intino.tara.dsl.Proteo;
import io.intino.tara.dsl.Verso;
import io.intino.tara.lang.model.Node;
import io.intino.tara.lang.model.Parameter;
import io.intino.tara.plugin.annotator.TaraAnnotator.AnnotateAndFix;
import io.intino.tara.plugin.annotator.semanticanalizer.TaraAnalyzer;
import io.intino.tara.plugin.lang.LanguageManager;
import io.intino.tara.plugin.lang.psi.TaraNode;
import io.intino.tara.plugin.lang.psi.impl.TaraUtil;

import java.util.Arrays;
import java.util.List;
import java.util.jar.Attributes;
import java.util.stream.Collectors;

import static io.intino.plugin.MessageProvider.message;
import static io.intino.tara.compiler.shared.Configuration.Level.Platform;
import static io.intino.tara.lang.semantics.errorcollector.SemanticNotification.Level.ERROR;

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
		if (configuration == null || modelNode == null || configuration.graph() == null) return;
		final Parameter languageNameParameter = modelNode.parameters().stream().filter(p -> p.name().equals("language")).findFirst().orElse(null);
		if (languageNameParameter == null) return;
		final String languageName = languageNameParameter.values().get(0).toString();
		if (languageName == null) return;
		String version = version();
		if ("LATEST".equals(version)) {
			final Configuration.LanguageLibrary language = configuration.language(l -> languageName.equals(l.name()));
			version = language != null ? language.effectiveVersion() : null;
		}
		if (version == null || version.isEmpty()) return;
		if (configuration.languageParameters() == null) {
			results.put((PsiElement) this.modelNode, new AnnotateAndFix(ERROR, message("language.not.found")));
			return;
		}
		final Language language = LanguageManager.getLanguage(module.getProject(), languageName, version);
		final Configuration.Level languageLevel = languageLevel();
		if ((languageLevel == null && configuration.level() != Platform) || (languageLevel != null && configuration.level() != null && configuration.level().compareLevelWith(languageLevel) != 1))
			results.put((PsiElement) this.modelNode, new AnnotateAndFix(ERROR, message("language.does.not.match", languageLevel == null ? "Verso or Proteo" : languageLevel.name())));
		if (language == null && !LanguageManager.silentReload(module.getProject(), languageName, version))
			results.put((PsiElement) this.modelNode, new AnnotateAndFix(ERROR, message("language.not.found")));
		else if ((language instanceof Verso || language instanceof Proteo) && !existMagritte(version))
			results.put(((TaraNode) this.modelNode).getSignature(), new AnnotateAndFix(ERROR, message("magritte.not.found")));
	}

	private Configuration.Level languageLevel() {
		if (configuration.languages().isEmpty()) return null;
		final String name = configuration.languages().get(0).name();
		if (Verso.class.getSimpleName().equals(name)) return null;
		if (Proteo.class.getSimpleName().equals(name)) return Platform;
		final Attributes attributes = configuration.languageParameters();
		return Configuration.Level.valueOf(attributes.getValue("level"));
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