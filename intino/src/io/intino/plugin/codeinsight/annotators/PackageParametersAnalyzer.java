package io.intino.plugin.codeinsight.annotators;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import io.intino.plugin.project.LegioConfiguration;
import io.intino.tara.compiler.shared.Configuration;
import io.intino.tara.lang.model.Node;
import io.intino.tara.plugin.annotator.semanticanalizer.TaraAnalyzer;
import io.intino.tara.plugin.lang.psi.impl.TaraUtil;

public class PackageParametersAnalyzer extends TaraAnalyzer {
	private final Node packageNode;
	private final Project project;
	private final LegioConfiguration configuration;

	PackageParametersAnalyzer(Node node) {
		this.packageNode = node;
		this.project = ((PsiElement) node).getProject();
		final Configuration configuration = TaraUtil.configurationOf((PsiElement) packageNode);
		this.configuration = configuration instanceof LegioConfiguration ? (LegioConfiguration) configuration : null;
	}

	@Override
	public void analyze() {

		if (packageNode.components().isEmpty()) return;
	}
}
