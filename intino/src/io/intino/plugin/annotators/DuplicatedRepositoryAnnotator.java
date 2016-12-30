package io.intino.plugin.annotators;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.psi.PsiElement;
import io.intino.legio.Project;
import io.intino.plugin.file.legio.LegioFileType;
import io.intino.tara.lang.model.Node;
import io.intino.tara.plugin.annotator.TaraAnnotator;
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
		return element.simpleType().equals("Repositories") || element.simpleType().equals(repositories());
	}

	private String repositories() {
		return Project.Repositories.class.getCanonicalName().replace(Project.Repositories.class.getPackage().getName() + ".", "");
	}
}
