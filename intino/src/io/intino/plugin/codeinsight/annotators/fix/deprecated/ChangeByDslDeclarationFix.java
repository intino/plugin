package io.intino.plugin.codeinsight.annotators.fix.deprecated;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.codeInspection.util.IntentionFamilyName;
import com.intellij.codeInspection.util.IntentionName;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.impl.CheckUtil;
import com.intellij.util.IncorrectOperationException;
import io.intino.plugin.lang.psi.TaraElementFactory;
import io.intino.plugin.lang.psi.impl.TaraPsiUtil;
import io.intino.tara.language.model.Mogram;
import io.intino.tara.language.model.Parameter;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.stream.Collectors;

import static com.intellij.openapi.command.WriteCommandAction.writeCommandAction;

public class ChangeByDslDeclarationFix implements IntentionAction {
	@SafeFieldForPreview
	private Mogram mogram;

	public ChangeByDslDeclarationFix(PsiElement element) {
		try {
			this.mogram = element instanceof Mogram ? (Mogram) element : TaraPsiUtil.getContainerNodeOf(element);
		} catch (Throwable e) {
			this.mogram = null;
		}
	}

	@Override
	public @IntentionName @NotNull String getText() {
		return "Change by Dsl declaration";
	}

	@Override
	public boolean isAvailable(@NotNull Project project, Editor editor, PsiFile file) {
		boolean writable = false;
		try {
			CheckUtil.checkWritable(file);
			writable = true;
		} catch (IncorrectOperationException ignored) {
		}
		return file.isValid() && mogram != null && writable;
	}

	@Override
	public void invoke(@NotNull Project project, Editor editor, PsiFile file) throws IncorrectOperationException {
		PsiElement element = (PsiElement) mogram;
		ApplicationManager.getApplication().invokeAndWait(() -> writeCommandAction(project, element.getContainingFile()).run(() -> {
			Map<String, String> parameters = mogram.parameters().stream().collect(Collectors.toMap(Parameter::name, p -> p.values().get(0).toString()));
			TaraElementFactory factory = TaraElementFactory.getInstance(project);
			Mogram dslMogram = factory.createFullMogram("Dsl(name=\"" + parameters.get("language") + "\", version=\"" + version(parameters) + "\")");
			dslMogram.type("Artifact.Dsl");
			element.addAfter((PsiElement) dslMogram, element);
			element.delete();
		}));
	}

	private static String version(Map<String, String> parameters) {
		String lang = parameters.get("language");
		return lang.equalsIgnoreCase("Proteo") ?
				"1.0.0" :
				parameters.get("version");
	}

	@Override
	public boolean startInWriteAction() {
		return false;
	}

	@Override
	public @NotNull @IntentionFamilyName String getFamilyName() {
		return getText();
	}
}
