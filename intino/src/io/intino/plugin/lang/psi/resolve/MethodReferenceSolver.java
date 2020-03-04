package io.intino.plugin.lang.psi.resolve;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import io.intino.plugin.lang.psi.Identifier;
import io.intino.plugin.project.module.ModuleProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

import static com.intellij.psi.search.GlobalSearchScope.moduleScope;
import static io.intino.plugin.lang.psi.impl.IntinoUtil.methodReference;

public class MethodReferenceSolver extends TaraReferenceSolver {
	private final Module module;

	public MethodReferenceSolver(Identifier identifier, TextRange range) {
		super(identifier, range);
		this.module = ModuleProvider.moduleOf(identifier);
	}

	@Override
	protected List<PsiElement> doMultiResolve() {
		if (module == null) return Collections.emptyList();
		final PsiClass aClass = JavaPsiFacade.getInstance(myElement.getProject()).findClass(methodReference(myElement), moduleScope(module));
		if (aClass == null) return Collections.emptyList();
		else return Collections.singletonList(findMethod(aClass.getMethods()));
	}

	private PsiElement findMethod(PsiMethod[] methods) {
		for (PsiMethod method : methods) if (method.getName().equals(myElement.getText())) return method;
		return null;
	}

	@Nullable
	@Override
	public PsiElement resolve() {
		ResolveResult[] resolveResults = multiResolve(false);
		return resolveResults.length == 1 ? resolveResults[0].getElement() : null;
	}

	@NotNull
	@Override
	public Object[] getVariants() {
		return new Object[0];
	}

}
