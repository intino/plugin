package io.intino.plugin.codeinsight.annotators.legio.fix;


import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.util.IncorrectOperationException;
import io.intino.Configuration;
import io.intino.plugin.codeinsight.annotators.fix.WithLiveTemplateFix;
import io.intino.plugin.lang.psi.impl.IntinoUtil;
import io.intino.plugin.lang.psi.impl.TaraPsiUtil;
import io.intino.plugin.project.configuration.model.LegioArtifact;
import io.intino.tara.language.model.Mogram;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class AddParameterFix extends WithLiveTemplateFix implements IntentionAction {
	@SafeFieldForPreview
	private final Mogram node;
	@SafeFieldForPreview
	private final Map<String, String> requiredParameters;

	public AddParameterFix(PsiElement element, Map<String, String> requiredParameters) {
		this.node = element instanceof Mogram ? (Mogram) element : (Mogram) TaraPsiUtil.getContainerOf(element);
		this.requiredParameters = requiredParameters;
	}

	@Nls
	@NotNull
	@Override
	public String getText() {
		return "Add required parameters";
	}

	@Nls
	@NotNull
	@Override
	public String getFamilyName() {
		return getText();
	}

	@Override
	public boolean isAvailable(@NotNull Project project, Editor editor, PsiFile file) {
		return file.isValid();
	}

	@Override
	public void invoke(@NotNull Project project, Editor editor, PsiFile file) throws IncorrectOperationException {
		Configuration configuration = IntinoUtil.configurationOf(file);
		LegioArtifact artifact = (LegioArtifact) configuration.artifact();
		artifact.addParameters(requiredParameters.keySet().toArray(new String[0]));
		commit(file, editor);
	}

	private void commit(PsiFile file, Editor editor) {
		PsiDocumentManager.getInstance(file.getProject()).doPostponedOperationsAndUnblockDocument(editor.getDocument());
	}

	@Override
	public boolean startInWriteAction() {
		return false;
	}
}
