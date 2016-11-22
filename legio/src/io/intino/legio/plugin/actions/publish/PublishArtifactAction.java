package io.intino.legio.plugin.actions.publish;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ui.configuration.ChooseModulesDialog;
import com.intellij.openapi.ui.Messages;
import io.intino.legio.plugin.LegioIcons;
import org.jetbrains.annotations.NotNull;
import tara.intellij.lang.psi.impl.TaraUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static io.intino.legio.plugin.MessageProvider.message;

public class PublishArtifactAction extends AnAction implements DumbAware {

	@Override
	public void actionPerformed(@NotNull AnActionEvent e) {
		final Project project = e.getData(CommonDataKeys.PROJECT);
		if (project == null) return;
		publish(project);
	}

	@Override
	public void update(@NotNull AnActionEvent e) {
		final Project project = e.getData(CommonDataKeys.PROJECT);
		boolean enabled = !collectModulesWithConfiguration(project).isEmpty();
		e.getPresentation().setVisible(enabled);
		e.getPresentation().setEnabled(enabled);
		e.getPresentation().setIcon(LegioIcons.ICON_16);
		if (enabled) e.getPresentation().setText(message("publish.artifact"));
	}

	private void publish(Project project) {
		List<Module> validModules = collectModulesWithConfiguration(project).stream().collect(Collectors.toList());
		if (validModules.isEmpty()) {
			Messages.showInfoMessage(project, message("no.modules"), " Artifact Publisher");
			return;
		}
		if (validModules.size() > 1) {
			ChooseModulesDialog dialog = createDialog(project, validModules);
			dialog.show();
			if (dialog.isOK())
				new ArtifactPublisher(project, dialog.getChosenElements(), ArtifactPublisher.Actions.DISTRIBUTE).publish();
		} else new ArtifactPublisher(project, validModules, ArtifactPublisher.Actions.DISTRIBUTE).publish();
	}

	private ChooseModulesDialog createDialog(Project project, List<Module> taraModules) {
		final ChooseModulesDialog chooseModulesDialog = new ChooseModulesDialog(project,
				taraModules,
				message("select.tara.module.title"),
				message("select.tara.module.description"));
		chooseModulesDialog.setSingleSelectionMode();
		chooseModulesDialog.selectElements(Collections.singletonList(taraModules.get(0)));
		return chooseModulesDialog;
	}

	private List<Module> collectModulesWithConfiguration(Project project) {
		List<Module> taraModules = new ArrayList<>();
		for (Module module : ModuleManager.getInstance(project).getModules())
			if (TaraUtil.configurationOf(module) != null) taraModules.add(module);
		return taraModules;
	}
}
