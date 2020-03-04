package io.intino.plugin.annotator.fix;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.impl.PsiManagerImpl;
import com.intellij.psi.impl.file.PsiDirectoryImpl;
import com.intellij.util.IncorrectOperationException;
import io.intino.magritte.lang.model.Primitive;
import io.intino.magritte.lang.model.Rule;
import io.intino.magritte.lang.model.Variable;
import io.intino.plugin.codeinsight.languageinjection.helpers.Format;
import io.intino.plugin.lang.psi.TaraModel;
import io.intino.plugin.lang.psi.TaraRule;
import io.intino.plugin.lang.psi.impl.IntinoUtil;
import io.intino.plugin.lang.psi.impl.TaraPsiUtil;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class CreateMetricClassIntention extends ClassCreationIntention {

	private static final String RULES_PACKAGE = ".rules";
	private final Rule rule;
	private String rulesPath;
	private final Variable variable;

	public CreateMetricClassIntention(Rule rule) {
		this.rule = rule;
		this.variable = TaraPsiUtil.getContainerByType((TaraRule) rule, Variable.class);
		if (variable != null)
			this.rulesPath = IntinoUtil.graphPackage((PsiElement) variable).toLowerCase() + RULES_PACKAGE;
	}

	@NotNull
	@Override
	public String getText() {
		return "Create metric class " + Format.javaValidName().format(((TaraRule) rule).getText());
	}

	@NotNull
	@Override
	public String getFamilyName() {
		return "Create metric class";
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

	private PsiClass createRuleClass(PsiFile file, PsiDirectoryImpl srcPsiDirectory) {
		PsiClass aClass;
		PsiDirectory destiny = findDestination(file, srcPsiDirectory, rulesPath);
		aClass = createClass(destiny, ((TaraRule) rule).getText());
		return aClass;
	}

	private PsiClass createClass(PsiDirectory destiny, String className) {
		PsiFile file = destiny.findFile(className + ".java");
		if (file != null) return null;
		Map<String, String> additionalProperties = new HashMap<>();
		additionalProperties.put("TYPE", getRuleType());
		return JavaDirectoryService.getInstance().createClass(destiny, className, "MetricClass", true, additionalProperties);
	}

	private String getRuleType() {
		if (variable.type().equals(Primitive.WORD)) return "Enum";
		if (variable.type().equals(Primitive.RESOURCE)) return "java.io.File";
		return variable.type().javaName();
	}

	@Override
	public boolean startInWriteAction() {
		return true;
	}
}
