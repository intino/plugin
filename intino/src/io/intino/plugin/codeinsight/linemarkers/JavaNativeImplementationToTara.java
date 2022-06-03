package io.intino.plugin.codeinsight.linemarkers;

import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo;
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider;
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder;
import com.intellij.openapi.module.Module;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiJavaFile;
import io.intino.plugin.IntinoIcons;
import io.intino.plugin.codeinsight.languageinjection.helpers.Format;
import io.intino.plugin.lang.psi.impl.IntinoUtil;
import io.intino.plugin.lang.psi.resolve.ReferenceManager;
import io.intino.plugin.project.module.ModuleProvider;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public class JavaNativeImplementationToTara extends RelatedItemLineMarkerProvider {
	private static final String NATIVE_PACKAGE = "natives";

	@Override
	protected void collectNavigationMarkers(@NotNull PsiElement element, Collection<? super RelatedItemLineMarkerInfo<?>> result) {
		if (!(element instanceof PsiClass)) return;
		PsiClass psiClass = (PsiClass) element;
		PsiElement destiny = ReferenceManager.resolveJavaNativeImplementation(psiClass);
		if (destiny == null || !isAvailable(psiClass, outPackage(destiny))) return;
		addResult(element, result, destiny);
	}

	private boolean isAvailable(PsiClass psiClass, String dsl) {
		return psiClass.getDocComment() != null && psiClass.getContainingFile() != null &&
				psiClass.getParent() instanceof PsiJavaFile && correctPackage(psiClass, dsl);
	}

	private boolean correctPackage(PsiClass psiClass, String dsl) {
		final Module module = ModuleProvider.moduleOf(psiClass);
		final String packageName = ((PsiJavaFile) psiClass.getContainingFile()).getPackageName();
		return packageName.startsWith(dsl.toLowerCase() + '.' + NATIVE_PACKAGE) ||
				(module != null && packageName.startsWith(Format.javaValidName().format(module.getName()).toString().toLowerCase() + '.' + NATIVE_PACKAGE));
	}

	private void addResult(@NotNull PsiElement element, Collection<? super RelatedItemLineMarkerInfo<?>> result, PsiElement destiny) {
		result.add(NavigationGutterIconBuilder.create(IntinoIcons.MODEL_16).setTarget(destiny).setTooltipText("Navigate to the native declaration").createLineMarkerInfo(element));
	}

	private String outPackage(@NotNull PsiElement element) {
		return IntinoUtil.modelPackage(element).isEmpty() ? ModuleProvider.moduleOf(element).getName() : IntinoUtil.modelPackage(element);
	}
}