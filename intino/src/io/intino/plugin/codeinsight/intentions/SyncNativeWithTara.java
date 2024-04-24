package io.intino.plugin.codeinsight.intentions;

import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.util.IncorrectOperationException;
import io.intino.plugin.codeinsight.languageinjection.helpers.Format;
import io.intino.plugin.codeinsight.languageinjection.helpers.QualifiedNameFormatter;
import io.intino.plugin.codeinsight.languageinjection.imports.Imports;
import io.intino.plugin.lang.psi.*;
import io.intino.plugin.lang.psi.impl.IntinoUtil;
import io.intino.plugin.lang.psi.impl.TaraPsiUtil;
import io.intino.plugin.lang.psi.resolve.ReferenceManager;
import io.intino.plugin.project.module.ModuleProvider;
import io.intino.tara.language.model.Primitive;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

public class SyncNativeWithTara extends PsiElementBaseIntentionAction {
	private static final String NATIVE_PACKAGE = "natives";

	@Override
	public boolean isAvailable(@NotNull Project project, Editor editor, @NotNull PsiElement element) {
		final PsiClass psiClass = TaraPsiUtil.getContainerByType(element, PsiClass.class);
		if (psiClass == null) return false;
		final PsiElement destiny = ReferenceManager.resolveJavaNativeImplementation(psiClass);
		final Valued valued = valued(destiny);
		return destiny != null && psiClass.getDocComment() != null &&
				isAvailable(psiClass, IntinoUtil.dslGenerationPackage(destiny)) &&
				valued != null && !valued.values().isEmpty() && (valued.values().get(0) instanceof Primitive.Expression || valued.values().get(0) instanceof Primitive.MethodReference);
	}

	@Override
	public void invoke(@NotNull Project project, Editor editor, @NotNull PsiElement element) throws IncorrectOperationException {
		PsiClass psiClass = TaraPsiUtil.getContainerByType(element, PsiClass.class);
		final PsiElement destiny = ReferenceManager.resolveJavaNativeImplementation(psiClass);
		Valued valued = valued(destiny);
		if (valued == null) {
			error(project, "Cannot find valued node");
			return;
		}
		Value value = valued.getBodyValue() != null ? valued.getBodyValue() : valued.getValue();
		if (value == null || psiClass == null || psiClass.getMethods().length == 0 || psiClass.getAllMethods()[0].getBody() == null) {
			error(project, "Cannot find class");
			return;
		}
		final TaraExpression taraExpression = value instanceof TaraBodyValue ? ((TaraBodyValue) value).getExpression() : getTaraExpression((TaraValue) value);
		if (taraExpression != null) {
			String body = psiClass.getAllMethods()[0].getBody().getText();
			body = body.substring(1, body.length() - 1);
			if (body.startsWith("return ")) body.substring("return ".length());
			taraExpression.updateText(body);
		}

		updateImports(psiClass, valued);
		success(project, psiClass.getQualifiedName());
	}

	@Nullable
	private Valued valued(PsiElement destiny) {
		Valued valued = findValuedScope(destiny);
		if (valued == null) return null;
		if (valued.getBodyValue() == null && valued.getValue() == null) return null;
		return valued;
	}

	private boolean isAvailable(PsiClass psiClass, String graphPackage) {
		return psiClass.getDocComment() != null && psiClass.getContainingFile() != null &&
				psiClass.getParent() instanceof PsiJavaFile &&
				correctPackage(psiClass, graphPackage);
	}

	private boolean correctPackage(PsiClass psiClass, String graphPackage) {
		final Module module = ModuleProvider.moduleOf(psiClass);
		final String packageName = ((PsiJavaFile) psiClass.getContainingFile()).getPackageName();
		return packageName.startsWith(graphPackage.toLowerCase() + '.' + NATIVE_PACKAGE) ||
				packageName.startsWith(Format.javaValidName().format(module.getName()).toString().toLowerCase() + '.' + NATIVE_PACKAGE);
	}

	private TaraExpression getTaraExpression(TaraValue value) {
		return value.getExpressionList().isEmpty() ? null : value.getExpressionList().get(0);
	}

	private void updateImports(PsiClass psiClass, Valued valued) {
		new Imports(valued.getProject()).save(IntinoUtil.importsFile(valued), QualifiedNameFormatter.qnOf(valued), getImports(psiClass.getContainingFile()));
	}

	private Set<String> getImports(PsiFile file) {
		if (file == null) return Collections.emptySet();
		final PsiImportList importList = ((PsiJavaFile) file).getImportList();
		if (importList == null) return Collections.emptySet();
		return Arrays.asList(importList.getAllImportStatements()).stream().map(PsiElement::getText).collect(Collectors.toSet());
	}

	private Valued findValuedScope(PsiElement element) {
		return TaraPsiUtil.getContainerByType(element, Valued.class);
	}

	@Nls
	@NotNull
	@Override
	public String getText() {
		return "Sync native with tara code";
	}

	@Nls
	@NotNull
	@Override
	public String getFamilyName() {
		return getText();
	}

	private void success(Project project, String aClass) {
		Notifications.Bus.notify(new Notification("Intino", "Synced successfully", aClass, NotificationType.INFORMATION), project);
	}

	private void error(Project project, String message) {
		Notifications.Bus.notify(new Notification("Intino", "Error syncing", message, NotificationType.ERROR), project);
	}

	@Override
	public boolean startInWriteAction() {
		return true;
	}
}
