package io.intino.plugin.build.postcompileactions;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class MethodCreationAction extends io.intino.plugin.build.PostCompileAction {
	private final String name;
	private boolean isStatic;
	private List<String> parameters;
	private String returnType;
	private List<String> exceptions;
	private PsiClass psiClass;

	public MethodCreationAction(Module module, List<String> parameters) {
		this(module, new File(parameters.get(0)), parameters.get(1), Boolean.parseBoolean(parameters.get(2)), List.of(parameters.get(3).split(";")), parameters.get(4), List.of(parameters.get(5).split(";")));
	}

	public MethodCreationAction(Module module, File file, String name, boolean isStatic, List<String> parameters, String returnType, List<String> exceptions) {
		super(module);
		this.name = name;
		this.isStatic = isStatic;
		this.parameters = parameters;
		this.returnType = returnType;
		this.exceptions = exceptions;
		VirtualFile virtualFile = VfsUtil.findFileByIoFile(file, true);
		if (virtualFile == null) return;
		PsiFile psiFile = PsiManager.getInstance(module.getProject()).findFile(virtualFile);
		if (psiFile == null) return;
		psiClass = (PsiClass) psiFile.getFirstChild();
	}

	@Override
	public void execute() {
		if (psiClass == null) return;
		if (psiClass.getMethods().length > 0) {
			PsiMethod method = findMethod(psiClass);
			if (method == null) {
				createMethod();
			} else {
				this.updateExceptions(method);
				this.updateReturnType(method);
			}
		}
	}

	private void createMethod() {
		PsiElementFactory elementFactory = JavaPsiFacade.getElementFactory(module.getProject());
		elementFactory.createMethodFromText(methodText(), psiClass);
	}

	private String methodText() {
		return "public " + (isStatic ? "static " : "") + returnType + " " + name + "(" + String.join(", ", parameters) + ") " + exceptions() + "{\n\n}";
	}

	private String exceptions() {
		return this.exceptions.isEmpty() ? "" : String.join(", ", exceptions) + " ";
	}


	private PsiMethod findMethod(PsiClass psiClass) {
		return Arrays.stream(psiClass.getMethods()).filter(method -> method.getName().equals(name)).findFirst().orElse(null);
	}

	private void updateExceptions(PsiMethod psiMethod) {
		final PsiElementFactory elementFactory = JavaPsiFacade.getElementFactory(module.getProject());
		exceptions.stream().
				filter(exception -> !hasException(psiMethod.getThrowsList().getReferenceElements(), exception)).
				forEach(exception -> psiMethod.getThrowsList().add(elementFactory.createReferenceFromText(exception, psiMethod)));
	}

	private boolean hasException(PsiJavaCodeReferenceElement[] referenceElements, String exception) {
		for (PsiJavaCodeReferenceElement referenceElement : referenceElements)
			if (exception.equals(referenceElement.getReferenceName())) return true;
		return false;
	}

	private void updateReturnType(PsiMethod psiMethod) {
		if (Objects.requireNonNull(psiMethod.getReturnType()).equalsToText(this.returnType)) return;
		PsiTypeElement returnType = createReturnType(psiMethod);
		psiMethod.getReturnTypeElement().replace(returnType);
	}

	@NotNull
	private PsiTypeElement createReturnType(PsiMethod psiMethod) {
		PsiElementFactory elementFactory = JavaPsiFacade.getElementFactory(module.getProject());
		return elementFactory.createTypeElement(elementFactory.createTypeFromText(this.returnType, psiMethod));
	}

}
