package io.intino.plugin.build.postcompileactions;

import com.intellij.openapi.module.Module;
import com.intellij.psi.*;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import static java.util.Collections.emptyList;

public class MethodCreationAction extends io.intino.plugin.build.PostCompileAction {
	private final String name;
	private boolean isStatic;
	private List<String> parameters;
	private String returnType;
	private List<String> exceptions;
	private PsiClass psiClass;

	public MethodCreationAction(Module module, List<String> parameters) {
		this(module, new File(parameters.get(0)),
				parameters.get(1),
				Boolean.parseBoolean(parameters.get(2)),
				parameters.get(3).isEmpty() ? emptyList() : List.of(parameters.get(3).split(";")),
				parameters.get(4),
				parameters.size() > 5 ? List.of(parameters.get(5).split(";")) : emptyList());
	}

	public MethodCreationAction(Module module, File file, String name, boolean isStatic, List<String> parameters, String returnType, List<String> exceptions) {
		super(module);
		this.name = name;
		this.isStatic = isStatic;
		this.parameters = parameters;
		this.returnType = returnType;
		this.exceptions = exceptions;
		this.psiClass = findClass(file);
	}

	@Override
	public FinishStatus execute() {
		if (psiClass == null) return FinishStatus.NothingDone;
		PsiMethod method = findMethod(psiClass);
		if (method == null) createMethod();
		else {
			this.updateExceptions(method);
			this.updateReturnType(method);
		}
		return FinishStatus.NothingDone;
	}

	private void createMethod() {
		PsiElementFactory factory = JavaPsiFacade.getElementFactory(module.getProject());
		PsiMethod method = read(() -> factory.createMethodFromText(methodText(), psiClass));
		write(() -> psiClass.addAfter(method, anchor(psiClass)));
	}

	private PsiElement anchor(PsiClass psiClass) {
		if (psiClass.getMethods().length == 0) {
			PsiField[] fields = psiClass.getFields();
			if (fields.length > 0) return fields[fields.length - 1].getNextSibling();
			return psiClass.getLBrace();
		}
		return psiClass.getMethods()[psiClass.getMethods().length - 1];
	}

	private String methodText() {
		return "public " + (isStatic ? "static " : "") + returnType + " " + name + "(" + String.join(", ", parameters) + ") " + exceptions() + "{" +
				"\n" +
				(returnType.equals("void") ? "" : "return null;") + "\n" +
				"}";
	}

	private String exceptions() {
		return this.exceptions.isEmpty() ? "" : String.join(", ", exceptions) + " ";
	}

	private PsiMethod findMethod(PsiClass psiClass) {
		return read(() -> Arrays.stream(psiClass.getMethods()).filter(method -> method.getName().equals(name)).findFirst().orElse(null));
	}

	private void updateExceptions(PsiMethod psiMethod) {
		final PsiElementFactory elementFactory = JavaPsiFacade.getElementFactory(module.getProject());
		exceptions.stream().
				filter(exception -> !hasException(read(() -> psiMethod.getThrowsList().getReferenceElements()), exception)).
				forEach(exception -> write(() -> psiMethod.getThrowsList().add(elementFactory.createReferenceFromText(exception, psiMethod))));
	}

	private boolean hasException(PsiJavaCodeReferenceElement[] referenceElements, String exception) {
		for (PsiJavaCodeReferenceElement referenceElement : referenceElements)
			if (exception.equals(referenceElement.getReferenceName())) return true;
		return false;
	}

	private void updateReturnType(PsiMethod psiMethod) {
		if (read(() -> psiMethod.getReturnType().equalsToText(this.returnType))) return;
		PsiTypeElement returnType = read(() -> psiMethod.getReturnTypeElement());
		PsiTypeElement newReturnType = read(() -> createReturnType(psiMethod));
		write(() -> returnType.replace(newReturnType));

	}

	@NotNull
	private PsiTypeElement createReturnType(PsiMethod psiMethod) {
		PsiElementFactory elementFactory = JavaPsiFacade.getElementFactory(module.getProject());
		return read(() -> elementFactory.createTypeElement(elementFactory.createTypeFromText(this.returnType, psiMethod)));
	}

}
