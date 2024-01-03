package io.intino.plugin.codeinsight.annotators.imports;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.intellij.util.IncorrectOperationException;
import io.intino.plugin.lang.psi.Identifier;
import io.intino.plugin.lang.psi.TaraModel;
import io.intino.plugin.lang.psi.impl.IntinoUtil;
import io.intino.tara.language.model.Mogram;
import io.intino.tara.language.model.MogramRoot;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;

public class AlternativesForReferenceFix implements IntentionAction {
	private final Identifier element;

	public AlternativesForReferenceFix(Identifier element) {
		this.element = element;
	}

	@NotNull
	@Override
	public String getText() {
		return "Find alternatives";
	}

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
		final List<Mogram> candidates = IntinoUtil.getAllNodesOfFile((TaraModel) file).stream().
				filter(c -> element.getText().equals(c.name()) && !c.isAnonymous() && !isInAnonymous(c)).
				collect(Collectors.toList());
	}

	private boolean isInAnonymous(Mogram node) {
		Mogram aNode = node;
		while (!(aNode.container() instanceof MogramRoot)) {
			if (aNode.isAnonymous()) return true;
			aNode = aNode.container();
		}
		return false;
	}

	@Override
	public boolean startInWriteAction() {
		return true;
	}
}
