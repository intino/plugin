package io.intino.legio.plugin.annotators;

import io.intino.legio.plugin.project.LegioConfiguration;
import tara.compiler.model.Model;
import tara.intellij.annotator.semanticanalizer.TaraAnalyzer;

public class DependencyAnalyzer extends TaraAnalyzer {
	private final Model model;
	private final LegioConfiguration configuration;

	public DependencyAnalyzer(Model model, LegioConfiguration configuration) {
		this.model = model;
		this.configuration = configuration;
	}

	@Override
	public void analyze() {
//		results.put(model, new TaraAnnotator.AnnotateAndFix(SemanticNotification.Level.ERROR, ""));
	}
}
