package io.intino.plugin.codeinsight.annotators.fix;

import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.impl.PsiManagerImpl;
import com.intellij.psi.impl.file.PsiDirectoryImpl;
import com.intellij.util.IncorrectOperationException;
import io.intino.plugin.codeinsight.languageinjection.helpers.Format;
import io.intino.plugin.lang.psi.TaraModel;
import io.intino.plugin.lang.psi.TaraRule;
import io.intino.plugin.lang.psi.impl.IntinoUtil;
import io.intino.plugin.lang.psi.impl.TaraPsiUtil;
import io.intino.tara.language.model.Mogram;
import io.intino.tara.language.model.Primitive;
import io.intino.tara.language.model.Rule;
import io.intino.tara.language.model.Variable;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("ALL")
public class CreateVariableRuleClassIntention extends ClassCreationIntention {
	private final Rule rule;
	private final Variable variable;
	private String rulesPath;

	public CreateVariableRuleClassIntention(Rule rule) {
		this.rule = rule;
		this.variable = TaraPsiUtil.getContainerByType((TaraRule) rule, Variable.class);
		if (variable != null)
			this.rulesPath = IntinoUtil.dslGenerationPackage((PsiElement) variable).toLowerCase() + RULES_PACKAGE;
	}

	@NotNull
	@Override
	public String getText() {
		if (variable.type() == null || rule == null) return "Create rule class";
		return "Create " + variable.type().getName() + " rule class " + Format.javaValidName().format(((TaraRule) rule).getText());
	}

	@NotNull
	@Override
	public String getFamilyName() {
		return "Create rule class";
	}

	@Override
	public boolean isAvailable(@NotNull Project project, Editor editor, @NotNull PsiElement element) {
		return element.getContainingFile() instanceof TaraModel;
	}

	@Override
	public void invoke(@NotNull Project project, Editor editor, @NotNull PsiElement element) throws IncorrectOperationException {
		final PsiFile file = element.getContainingFile();
		VirtualFile srcDirectory = getSrcDirectory(IntinoUtil.getSourceRoots(file));
		PsiDirectoryImpl srcPsiDirectory = new PsiDirectoryImpl((PsiManagerImpl) file.getManager(), srcDirectory);
		PsiClass aClass = createRuleClass(file, srcPsiDirectory);
		if (aClass != null) aClass.navigate(true);
	}

	public PsiClass createRuleClass(PsiFile file, PsiDirectoryImpl srcPsiDirectory) {
		return createClass(findDestination(file, srcPsiDirectory, rulesPath), ((TaraRule) rule).getText());
	}

	public PsiClass createClass(PsiDirectory destination, String className) {
		PsiFile file = destination.findFile(className + ".java");
		if (file != null) return null;
		Map<String, String> additionalProperties = new HashMap<>();
		additionalProperties.put("TYPE", getRuleType());
		Application application = ApplicationManager.getApplication();
		return application.runWriteAction((Computable<PsiClass>) () -> createClass(destination, className, additionalProperties));
	}

	private PsiClass createClass(PsiDirectory destiny, String className, Map<String, String> additionalProperties) {
		return JavaDirectoryService.getInstance().createClass(destiny, className, variable.type().equals(Primitive.WORD) ? "WordRule" : "Rule", true, additionalProperties);
	}

	public String getRuleType() {
		if (variable.type().equals(Primitive.WORD)) return "Enum";
		if (variable.type().equals(Primitive.RESOURCE)) return "java.io.File";
		if (variable.type().equals(Primitive.REFERENCE)) return Mogram.class.getName();
		return variable.type().javaName();
	}

	@Override
	public boolean startInWriteAction() {
		return true;
	}

	@Override
	public String toString() {
		return "CreateRuleClassIntention{" +
				"rule=" + rule +
				'}';
	}
}
