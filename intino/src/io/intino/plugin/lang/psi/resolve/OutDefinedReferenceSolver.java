package io.intino.plugin.lang.psi.resolve;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import io.intino.plugin.lang.psi.impl.TaraPsiUtil;
import io.intino.plugin.lang.psi.impl.TaraUtil;
import io.intino.plugin.project.module.ModuleProvider;
import io.intino.tara.lang.model.Metric;
import io.intino.tara.lang.model.Primitive;
import io.intino.tara.lang.model.Variable;
import io.intino.tara.lang.model.rules.NodeRule;
import io.intino.tara.lang.model.rules.variable.NativeObjectRule;
import io.intino.tara.lang.model.rules.variable.VariableRule;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static io.intino.tara.lang.model.Primitive.FUNCTION;
import static io.intino.tara.lang.model.Primitive.OBJECT;
import static java.util.Collections.singletonList;

public class OutDefinedReferenceSolver extends TaraReferenceSolver {
	private final Module module;
	private String outputDsl;

	public OutDefinedReferenceSolver(@NotNull PsiElement element, TextRange range) {
		super(element, range);
		this.module = ModuleProvider.moduleOf(element);
		this.outputDsl = TaraUtil.graphPackage(element);
	}

	@Override
	protected List<PsiElement> doMultiResolve() {
		if (outputDsl == null || module == null) return Collections.emptyList();
		return singletonList(JavaPsiFacade.getInstance(myElement.getProject()).findClass(reference(), GlobalSearchScope.moduleWithDependenciesAndLibrariesScope(module)));
	}

	@NotNull
	private String reference() {
		Variable variable = TaraPsiUtil.getContainerByType(myElement, Variable.class);
		if (variable == null) return outputDsl.toLowerCase() + ".rules." + myElement.getText();
		else if (OBJECT.equals(variable.type())) return ((NativeObjectRule) variable.rule()).type();
		else return getPackage(variable.type()) + "." + myElement.getText();
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
		final Variable variableContainer = TaraPsiUtil.getContainerByType(myElement, Variable.class);
		if (variableContainer != null) return variableVariants(variableContainer);
		return nodeRuleVariants();
	}

	private Object[] nodeRuleVariants() {
		if (outputDsl == null) return new Object[0];
		final JavaPsiFacade java = JavaPsiFacade.getInstance(myElement.getProject());
		return classes(java.findPackage(outputDsl.toLowerCase() + ".rules"), NodeRule.class).toArray();
	}

	private Object[] variableVariants(Variable variable) {
		final JavaPsiFacade java = JavaPsiFacade.getInstance(myElement.getProject());
		if (variable.type() == null) return new Object[0];
		return variableClasses(java.findPackage(getPackage(variable.type())), variable.type().javaName(), variable.type().equals(FUNCTION) ? new Class[0] : new Class[]{VariableRule.class, Metric.class}).toArray();
	}

	private List<String> variableClasses(PsiPackage aPackage, String type, Class[] ruleClasses) {
		List<String> list = new ArrayList<>();
		if (aPackage == null) return Collections.emptyList();
		for (PsiClass psiClass : aPackage.getClasses()) {
			if (implementsClass(psiClass.getImplementsListTypes(), type, ruleClasses))
				list.add(psiClass.getName());
		}
		return list;
	}

	private List<String> classes(PsiPackage aPackage, Class ruleClass) {
		List<String> list = new ArrayList<>();
		if (aPackage == null) return Collections.emptyList();
		for (PsiClass psiClass : aPackage.getClasses()) {
			if (implementsClass(psiClass.getImplementsListTypes(), ruleClass.getSimpleName()))
				list.add(psiClass.getName());
		}
		return list;
	}

	private boolean implementsClass(PsiClassType[] types, String name) {
		for (PsiClassType type : types)
			if (type.getClassName().equals(name)) return true;
		return false;
	}

	private boolean implementsClass(PsiClassType[] types, String variableType, Class[] classes) {
		if (classes.length == 0) return true;
		for (PsiClassType type : types)
			for (Class aClass : classes)
				if (type.getClassName().equals(aClass.getSimpleName()) && type.getParameters()[0].getPresentableText().equals(variableType))
					return true;
		return false;
	}

	@NotNull
	private String getPackage(Primitive type) {
		return type == null || outputDsl == null ? "" :
				FUNCTION.equals(type) ?
						outputDsl.toLowerCase() + ".functions" :
						outputDsl.toLowerCase() + ".rules";
	}

}
