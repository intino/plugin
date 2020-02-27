package io.intino.plugin.codeinsight.languageinjection;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.JavaDirectoryService;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.file.PsiDirectoryImpl;
import com.intellij.util.IncorrectOperationException;
import io.intino.magritte.lang.model.Variable;
import io.intino.magritte.lang.model.rules.variable.NativeRule;
import io.intino.plugin.annotator.fix.ClassCreationIntention;
import io.intino.plugin.lang.psi.TaraModel;
import io.intino.plugin.lang.psi.impl.TaraUtil;
import io.intino.plugin.lang.psi.impl.TaraVariableImpl;
import io.intino.plugin.lang.psi.resolve.ReferenceManager;
import io.intino.plugin.project.module.ModuleProvider;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

public class CreateFunctionInterfaceIntention extends ClassCreationIntention {

	private final PsiDirectory srcDirectory;
	private final Module module;
	private final Variable variable;
	private PsiDirectory destiny;

	public CreateFunctionInterfaceIntention(Variable variable) {
		this.variable = variable;
		final VirtualFile srcRoot = TaraUtil.getSrcRoot(ModuleProvider.moduleOf((PsiElement) variable));
		this.srcDirectory = srcRoot == null ? null : new PsiDirectoryImpl((com.intellij.psi.impl.PsiManagerImpl) ((PsiElement) variable).getManager(), srcRoot);
		this.module = ModuleProvider.moduleOf((PsiElement) variable);
		this.destiny = TaraUtil.findFunctionsDirectory(module, TaraUtil.graphPackage(((PsiElement) variable).getContainingFile()));
	}

	@Nls
	@NotNull
	@Override
	public String getText() {
		return "Create function interface";
	}

	@Nls
	@NotNull
	@Override
	public String getFamilyName() {
		return getText();
	}

	@Override
	public boolean isAvailable(@NotNull Project project, Editor editor, @NotNull PsiElement element) {
		return element.getContainingFile() instanceof TaraModel && destiny != null && srcDirectory != null;
	}

	@Override
	public void invoke(@NotNull Project project, Editor editor, @NotNull PsiElement element) throws IncorrectOperationException {
		final PsiClass nativeClass = createNativeClass();
		if (nativeClass != null) nativeClass.navigate(true);
	}

	@Override
	public boolean startInWriteAction() {
		return true;
	}

	private PsiClass createNativeClass() {
		PsiClass aClass = (PsiClass) ReferenceManager.resolveRule(((TaraVariableImpl) variable).getRule());
		if (aClass == null) {
			aClass = JavaDirectoryService.getInstance().createInterface(destiny, ((NativeRule) variable.rule()).interfaceClass());
			if (aClass.getModifierList() != null)
				aClass.getModifierList().addAnnotation("java.lang.FunctionalInterface");
		}
		return aClass;
	}


}
