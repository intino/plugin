package io.intino.plugin.codeinsight.livetemplates;

import com.intellij.codeInsight.template.TemplateActionContext;
import com.intellij.codeInsight.template.TemplateContextType;
import io.intino.plugin.lang.psi.TaraModel;
import org.jetbrains.annotations.NotNull;

public class TaraTemplateContext extends TemplateContextType {
	public TaraTemplateContext() {
		super("Tara");
	}

	@Override
	public boolean isInContext(@NotNull TemplateActionContext context) {
		return context.getFile() instanceof TaraModel;
	}
}