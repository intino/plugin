package io.intino.plugin.project.run;

import com.intellij.execution.*;
import com.intellij.execution.actions.ConfigurationContext;
import com.intellij.execution.actions.ConfigurationFromContext;
import com.intellij.execution.actions.RunContextAction;
import com.intellij.execution.application.ApplicationConfiguration;
import com.intellij.execution.configurations.ConfigurationType;
import com.intellij.execution.configurations.LocatableConfiguration;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.ListPopup;
import com.intellij.openapi.ui.popup.PopupStep;
import com.intellij.openapi.ui.popup.util.BaseListPopupStep;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.testFramework.MapDataContext;
import com.intellij.ui.awt.RelativePoint;
import io.intino.plugin.project.LegioConfiguration;
import io.intino.tara.compiler.shared.Configuration;
import io.intino.tara.lang.model.Node;
import io.intino.tara.plugin.lang.psi.impl.TaraUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.intellij.execution.actions.ConfigurationFromContext.NAME_COMPARATOR;
import static io.intino.plugin.project.LegioConfiguration.parametersOf;
import static io.intino.plugin.project.Safe.safeList;

@SuppressWarnings("ComponentNotRegistered")
public class IntinoRunContextAction extends RunContextAction {
	private final ConfigurationContext context;
	private Node runConfiguration;

	public IntinoRunContextAction(@NotNull Executor executor, PsiElement runConfiguration) {
		super(executor);
		this.context = createContext(runConfiguration);
		this.runConfiguration = (Node) runConfiguration;
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
						JBPopupFactory.getInstance().createListPopup(new BaseListPopupStep<ConfigurationFromContext>(ExecutionBundle.message("configuration.action.chooser.title"), producers) {
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
							public PopupStep onChosen(final ConfigurationFromContext producer, final boolean finalChoice) {
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

	private void perform(final ConfigurationFromContext configurationFromContext) {
//		configurationFromContext.getConfiguration().setName(runConfiguration.name());
		setRunParameters(configurationFromContext.getConfigurationSettings());
		configurationFromContext.onFirstRun(context, () -> perform(context));
	}

	private void setRunParameters(RunnerAndConfigurationSettings configurationSettings) {
		context.setConfiguration(configurationSettings);
		final ApplicationConfiguration configuration = (ApplicationConfiguration) configurationSettings.getConfiguration();
		configuration.setProgramParameters(collectParameters());
	}

	private String collectParameters() {
		final LegioConfiguration configuration = (LegioConfiguration) configuration();
		if (configuration == null) return "";
		final List<io.intino.legio.graph.RunConfiguration> runConfigurations = safeList(() -> configuration.graph().runConfigurationList());
		for (io.intino.legio.graph.RunConfiguration legioConf : runConfigurations)
			if (this.runConfiguration.name().equals(legioConf.name$())) return parametersOf(legioConf);
		return runConfigurations.isEmpty() ? "" : parametersOf(runConfigurations.get(0));
	}

	@Override
	public void update(final AnActionEvent event) {
		super.update(event);

	}

	private void set(Presentation presentation, boolean b) {
		presentation.setEnabled(b);
		presentation.setVisible(b);
	}

	private Project getProject(AnActionEvent event) {
		return event.getProject() == null ? ((PsiElement) event.getDataContext().getData("psi.Element")).getProject() : event.getProject();
	}

	private Configuration configuration() {
		return TaraUtil.configurationOf((PsiElement) runConfiguration);
	}

	private ConfigurationContext createContext(@NotNull PsiElement runConfiguration) {
		MapDataContext dataContext = new MapDataContext();
		dataContext.put(CommonDataKeys.PROJECT, runConfiguration.getProject());
		if (LangDataKeys.MODULE.getData(dataContext) == null)
			dataContext.put(LangDataKeys.MODULE, ModuleUtilCore.findModuleForPsiElement(runConfiguration));
		dataContext.put(Location.DATA_KEY, PsiLocation.fromPsiElement(runConfiguration));
		return ConfigurationContext.getFromContext(dataContext);
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

	@Override
	public String toString() {
		return super.toString();
	}
}
