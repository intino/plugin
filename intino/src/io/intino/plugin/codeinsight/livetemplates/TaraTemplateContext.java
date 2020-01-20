package io.intino.plugin.codeinsight.livetemplates;

import com.intellij.codeInsight.template.TemplateContextType;
import com.intellij.psi.PsiFile;
import io.intino.plugin.lang.psi.TaraModel;
import org.jetbrains.annotations.NotNull;

public class TaraTemplateContext extends TemplateContextType {
	public TaraTemplateContext() {
		super("Tara", "Tara");
	}

	@Override
	public boolean isInContext(@NotNull PsiFile file, int offset) {
		return file instanceof TaraModel;
	}
}
