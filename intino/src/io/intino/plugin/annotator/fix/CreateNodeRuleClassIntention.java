package io.intino.plugin.annotator.fix;

import com.intellij.openapi.application.AccessToken;
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
import io.intino.magritte.lang.model.Node;
import io.intino.magritte.lang.model.Rule;
import io.intino.plugin.codeinsight.languageinjection.helpers.Format;
import io.intino.plugin.lang.psi.TaraModel;
import io.intino.plugin.lang.psi.TaraRule;
import io.intino.plugin.lang.psi.impl.TaraPsiUtil;
import io.intino.plugin.lang.psi.impl.TaraUtil;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("ALL")
public class CreateNodeRuleClassIntention extends ClassCreationIntention {

	private static final String RULES_PACKAGE = ".rules";
	private final Rule rule;
	private final Node node;
	private String rulesPath;

	public CreateNodeRuleClassIntention(Rule rule) {
		this.rule = rule;
		this.node = TaraPsiUtil.getContainerByType((TaraRule) rule, Node.class);
		if (node != null) this.rulesPath = TaraUtil.graphPackage((PsiElement) node).toLowerCase() + RULES_PACKAGE;
	}

	@NotNull
	@Override
	public String getText() {
		if (rule == null) return "Create rule class";
		return "Create rule " + Format.javaValidName().format(((TaraRule) rule).getText());
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
		VirtualFile srcDirectory = getSrcDirectory(TaraUtil.getSourceRoots(file));
		PsiDirectoryImpl srcPsiDirectory = new PsiDirectoryImpl((PsiManagerImpl) file.getManager(), srcDirectory);
		PsiClass aClass = createRuleClass(file, srcPsiDirectory);
		if (aClass != null) aClass.navigate(true);
	}

	public PsiClass createRuleClass(PsiFile file, PsiDirectoryImpl srcPsiDirectory) {
		PsiClass aClass;
		PsiDirectory destiny = findDestination(file, srcPsiDirectory, rulesPath);
		aClass = createClass(destiny, ((TaraRule) rule).getText());
		return aClass;
	}

	public PsiClass createClass(PsiDirectory destiny, String className) {
		PsiFile file = destiny.findFile(className + ".java");
		if (file != null) return null;
		Map<String, String> additionalProperties = new HashMap<>();
		final Application application = ApplicationManager.getApplication();
		if (application.isWriteAccessAllowed()) {
			final AccessToken accessToken = application.acquireWriteActionLock(CreateNodeRuleClassIntention.class);
			final PsiClass aClass = createClass(destiny, className, additionalProperties);
			accessToken.close();
			return aClass;
		} else return ApplicationManager.getApplication().runWriteAction(
				new Computable<PsiClass>() {
					@Override
					public PsiClass compute() {
						return createClass(destiny, className, additionalProperties);
					}
				}
		);
	}

	public PsiClass createClass(PsiDirectory destiny, String className, Map<String, String> additionalProperties) {
		return JavaDirectoryService.getInstance().createClass(destiny, className, "NodeRule", false, additionalProperties);
	}

	@Override
	public boolean startInWriteAction() {
		return true;
	}

}