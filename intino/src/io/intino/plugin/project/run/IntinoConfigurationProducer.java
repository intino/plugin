package io.intino.plugin.project.run;

import com.intellij.codeInsight.TestFrameworks;
import com.intellij.execution.JavaExecutionUtil;
import com.intellij.execution.actions.ConfigurationContext;
import com.intellij.execution.actions.ConfigurationFromContext;
import com.intellij.execution.application.AbstractApplicationConfigurationProducer;
import com.intellij.execution.application.ApplicationConfiguration;
import com.intellij.execution.application.ApplicationConfigurationType;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.Ref;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;

public class IntinoConfigurationProducer extends AbstractApplicationConfigurationProducer<IntinoRunConfiguration> {


	public IntinoConfigurationProducer(IntinoConfigurationType configurationType) {
		super(configurationType);
	}


	public boolean isConfigurationFromContext(IntinoRunConfiguration configuration, ConfigurationContext context) {
		final PsiElement location = context.getPsiLocation();
		final PsiClass aClass = ApplicationConfigurationType.getMainClass(location);
		if (aClass != null && Comparing.equal(JavaExecutionUtil.getRuntimeQualifiedName(aClass), configuration.MAIN_CLASS_NAME)) {
			final PsiMethod method = PsiTreeUtil.getParentOfType(location, PsiMethod.class, false);
			if (method != null && TestFrameworks.getInstance().isTestMethod(method)) {
				return false;
			}

			final Module configurationModule = configuration.getConfigurationModule().getModule();
			if (Comparing.equal(context.getModule(), configurationModule)) return true;

			ApplicationConfiguration template =
					(ApplicationConfiguration) context.getRunManager().getConfigurationTemplate(getConfigurationFactory()).getConfiguration();
			final Module predefinedModule = template.getConfigurationModule().getModule();
			return Comparing.equal(predefinedModule, configurationModule);
		}
		return false;
	}

	@Override
	public boolean shouldReplace(@NotNull ConfigurationFromContext self, @NotNull ConfigurationFromContext other) {
		return self.getSourceElement().equals(other.getSourceElement());
	}
}
