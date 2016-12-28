package io.intino.plugin.annotators;

import com.intellij.openapi.module.Module;
import com.intellij.psi.PsiElement;
import io.intino.plugin.project.LegioConfiguration;
import io.intino.tara.plugin.annotator.TaraAnnotator;
import io.intino.tara.plugin.annotator.semanticanalizer.TaraAnalyzer;
import io.intino.tara.plugin.lang.LanguageManager;
import io.intino.tara.plugin.lang.psi.impl.TaraUtil;
import io.intino.tara.lang.model.Node;
import io.intino.tara.lang.model.Parameter;
import io.intino.tara.lang.semantics.errorcollector.SemanticNotification;

import static io.intino.plugin.MessageProvider.message;

class LanguageDeclarationAnalyzer extends TaraAnalyzer {
	private final Node languageNode;
	private final LegioConfiguration configuration;
	private Module module;

	LanguageDeclarationAnalyzer(Node node, Module module) {
		this.languageNode = node;
		this.module = module;
		this.configuration = (LegioConfiguration) TaraUtil.configurationOf(module);
	}

	@Override
	public void analyze() {
		if (configuration == null) return;
		final String language = languageNode.name();
		String version = version();
		if ("LATEST".equals(version)) version = configuration.dslEffectiveVersion();
		if (version == null || language == null) return;
		if (LanguageManager.getLanguage(module.getProject(), language, version) == null && !LanguageManager.silentReload(module.getProject(), language, version))
			results.put((PsiElement) languageNode, new TaraAnnotator.AnnotateAndFix(SemanticNotification.Level.ERROR, message("language.not.found")));
	}

	private String version() {
		for (Parameter parameter : languageNode.parameters())
			if (parameter.name().equals("version")) return parameter.values().get(0).toString();
		return null;
	}
}