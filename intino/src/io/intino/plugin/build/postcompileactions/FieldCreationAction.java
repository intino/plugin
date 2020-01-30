package io.intino.plugin.build.postcompileactions;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import io.intino.plugin.build.PostCompileAction;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public class FieldCreationAction extends PostCompileAction {
	private final File file;
	private final String name;
	private final String type;
	private final boolean isStatic;
	private final String modifier;


	public FieldCreationAction(Module module, List<String> parameters) {
		this(module, new File(parameters.get(0)), parameters.get(0), parameters.get(1), Boolean.parseBoolean(parameters.get(2)), parameters.get(3));
	}

	public FieldCreationAction(Module module, File file, String name, String type, boolean isStatic, String modifier) {
		super(module);
		this.file = file;
		this.name = name;
		this.type = type;
		this.isStatic = isStatic;
		this.modifier = modifier;
	}

	@Override
	public void execute() {
		VirtualFile virtualFile = VfsUtil.findFileByIoFile(file, true);
		if (virtualFile == null) return;
		PsiFile psiFile = PsiManager.getInstance(module.getProject()).findFile(virtualFile);
		if (psiFile == null) return;
		doUpdateFields((PsiClass) psiFile.getFirstChild());
	}


	private void doUpdateFields(PsiClass psiClass) {
		PsiElementFactory elementFactory = JavaPsiFacade.getElementFactory(module.getProject());
		if (Arrays.stream(psiClass.getAllFields()).noneMatch((f) -> name.equalsIgnoreCase(f.getName())))
			psiClass.addAfter(this.createField(psiClass, elementFactory), psiClass.getLBrace().getNextSibling());
	}

	private PsiField createField(PsiClass psiClass, PsiElementFactory elementFactory) {
		PsiField field = elementFactory.createField(name, elementFactory.createTypeFromText(type, psiClass));
		if (field.getModifierList() != null) {
			boolean isPublic = modifier.equalsIgnoreCase("public");
			field.getModifierList().setModifierProperty("public", isPublic);
			field.getModifierList().setModifierProperty("private", !isPublic);
			field.getModifierList().setModifierProperty("static", isStatic);
		}
		return field;
	}
}
