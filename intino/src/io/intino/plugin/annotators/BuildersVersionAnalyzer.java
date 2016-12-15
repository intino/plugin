package io.intino.plugin.annotators;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.psi.PsiElement;
import io.intino.legio.Project;
import io.intino.plugin.MessageProvider;
import io.intino.plugin.project.LegioConfiguration;
import tara.compiler.shared.Configuration;
import tara.intellij.annotator.TaraAnnotator;
import tara.intellij.annotator.semanticanalizer.TaraAnalyzer;
import tara.intellij.lang.psi.impl.TaraUtil;
import tara.intellij.project.module.ModuleProvider;
import tara.lang.model.Node;
import tara.lang.model.Parameter;
import tara.lang.semantics.errorcollector.SemanticNotification;

import java.util.Arrays;
import java.util.List;

class BuildersVersionAnalyzer extends TaraAnalyzer {
	private final Node builderNode;
	private final LegioConfiguration configuration;

	BuildersVersionAnalyzer(Node node, LegioConfiguration configuration) {
		this.builderNode = node;
		this.configuration = configuration;
	}

	@Override
	public void analyze() {
		final String builderVersion = version(builderNode);
		if (builderVersion == null) return;
		for (Module module : otherModules()) {
			final Configuration configuration = TaraUtil.configurationOf(module);
			if (!(configuration instanceof LegioConfiguration)) continue;
			final Project.Factory factory = ((LegioConfiguration) configuration).factory();
			if (factory == null) continue;
			checkBuilders(builderVersion, factory);
		}
	}

	private void checkBuilders(String builderVersion, Project.Factory factory) {
		if (builderNode.simpleType().contains("Interface") && !builderVersion.equals(factory.interface$().version()))
			results.put((PsiElement) builderNode, new TaraAnnotator.AnnotateAndFix(SemanticNotification.Level.ERROR, MessageProvider.message("reject.builder.version")));
		if (builderNode.simpleType().contains("Behavior") && !builderVersion.equals(factory.behavior().version()))
			results.put((PsiElement) builderNode, new TaraAnnotator.AnnotateAndFix(SemanticNotification.Level.ERROR, MessageProvider.message("reject.builder.version")));
	}

	private List<Module> otherModules() {
		final List<Module> modules = Arrays.asList(ModuleManager.getInstance(((PsiElement) builderNode).getProject()).getModules());
		modules.remove(ModuleProvider.moduleOf((PsiElement) builderNode));
		return modules;

	}

	private String version(Node builderNode) {
		for (Parameter parameter : builderNode.parameters())
			if (parameter.name().equals("version")) return parameter.values().get(0).toString();
		return null;
	}

}
