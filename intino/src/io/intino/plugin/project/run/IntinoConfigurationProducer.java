package io.intino.plugin.project.run;

import com.intellij.codeInsight.TestFrameworks;
import com.intellij.execution.JavaExecutionUtil;
import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.execution.actions.ConfigurationContext;
import com.intellij.execution.actions.ConfigurationFromContext;
import com.intellij.execution.actions.ConfigurationFromContextImpl;
import com.intellij.execution.application.ApplicationConfiguration;
import com.intellij.execution.application.ApplicationConfigurationProducer;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.util.Ref;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.util.PsiTreeUtil;
import io.intino.Configuration;
import io.intino.Configuration.RunConfiguration;
import io.intino.plugin.lang.psi.TaraMogram;
import io.intino.plugin.lang.psi.impl.IntinoUtil;
import io.intino.plugin.lang.psi.impl.TaraPsiUtil;
import io.intino.plugin.project.Safe;
import io.intino.plugin.project.configuration.ArtifactLegioConfiguration;
import io.intino.tara.language.model.Mogram;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

import static com.intellij.psi.search.GlobalSearchScope.allScope;
import static io.intino.Configuration.Artifact;

public class IntinoConfigurationProducer extends ApplicationConfigurationProducer {
	private static final Logger LOG = Logger.getInstance(IntinoConfigurationProducer.class);

	@Override
	protected boolean setupConfigurationFromContext(@NotNull ApplicationConfiguration configuration, @NotNull ConfigurationContext context, Ref<PsiElement> sourceElement) {
		final PsiElement element = sourceElement.get();
		final boolean isSuitable = element instanceof TaraMogram && ((TaraMogram) element).type().equals(RunConfiguration.class.getSimpleName());
		if (isSuitable) {
			final ArtifactLegioConfiguration legio = (ArtifactLegioConfiguration) IntinoUtil.configurationOf(element);
			final PsiClass mainClass = getMainClass(legio, element);
			if (mainClass == null) return false;
			configuration.setName(configurationName(element, legio).toLowerCase());
			setupConfigurationModule(context, configuration);
			configuration.setMainClass(mainClass);
		}
		return isSuitable;
	}

	public boolean isConfigurationFromContext(@NotNull ApplicationConfiguration configuration, ConfigurationContext context) {
		final PsiElement location = context.getPsiLocation();
		if (location == null) return false;
		Configuration conf = IntinoUtil.configurationOf(location);
		if (!(conf instanceof ArtifactLegioConfiguration legio)) return false;
		final PsiClass aClass = getMainClass(legio, location);
		if (aClass != null && Objects.equals(JavaExecutionUtil.getRuntimeQualifiedName(aClass), configuration.getMainClassName())) {
			final PsiMethod method = PsiTreeUtil.getParentOfType(location, PsiMethod.class, false);
			if (method != null && TestFrameworks.getInstance().isTestMethod(method)) return false;
			final Module configurationModule = configuration.getConfigurationModule().getModule();
			if (Objects.equals(context.getModule(), configurationModule) && configuration.getName().equalsIgnoreCase(configurationName(location, legio)))
				return true;
			ApplicationConfiguration template = (ApplicationConfiguration) context.getRunManager().getConfigurationTemplate(getConfigurationFactory()).getConfiguration();
			return Objects.equals(template.getConfigurationModule().getModule(), configurationModule);
		}
		return false;
	}

	@Nullable
	public ConfigurationFromContext createConfigurationFromContext(@NotNull ConfigurationContext context) {
		final RunnerAndConfigurationSettings settings = cloneTemplateConfiguration(context);
		Ref<PsiElement> ref = new Ref<>(context.getPsiLocation());
		try {
			if (!setupConfigurationFromContext((ApplicationConfiguration) settings.getConfiguration(), context, ref))
				return null;
		} catch (ClassCastException e) {
			LOG.error(getConfigurationFactory() + " produced wrong type", e);
			return null;
		} catch (Throwable e) {
			LOG.error(e);
			return null;
		}
		context.setConfiguration(settings);
		return new ConfigurationFromContextImpl(this, settings, ref.get());
	}

	@Override
	public boolean shouldReplace(@NotNull ConfigurationFromContext self, @NotNull ConfigurationFromContext other) {
		return self.getSourceElement().equals(other.getSourceElement());
	}


	@NotNull
	private String configurationName(PsiElement location, ArtifactLegioConfiguration conf) {
		return conf.artifact().name() + "-" + name(location);
	}

	private String name(PsiElement location) {
		if (location instanceof TaraMogram) return ((TaraMogram) location).name();
		else {
			final Mogram mogram = TaraPsiUtil.getContainerNodeOf(location);
			return mogram == null ? null : mogram.name();
		}
	}

	private PsiClass getMainClass(ArtifactLegioConfiguration legio, PsiElement runConfigurationNode) {
		final Artifact.Package safe = Safe.safe(() -> legio.artifact().packageConfiguration());
		if (safe == null || !safe.isRunnable()) return null;
		final JavaPsiFacade facade = JavaPsiFacade.getInstance(runConfigurationNode.getProject());
		return facade.findClass(safe.mainClass(), allScope(runConfigurationNode.getProject()));
	}
}
