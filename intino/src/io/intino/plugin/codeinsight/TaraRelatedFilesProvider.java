package io.intino.plugin.codeinsight;

import com.intellij.navigation.GotoRelatedItem;
import com.intellij.navigation.GotoRelatedProvider;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.uiDesigner.compiler.Utils;
import io.intino.plugin.lang.file.TaraFileType;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

import static io.intino.plugin.project.Safe.safe;

public class TaraRelatedFilesProvider extends GotoRelatedProvider {
	private static final Logger LOG = Logger.getInstance(TaraRelatedFilesProvider.class.getName());

	@NotNull
	@Override
	public List<? extends GotoRelatedItem> getItems(@NotNull PsiElement context) {
		PsiClass psiClass = PsiTreeUtil.getParentOfType(context, PsiClass.class, false);
		if (psiClass != null) while (psiClass != null) psiClass = PsiTreeUtil.getParentOfType(psiClass, PsiClass.class);
		else if (TaraFileType.instance().equals(safe(() -> context.getContainingFile().getFileType())))
			return findClass(context.getContainingFile());
		return Collections.emptyList();
	}

	@NotNull
	private List<? extends GotoRelatedItem> findClass(PsiFile file) {
		try {
			String className = Utils.getBoundClassName(file.getText());
			if (className != null) {
				Project project = file.getProject();
				PsiClass aClass = JavaPsiFacade.getInstance(project).findClass(className, GlobalSearchScope.allScope(project));
				return aClass != null ? Collections.singletonList(new GotoRelatedItem(aClass, "Java")) : Collections.emptyList();
			}
		} catch (Exception e) {
			LOG.info(e.getMessage(), e);
		}
		return Collections.emptyList();
	}

}
