package io.intino.plugin.codeinsight.annotators.legio.analyzers;

import com.intellij.psi.PsiElement;
import io.intino.plugin.MessageProvider;
import io.intino.plugin.codeinsight.annotators.TaraAnnotator;
import io.intino.plugin.codeinsight.annotators.semanticanalizer.TaraAnalyzer;
import io.intino.tara.language.model.Mogram;
import io.intino.tara.language.model.Parameter;
import io.intino.tara.language.semantics.errorcollector.SemanticNotification;

import java.util.HashSet;
import java.util.Set;

public class DuplicatedRepositoryAnalyzer extends TaraAnalyzer {
	private final Mogram repositoriesNode;

	public DuplicatedRepositoryAnalyzer(Mogram node) {
		this.repositoriesNode = node;
	}

	@Override
	public void analyze() {
		Set<String> repos = new HashSet<>();
		for (Mogram repoNode : repositoriesNode.components())
			if (!repos.add(repoNode.type() + url(repoNode)))
				results.put((PsiElement) repoNode, new TaraAnnotator.AnnotateAndFix(SemanticNotification.Level.ERROR, MessageProvider.message("duplicated.repository")));
	}

	private String url(Mogram repoNode) {
		for (Parameter parameter : repoNode.parameters())
			if (parameter.name().equals("url")) return parameter.values().get(0).toString();
		return null;
	}
}