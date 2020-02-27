package io.intino.plugin.codeinsight.annotators;

import com.intellij.psi.PsiElement;
import io.intino.magritte.lang.model.Node;
import io.intino.magritte.lang.model.Parameter;
import io.intino.magritte.lang.semantics.errorcollector.SemanticNotification;
import io.intino.plugin.MessageProvider;
import io.intino.plugin.annotator.TaraAnnotator;
import io.intino.plugin.annotator.semanticanalizer.TaraAnalyzer;

import java.util.HashSet;
import java.util.Set;

class DuplicatedRepositoryAnalyzer extends TaraAnalyzer {
	private final Node repositoriesNode;

	DuplicatedRepositoryAnalyzer(Node node) {
		this.repositoriesNode = node;
	}

	@Override
	public void analyze() {
		Set<String> repos = new HashSet<>();
		for (Node repoNode : repositoriesNode.components())
			if (!repos.add(repoNode.type() + url(repoNode)))
				results.put((PsiElement) repoNode, new TaraAnnotator.AnnotateAndFix(SemanticNotification.Level.ERROR, MessageProvider.message("duplicated.repository")));
	}

	private String url(Node repoNode) {
		for (Parameter parameter : repoNode.parameters())
			if (parameter.name().equals("url")) return parameter.values().get(0).toString();
		return null;
	}
}