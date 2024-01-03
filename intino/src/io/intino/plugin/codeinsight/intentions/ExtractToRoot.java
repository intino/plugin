package io.intino.plugin.codeinsight.intentions;

import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.util.IncorrectOperationException;
import io.intino.plugin.lang.psi.Signature;
import io.intino.plugin.lang.psi.TaraSignature;
import io.intino.plugin.lang.psi.impl.TaraPsiUtil;
import io.intino.tara.language.model.Mogram;
import io.intino.tara.language.model.MogramRoot;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

public class ExtractToRoot extends PsiElementBaseIntentionAction {
	@Override
	public void invoke(@NotNull Project project, Editor editor, @NotNull PsiElement element) throws IncorrectOperationException {

	}


	@Override
	public boolean isAvailable(@NotNull Project project, Editor editor, @NotNull PsiElement element) {
		Mogram mogram = TaraPsiUtil.getContainerByType(element, Mogram.class);
		return element.isWritable() && isInSignature(element) && mogram != null && !(mogram.container() instanceof MogramRoot);
	}

	private boolean isInSignature(PsiElement element) {
		return element instanceof Signature || TaraPsiUtil.getContainerByType(element, TaraSignature.class) != null;
	}

	@NotNull
	public String getText() {
		return "Extract element to root TODO";
	}

	@Nls
	@NotNull
	@Override
	public String getFamilyName() {
		return getText();
	}
}
