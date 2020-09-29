package io.intino.plugin.codeinsight.intentions;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.util.IncorrectOperationException;
import io.intino.magritte.lang.model.NodeContainer;
import io.intino.magritte.lang.model.Parameter;
import io.intino.magritte.lang.model.Parametrized;
import io.intino.magritte.lang.semantics.Constraint;
import io.intino.plugin.lang.psi.Body;
import io.intino.plugin.lang.psi.TaraNode;
import io.intino.plugin.lang.psi.TaraVarInit;
import io.intino.plugin.lang.psi.impl.IntinoUtil;
import org.jetbrains.annotations.NotNull;

import static io.intino.plugin.lang.psi.impl.TaraPsiUtil.getContainerByType;

public class FromBodyToExplicitParameters extends ParametersIntentionAction {

	@Override
	public void invoke(@NotNull Project project, Editor editor, @NotNull PsiElement element) throws IncorrectOperationException {
		Parameter varInit = getContainerByType(element, TaraVarInit.class);
		if (varInit == null || parameterExists(varInit) || varInit.name() == null || varInit.values() == null) return;
		final NodeContainer container = varInit.container();
		((Parametrized) container).addParameter(varInit.name(), varInit.aspect(), getPosition(varInit), varInit.metric(), varInit.line(), varInit.column(), varInit.values());
		((PsiElement) varInit).getPrevSibling().delete();
		((PsiElement) varInit).delete();
		removeEmptyBody(container);
	}

	private void removeEmptyBody(NodeContainer container) {
		Body body = ((TaraNode) container).getBody();
		if (body != null && isEmpty(body)) body.delete();
	}

	private boolean isEmpty(Body body) {
		return body.getStatements().isEmpty();
	}

	private int getPosition(Parameter parameter) {
		final Constraint.Parameter correspondingConstraint = IntinoUtil.parameterConstraintOf(parameter);
		return correspondingConstraint == null ? 0 : correspondingConstraint.position();
	}

	private boolean parameterExists(Parameter parameter) {
		for (Parameter p : parameter.container().parameters())
			if (!p.equals(parameter) && parameter.name().equals(p.name())) return true;
		return false;
	}

	@Override
	public boolean isAvailable(@NotNull Project project, Editor editor, @NotNull PsiElement element) {
		final TaraVarInit varInit = getContainerByType(element, TaraVarInit.class);
		return element.isWritable() && varInit != null && varInit.getBodyValue() == null;
	}


	@NotNull
	public String getText() {
		return "Move to header";
	}
}
