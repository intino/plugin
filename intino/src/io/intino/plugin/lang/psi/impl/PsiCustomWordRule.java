package io.intino.plugin.lang.psi.impl;

import com.intellij.openapi.module.Module;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import io.intino.plugin.lang.psi.TaraVariable;
import io.intino.plugin.project.module.ModuleProvider;
import io.intino.tara.lang.model.rules.CustomRule;
import io.intino.tara.lang.model.rules.variable.VariableRule;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PsiCustomWordRule implements VariableRule<Object>, CustomRule {

	private final String destiny;
	private final TaraVariable variable;
	private final PsiClass psiClass;
	private final List<String> words;

	public PsiCustomWordRule(String destiny, TaraVariable variable) {
		this.destiny = destiny;
		this.variable = variable;
		psiClass = findClass();
		words = collectEnums();
	}

	private PsiClass findClass() {
		final Module module = ModuleProvider.moduleOf(variable);
		if (module == null) return null;
		return JavaPsiFacade.getInstance(variable.getProject()).findClass(TaraUtil.graphPackage(variable).toLowerCase() + ".rules." + destiny, GlobalSearchScope.moduleScope(module));
	}

	@Override
	public boolean accept(Object value) {
		if (psiClass != null && !isEnumType() || !(value instanceof List)) return true;
		for (Object o : ((List) value)) if (!words.contains(o.toString())) return false;
		return true;
	}

	public List<String> words() {
		return words;
	}

	@Override
	public List<Object> errorParameters() {
		return new ArrayList<>(words);
	}

	private List<String> collectEnums() {
		List<String> list = new ArrayList<>();
		if (psiClass == null) return Collections.emptyList();
		for (PsiField psiField : psiClass.getFields())
			if (psiField instanceof PsiEnumConstant) list.add(psiField.getName());
		return list;
	}

	private boolean isEnumType() {
		for (PsiClassType psiClassType : psiClass.getImplementsListTypes())
			if (psiClassType.getClassName().equals("Rule") && psiClassType.getParameters().length == 1 &&
					"Enum".equals(psiClassType.getParameters()[0].getPresentableText())) return true;
		return false;
	}

	@Override
	public Class<?> loadedClass() {
		return null;
	}

	@Override
	public void setLoadedClass(Class<?> loadedClass) {

	}

	@Override
	public void classFile(File file) {

	}

	@Override
	public File classFile() {
		return null;
	}

	@Override
	public String externalClass() {
		return psiClass.getQualifiedName();
	}
}
