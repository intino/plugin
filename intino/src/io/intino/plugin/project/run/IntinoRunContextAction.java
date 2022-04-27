package io.intino.plugin.project.run;

import com.intellij.execution.*;
import com.intellij.execution.actions.ConfigurationContext;
import com.intellij.execution.actions.ConfigurationFromContext;
import com.intellij.execution.actions.RunContextAction;
import com.intellij.execution.application.ApplicationConfiguration;
import com.intellij.execution.configurations.ConfigurationType;
import com.intellij.execution.configurations.LocatableConfiguration;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.runners.ExecutionUtil;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.ListPopup;
import com.intellij.openapi.ui.popup.PopupStep;
import com.intellij.openapi.ui.popup.util.BaseListPopupStep;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.ui.awt.RelativePoint;
import com.intellij.util.concurrency.AppExecutorUtil;
import io.intino.Configuration;
import io.intino.magritte.lang.model.Node;
import io.intino.plugin.lang.psi.impl.IntinoUtil;
import io.intino.plugin.project.configuration.LegioConfiguration;
import io.intino.plugin.project.configuration.model.LegioRunConfiguration;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.util.*;

import static com.intellij.execution.actions.ConfigurationFromContext.NAME_COMPARATOR;

public class IntinoRunContextAction extends RunContextAction {
	private final ConfigurationContext context;
	private final Node runConfiguration;

	public IntinoRunContextAction(@NotNull Executor executor, PsiElement runConfiguration) {
		super(executor);
		this.context = createContext(runConfiguration);
		this.runConfiguration = (Node) runConfiguration;
	}

	@Override
	protected void perform(ConfigurationContext context) {
		final RunManagerEx runManager = (RunManagerEx) context.getRunManager();
		DataContext dataContext = context.getDefaultDataContext();
		ReadAction.nonBlocking(() -> context.findExisting() != null ? context.findExisting() : context.getConfiguration())
				.finishOnUiThread(ModalityState.NON_MODAL, existingConfiguration -> perform(runManager, existingConfiguration, dataContext))
				.submit(AppExecutorUtil.getAppExecutorService());
	}

	private void perform(RunManagerEx runManager,
						 RunnerAndConfigurationSettings configuration,
						 DataContext dataContext) {
		if (runManager.findConfigurationByName(configuration.getName()) == null)
			runManager.addConfiguration(configuration);
		if (runManager.shouldSetRunConfigurationFromContext()) runManager.setSelectedConfiguration(configuration);
		if (LOG.isDebugEnabled()) {
			String configurationClass = configuration.getConfiguration().getClass().getName();
			LOG.debug(String.format("Execute run configuration: %s", configurationClass));
		}
		if (ApplicationManager.getApplication().isUnitTestMode()) return;
		ExecutionUtil.doRunConfiguration(configuration, getExecutor(), null, null, dataContext);
	}

	@Override
	public void actionPerformed(final AnActionEvent e) {
		final DataContext dataContext = e.getDataContext();
		final RunnerAndConfigurationSettings existing = context.findExisting();
		if (existing == null) {
			final List<ConfigurationFromContext> producers = getConfigurationsFromContext();
			if (producers.isEmpty()) return;
			if (producers.size() > 1) {
				final Editor editor = CommonDataKeys.EDITOR.getData(dataContext);
				producers.sort(NAME_COMPARATOR);
				final ListPopup popup =
						JBPopupFactory.getInstance().createListPopup(new BaseListPopupStep<>("Choose Configuration to Run", producers) {
							@Override
							@NotNull
							public String getTextFor(final ConfigurationFromContext producer) {
								return childActionName(producer.getConfigurationType(), producer.getConfiguration());
							}

							@Override
							public Icon getIconFor(final ConfigurationFromContext producer) {
								return producer.getConfigurationType().getIcon();
							}

							@Override
							public PopupStep<?> onChosen(final ConfigurationFromContext producer, final boolean finalChoice) {
								perform(producer);
								return FINAL_CHOICE;
							}
						});
				final InputEvent event = e.getInputEvent();
				if (event instanceof MouseEvent) popup.show(new RelativePoint((MouseEvent) event));
				else if (editor != null) popup.showInBestPositionFor(editor);
				else popup.showInBestPositionFor(dataContext);
			} else {
				perform(producers.get(0));
			}
			return;
		}
		setRunParameters(existing);
		perform(context);
	}

	@NotNull
	private static String childActionName(ConfigurationType configurationType, RunConfiguration configuration) {
		return configuration instanceof LocatableConfiguration
				? StringUtil.unquoteString(suggestRunActionName((LocatableConfiguration) configuration))
				: configurationType.getDisplayName();
	}

	private void perform(final ConfigurationFromContext conf) {
		setRunParameters(conf.getConfigurationSettings());
		conf.onFirstRun(context, () -> perform(context));
	}

	private void setRunParameters(RunnerAndConfigurationSettings configurationSettings) {
		context.setConfiguration(configurationSettings);
		final ApplicationConfiguration configuration = (ApplicationConfiguration) configurationSettings.getConfiguration();
		configuration.setModule(context.getModule());
		configuration.setProgramParameters(collectParameters());
	}

	private String collectParameters() {
		final LegioConfiguration configuration = (LegioConfiguration) configuration();
		if (configuration == null) return "";
		final List<Configuration.RunConfiguration> runConfigurations = configuration.runConfigurations();
		for (Configuration.RunConfiguration rc : runConfigurations)
			if (this.runConfiguration.name().equals(rc.name())) return ((LegioRunConfiguration) rc).argumentsChain();
		return runConfigurations.isEmpty() ? "" : ((LegioRunConfiguration) runConfigurations.get(0)).argumentsChain();
	}

	private Configuration configuration() {
		return IntinoUtil.configurationOf((PsiElement) runConfiguration);
	}

	private ConfigurationContext createContext(@NotNull PsiElement runConfiguration) {
		MapDataContext dataContext = new MapDataContext();
		dataContext.put(CommonDataKeys.PROJECT, runConfiguration.getProject());
		if (LangDataKeys.MODULE.getData(dataContext) == null)
			dataContext.put(LangDataKeys.MODULE, ModuleUtilCore.findModuleForPsiElement(runConfiguration));
		dataContext.put(Location.DATA_KEY, PsiLocation.fromPsiElement(runConfiguration));
		return ConfigurationContext.getFromContext(dataContext, ActionPlaces.UNKNOWN);
	}

	@NotNull
	private List<ConfigurationFromContext> getConfigurationsFromContext() {
		final List<ConfigurationFromContext> fromContext = context.getConfigurationsFromContext();
		if (fromContext == null) return Collections.emptyList();
		final List<ConfigurationFromContext> enabledConfigurations = new ArrayList<>();
		for (ConfigurationFromContext configurationFromContext : fromContext)
			if (isEnabledFor(configurationFromContext.getConfiguration()))
				enabledConfigurations.add(configurationFromContext);
		return enabledConfigurations;
	}

	public static class MapDataContext implements DataContext {
		private final Map<String, Object> myMap = new HashMap<>();

		public MapDataContext() {
		}

		@Override
		public Object getData(@NotNull String dataId) {
			return myMap.get(dataId);
		}

		public void put(@NotNull String dataId, Object data) {
			myMap.put(dataId, data);
		}

		public <T> void put(@NotNull DataKey<T> dataKey, T data) {
			put(dataKey.getName(), data);
		}
	}
}
