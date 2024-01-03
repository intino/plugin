package io.intino.plugin.codeinsight.intentions;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.util.IncorrectOperationException;
import io.intino.plugin.lang.psi.Body;
import io.intino.plugin.lang.psi.TaraMogram;
import io.intino.plugin.lang.psi.TaraVarInit;
import io.intino.plugin.lang.psi.impl.IntinoUtil;
import io.intino.tara.language.model.MogramContainer;
import io.intino.tara.language.model.Parameter;
import io.intino.tara.language.model.Parametrized;
import io.intino.tara.language.semantics.Constraint;
import org.jetbrains.annotations.NotNull;

import static io.intino.plugin.lang.psi.impl.TaraPsiUtil.getContainerByType;

public class FromBodyToExplicitParameters extends ParametersIntentionAction {

	@Override
	public void invoke(@NotNull Project project, Editor editor, @NotNull PsiElement element) throws IncorrectOperationException {
		Parameter varInit = getContainerByType(element, TaraVarInit.class);
		if (varInit == null || parameterExists(varInit) || varInit.name() == null || varInit.values() == null) return;
		final MogramContainer container = varInit.container();
		((Parametrized) container).addParameter(varInit.name(), varInit.facet(), getPosition(varInit), varInit.metric(), varInit.line(), varInit.column(), varInit.values());
		((PsiElement) varInit).getPrevSibling().delete();
		((PsiElement) varInit).delete();
		removeEmptyBody(container);
	}

	private void removeEmptyBody(MogramContainer container) {
		Body body = ((TaraMogram) container).getBody();
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
