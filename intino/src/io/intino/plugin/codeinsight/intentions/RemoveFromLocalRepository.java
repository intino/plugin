package io.intino.plugin.codeinsight.intentions;

import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.util.IncorrectOperationException;
import io.intino.plugin.dependencyresolution.DependencyPurger;
import io.intino.plugin.file.legio.LegioFileType;
import io.intino.plugin.project.LegioConfiguration;
import io.intino.tara.lang.model.Node;
import io.intino.tara.lang.model.Parameter;
import io.intino.tara.plugin.lang.psi.impl.TaraPsiImplUtil;
import io.intino.tara.plugin.lang.psi.impl.TaraUtil;
import io.intino.tara.plugin.project.module.ModuleProvider;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class RemoveFromLocalRepository extends PsiElementBaseIntentionAction {


	private boolean isDependency(PsiElement element) {
		Node node = element instanceof Node ? (Node) element : TaraPsiImplUtil.getContainerNodeOf(element);
		return node.simpleType().equals("Compile")
				|| node.simpleType().equals("Test")
				|| node.simpleType().equals("Provided")
				|| node.simpleType().equals("Runtime")
				|| node.simpleType().equals("Artifact.Imports.Compile")
				|| node.simpleType().equals("Artifact.Imports.Test")
				|| node.simpleType().equals("Artifact.Imports.Provided")
				|| node.simpleType().equals("Artifact.Imports.Runtime");
	}

	@Override
	public void invoke(@NotNull Project project, Editor editor, @NotNull PsiElement element) throws IncorrectOperationException {
		Module module = ModuleProvider.moduleOf(element);
		Node node = element instanceof Node ? (Node) element : TaraPsiImplUtil.getContainerNodeOf(element);
		DependencyPurger purger = new DependencyPurger(module, (LegioConfiguration) TaraUtil.configurationOf(module));
		List<Parameter> parameters = node.parameters();
		purger.purgeDependency(parameter(parameters, 0).replace(".", ":") + ":" + parameter(parameters, 1) + ":" + parameter(parameters, 2));
	}

	private String parameter(List<Parameter> parameters, int index) {
		return parameters.get(index).values().get(0).toString();
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
}