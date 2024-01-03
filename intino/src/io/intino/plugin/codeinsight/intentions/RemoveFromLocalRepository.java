package io.intino.plugin.codeinsight.intentions;

import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.util.IncorrectOperationException;
import io.intino.plugin.dependencyresolution.DependencyPurger;
import io.intino.plugin.file.LegioFileType;
import io.intino.plugin.lang.psi.impl.TaraPsiUtil;
import io.intino.tara.language.model.Mogram;
import io.intino.tara.language.model.Parameter;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class RemoveFromLocalRepository extends PsiElementBaseIntentionAction {
	@Override
	public void invoke(@NotNull Project project, Editor editor, @NotNull PsiElement element) throws IncorrectOperationException {
		DependencyPurger purger = new DependencyPurger();
		List<Parameter> parameters = (element instanceof Mogram ? (Mogram) element : TaraPsiUtil.getContainerNodeOf(element)).parameters();
		purger.purgeDependency(parameter(parameters, 0).replace(".", ":") + ":" + parameter(parameters, 1) + ":" + parameter(parameters, 2));
	}

	@Override
	public boolean isAvailable(@NotNull Project project, Editor editor, @NotNull PsiElement element) {
		return element.getContainingFile().getName().endsWith("." + LegioFileType.instance().getDefaultExtension()) && isDependency(element);
	}

	@Nls(capitalization = Nls.Capitalization.Sentence)
	@NotNull
	@Override
	public String getFamilyName() {
		return "Remove version from local repository";
	}

	@Nls(capitalization = Nls.Capitalization.Sentence)
	@NotNull
	@Override
	public String getText() {
		return "Remove version from local repository";
	}

	private boolean isDependency(PsiElement element) {
		Mogram mogram = element instanceof Mogram ? (Mogram) element : TaraPsiUtil.getContainerNodeOf(element);
		return mogram != null && ("Compile".equals(mogram.type())
				|| "Test".equals(mogram.type())
				|| "Provided".equals(mogram.type())
				|| "Runtime".equals(mogram.type())
				|| "Artifact.Imports.Compile".equals(mogram.type())
				|| "Artifact.Imports.Test".equals(mogram.type())
				|| "Artifact.Imports.Provided".equals(mogram.type())
				|| "Artifact.Imports.Runtime".equals(mogram.type()));
	}

	private String parameter(List<Parameter> parameters, int index) {
		return parameters.get(index).values().get(0).toString();
	}
}