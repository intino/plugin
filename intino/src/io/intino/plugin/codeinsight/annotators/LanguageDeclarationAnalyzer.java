package io.intino.plugin.codeinsight.annotators;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.roots.LibraryOrderEntry;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.OrderEntry;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.psi.PsiElement;
import io.intino.plugin.project.LegioConfiguration;
import io.intino.tara.Language;
import io.intino.tara.dsl.Proteo;
import io.intino.tara.dsl.Verso;
import io.intino.tara.lang.model.Node;
import io.intino.tara.lang.model.Parameter;
import io.intino.tara.lang.semantics.errorcollector.SemanticNotification;
import io.intino.tara.plugin.annotator.TaraAnnotator;
import io.intino.tara.plugin.annotator.semanticanalizer.TaraAnalyzer;
import io.intino.tara.plugin.lang.LanguageManager;
import io.intino.tara.plugin.lang.psi.TaraNode;
import io.intino.tara.plugin.lang.psi.impl.TaraUtil;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static io.intino.plugin.MessageProvider.message;

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
		if ("LATEST".equals(version))
			version = configuration.language(l -> l.name().equals(languageName)).effectiveVersion();
		if (version == null || version.isEmpty()) return;
		final Language language = LanguageManager.getLanguage(module.getProject(), languageName, version);
		if (language == null && !LanguageManager.silentReload(module.getProject(), languageName, version))
			results.put((PsiElement) this.modelNode, new TaraAnnotator.AnnotateAndFix(SemanticNotification.Level.ERROR, message("language.not.found")));
		else if ((language instanceof Verso || language instanceof Proteo) && !existMagritte(version))
			results.put(((TaraNode) this.modelNode).getSignature(), new TaraAnnotator.AnnotateAndFix(SemanticNotification.Level.ERROR, message("magritte.not.found")));
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