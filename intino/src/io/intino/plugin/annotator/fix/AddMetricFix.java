package io.intino.plugin.annotator.fix;

import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.util.IncorrectOperationException;
import io.intino.plugin.lang.psi.TaraParameter;
import io.intino.plugin.lang.psi.TaraTypes;
import io.intino.plugin.lang.psi.impl.TaraPsiUtil;
import io.intino.tara.lang.model.Parameter;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

class AddMetricFix extends PsiElementBaseIntentionAction {

	private final Parameter parameter;
	private final String[] parameters;

	public AddMetricFix(PsiElement element, String... parameters) {
		this.parameter = findParameterContainer(element);
		this.parameters = parameters;
	}

	public AddMetricFix(PsiElement element) {
		this.parameter = findParameterContainer(element);
		this.parameters = new String[0];
	}

	private Parameter findParameterContainer(PsiElement element) {
		return element instanceof Parameter ? (Parameter) element : TaraPsiUtil.getContainerByType(element.getNode().getElementType().equals(TaraTypes.NEWLINE) ? findParameter(element) : element, TaraParameter.class);
	}

	private PsiElement findParameter(PsiElement element) {
		PsiElement node = element.getPrevSibling();
		while (node.getLastChild() != null) {
			node = node.getLastChild();
		}
		return node;
	}

	@Override
	public void invoke(@NotNull Project project, Editor editor, @NotNull PsiElement element) throws IncorrectOperationException {
		if (parameters != null && parameters.length > 0 && parameter != null) parameter.metric(parameters[0]);
	}

	@Override
	public boolean isAvailable(@NotNull Project project, Editor editor, @NotNull PsiElement element) {
		return element.getContainingFile().isValid();
	}

	@Nls
	@NotNull
	@Override
	public String getFamilyName() {
		return getText();
	}

	@NotNull
	@Override
	public String getText() {
		return "Add metric";
	}

}