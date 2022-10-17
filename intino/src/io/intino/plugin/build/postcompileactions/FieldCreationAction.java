package io.intino.plugin.build.postcompileactions;

import com.intellij.openapi.module.Module;
import com.intellij.psi.*;
import io.intino.plugin.build.PostCompileAction;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import static io.intino.plugin.build.PostCompileAction.FinishStatus.NothingDone;

public class FieldCreationAction extends PostCompileAction {
	private final File file;
	private final String name;
	private final String type;
	private final boolean isStatic;
	private final String modifier;

	public FieldCreationAction(Module module, List<String> parameters) {
		this(module, new File(parameters.get(0)), parameters.get(1), parameters.get(2), parameters.get(4), Boolean.parseBoolean(parameters.get(3)));
	}

	public FieldCreationAction(Module module, File file, String name, String modifier, String type, boolean isStatic) {
		super(module);
		this.file = file;
		this.name = name;
		this.type = type;
		this.isStatic = isStatic;
		this.modifier = modifier;
	}

	@Override
	public FinishStatus execute() {
		PsiClass psiClass = findClass(this.file);
		if (psiClass == null) return NothingDone;
		doUpdateFields(psiClass);
		return NothingDone;
	}

	private void doUpdateFields(PsiClass psiClass) {
		final PsiField existing = read(() -> Arrays.stream(psiClass.getAllFields())
				.filter(f -> name.equalsIgnoreCase(f.getName())).findFirst().orElse(null));
		if (existing != null && !read(() -> existing.getType().getPresentableText().equals(type)) && !read(() -> existing.getType().getCanonicalText().equals(type)))
			write(existing::delete);
		if (existing == null || !existing.isValid()) createField(psiClass);
	}

	private void createField(PsiClass psiClass) {
		PsiField field = read(() -> this.createField(psiClass, JavaPsiFacade.getElementFactory(module.getProject())));
		write(() -> psiClass.addAfter(field, psiClass.getLBrace().getNextSibling()));
	}

	private PsiField createField(PsiClass psiClass, PsiElementFactory elementFactory) {
		PsiField field = elementFactory.createField(name, elementFactory.createTypeFromText(type, psiClass));
		if (field.getModifierList() != null) {
			boolean isPublic = modifier.equalsIgnoreCase("public");
			field.getModifierList().setModifierProperty(PsiModifier.PUBLIC, isPublic);
			field.getModifierList().setModifierProperty(PsiModifier.PRIVATE, !isPublic);
			field.getModifierList().setModifierProperty(PsiModifier.STATIC, isStatic);
		}
		return field;
	}
}
