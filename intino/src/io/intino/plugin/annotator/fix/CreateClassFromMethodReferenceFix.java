package io.intino.plugin.annotator.fix;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.intellij.util.IncorrectOperationException;
import io.intino.plugin.codeinsight.intentions.MethodReferenceCreator;
import io.intino.plugin.lang.psi.IdentifierReference;
import io.intino.plugin.lang.psi.TaraModel;
import io.intino.plugin.lang.psi.TaraTypes;
import io.intino.plugin.lang.psi.Valued;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

public class CreateClassFromMethodReferenceFix extends ClassCreationIntention {
	private final Valued valued;

	public CreateClassFromMethodReferenceFix(Valued valued) {
		this.valued = valued;

	}

	@Nls
	@NotNull
	@Override
	public String getText() {
		return "Create method reference";
	}

	@Nls
	@NotNull
	@Override
	public String getFamilyName() {
		return getText();
	}

	@Override
	public boolean isAvailable(@NotNull Project project, Editor editor, @NotNull PsiElement element) {
		return element.getContainingFile() instanceof TaraModel;
	}

	@Override
	public void invoke(@NotNull Project project, Editor editor, @NotNull PsiElement element) throws IncorrectOperationException {
		final PsiMethod method = new MethodReferenceCreator(valued, isReference(element) ? element.getText() : findReference(element)).create("");
		if (method != null) {
			QuickEditHandler handler = new QuickEditHandler(project, editor, method.getContainingFile(), method);
			if (!ApplicationManager.getApplication().isUnitTestMode()) handler.navigate();
		}
	}

	private boolean isReference(@NotNull PsiElement element) {
		return element instanceof IdentifierReference || element.getNode().getElementType().equals(TaraTypes.IDENTIFIER_KEY);
	}

	private String findReference(PsiElement element) {
		return element.getPrevSibling().getLastChild().getLastChild().getLastChild().getText();
	}

}
