package io.intino.plugin.codeinsight.annotators.legio;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.psi.PsiElement;
import io.intino.Configuration.Repository;
import io.intino.magritte.lang.model.Node;
import io.intino.plugin.codeinsight.annotators.TaraAnnotator;
import io.intino.plugin.codeinsight.annotators.legio.analyzers.DuplicatedRepositoryAnalyzer;
import io.intino.plugin.file.LegioFileType;
import org.jetbrains.annotations.NotNull;

public class DuplicatedRepositoryAnnotator extends TaraAnnotator {

	@Override
	public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
		this.holder = holder;
		if (element instanceof Node && element.getContainingFile().getName().endsWith("." + LegioFileType.instance().getDefaultExtension()) &&
				isRepositoriesNode((Node) element))
			analyzeAndAnnotate(new DuplicatedRepositoryAnalyzer((Node) element));
	}

	private boolean isRepositoriesNode(Node element) {
		return element.type().equals("Repository") || element.type().equals(repositories());
	}

	private String repositories() {
		return Repository.class.getCanonicalName().replace(Repository.class.getPackage().getName() + ".", "");
	}
}