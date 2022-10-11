package io.intino.plugin.codeinsight.annotators.fix;

import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.util.IncorrectOperationException;
import io.intino.magritte.lang.model.Variable;
import io.intino.plugin.lang.psi.TaraVariable;
import io.intino.plugin.lang.psi.impl.TaraPsiUtil;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

public class LowerCaseVariableFix extends PsiElementBaseIntentionAction {

	private final Variable variable;

	public LowerCaseVariableFix(PsiElement element) {
		this.variable = TaraPsiUtil.getContainerByType(element, TaraVariable.class);
	}

	@Override
	public void invoke(@NotNull Project project, Editor editor, @NotNull PsiElement element) throws IncorrectOperationException {
		if (variable != null) variable.name(firstLowerCase(variable.name()));
	}

	public static String firstLowerCase(String value) {
		return value.substring(0, 1).toLowerCase() + value.substring(1);
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
		return "To lowercase";
	}

}