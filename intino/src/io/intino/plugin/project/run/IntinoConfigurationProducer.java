package io.intino.plugin.project.run;

import com.intellij.codeInsight.TestFrameworks;
import com.intellij.execution.JavaExecutionUtil;
import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.execution.actions.ConfigurationContext;
import com.intellij.execution.actions.ConfigurationFromContext;
import com.intellij.execution.actions.ConfigurationFromContextImpl;
import com.intellij.execution.application.ApplicationConfiguration;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.junit.JavaRunConfigurationProducerBase;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.util.Ref;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.util.PsiTreeUtil;
import io.intino.plugin.lang.psi.TaraNode;
import io.intino.plugin.lang.psi.impl.TaraPsiUtil;
import io.intino.plugin.lang.psi.impl.TaraUtil;
import io.intino.plugin.project.LegioConfiguration;
import io.intino.plugin.project.Safe;
import io.intino.tara.compiler.shared.Configuration;
import io.intino.tara.compiler.shared.Configuration.RunConfiguration;
import io.intino.tara.lang.model.Node;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.intellij.openapi.util.Comparing.equal;
import static com.intellij.psi.search.GlobalSearchScope.allScope;
import static io.intino.tara.compiler.shared.Configuration.Artifact;

public class IntinoConfigurationProducer extends JavaRunConfigurationProducerBase<ApplicationConfiguration> {
	private static final Logger LOG = Logger.getInstance(IntinoConfigurationProducer.class);

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
		} catch (ProcessCanceledException e) {
			throw e;
		} catch (Throwable e) {
			LOG.error(e);
			return null;
		}
		return new ConfigurationFromContextImpl(this, settings, ref.get());
	}

	@Override
	protected boolean setupConfigurationFromContext(@NotNull ApplicationConfiguration configuration, @NotNull ConfigurationContext context, Ref<PsiElement> sourceElement) {
		final PsiElement element = sourceElement.get();
		final boolean isSuitable = element instanceof TaraNode && ((TaraNode) element).type().equals(RunConfiguration.class.getSimpleName());
		if (isSuitable) {
			final LegioConfiguration legio = (LegioConfiguration) TaraUtil.configurationOf(element);
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
		Configuration conf = TaraUtil.configurationOf(location);
		if (!(conf instanceof LegioConfiguration)) return false;
		final LegioConfiguration legio = (LegioConfiguration) conf;
		final PsiClass aClass = getMainClass(legio, location);
		if (aClass != null && equal(JavaExecutionUtil.getRuntimeQualifiedName(aClass), configuration.getMainClassName())) {
			final PsiMethod method = PsiTreeUtil.getParentOfType(location, PsiMethod.class, false);
			if (method != null && TestFrameworks.getInstance().isTestMethod(method)) return false;
			final Module configurationModule = configuration.getConfigurationModule().getModule();
			if (equal(context.getModule(), configurationModule) && configuration.getName().equalsIgnoreCase(configurationName(location, legio)))
				return true;
			ApplicationConfiguration template = (ApplicationConfiguration) context.getRunManager().getConfigurationTemplate(getConfigurationFactory()).getConfiguration();
			return equal(template.getConfigurationModule().getModule(), configurationModule);
		}
		return false;
	}

	@Override
	public boolean shouldReplace(@NotNull ConfigurationFromContext self, @NotNull ConfigurationFromContext other) {
		return self.getSourceElement().equals(other.getSourceElement());
	}

	@Override
	public boolean isPreferredConfiguration(ConfigurationFromContext self, ConfigurationFromContext other) {
		return super.isPreferredConfiguration(self, other);
	}

	@NotNull
	@Override
	public ConfigurationFactory getConfigurationFactory() {
		return IntinoConfigurationType.getInstance().getConfigurationFactories()[0];
	}

	@NotNull
	private String configurationName(PsiElement location, LegioConfiguration conf) {
		return conf.artifact().name() + "-" + name(location);
	}

	private String name(PsiElement location) {
		if (location instanceof TaraNode) return ((TaraNode) location).name();
		else {
			final Node node = TaraPsiUtil.getContainerNodeOf(location);
			if (node == null) return null;
			return node.name();
		}
	}

	private PsiClass getMainClass(LegioConfiguration legio, PsiElement runConfigurationNode) {
		final Artifact.Package safe = Safe.safe(() -> legio.artifact().packageConfiguration());
		if (safe == null || !safe.isRunnable()) return null;
		final JavaPsiFacade facade = JavaPsiFacade.getInstance(runConfigurationNode.getProject());
		return facade.findClass(safe.mainClass(), allScope(runConfigurationNode.getProject()));
	}
}
