package io.intino.plugin.codeinsight.intentions;

import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.util.IncorrectOperationException;
import io.intino.plugin.codeinsight.intentions.dialog.CreateStringValues;
import io.intino.plugin.lang.psi.StringValue;
import io.intino.plugin.lang.psi.impl.TaraPsiUtil;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

public class CreateI18Value extends PsiElementBaseIntentionAction {

	@Override
	public void invoke(@NotNull Project project, Editor editor, @NotNull PsiElement element) throws IncorrectOperationException {
		final CreateStringValues dialog = new CreateStringValues(element, TaraPsiUtil.getContainerByType(element, StringValue.class).getValue());
		dialog.pack();
		dialog.setLocationRelativeTo(dialog.getParent());
		ApplicationManager.getApplication().invokeLater(() -> dialog.setVisible(true));
	}

	@Override
	public boolean isAvailable(@NotNull Project project, Editor editor, @NotNull PsiElement element) {
		final StringValue stringValue = TaraPsiUtil.getContainerByType(element, StringValue.class);
		return stringValue != null;
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
		return "Create i18n value";
	}

}