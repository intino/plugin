package io.intino.plugin.toolwindows.output;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import io.intino.alexandria.exceptions.BadRequest;
import io.intino.alexandria.exceptions.InternalServerError;
import io.intino.cesar.box.schemas.ProcessInfo;
import io.intino.cesar.box.schemas.ProcessStatus;
import io.intino.plugin.project.CesarAccessor;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static com.intellij.icons.AllIcons.Actions.Suspend;
import static com.intellij.icons.AllIcons.RunConfigurations.TestState.Run;

public class StartStopAction extends AnAction implements DumbAware {
	private final CesarAccessor cesarAccessor;
	private ProcessInfo selectedProcess;
	private ProcessStatus status;
	private boolean inProcess = false;

	public StartStopAction(List<ProcessInfo> infos, ComboBox<Object> processSelector, Project project) {
		cesarAccessor = new CesarAccessor(project);
		selectedProcess = infos.get(0);
		status = cesarAccessor.processStatus(selectedProcess.server().name(), selectedProcess.id());
		processSelector.addItemListener(e -> {
			String anObject = e.getItem().toString();
			selectedProcess = infos.stream().filter(p -> p.artifact().equals(anObject)).findFirst().get();
			status = cesarAccessor.processStatus(selectedProcess.server().name(), selectedProcess.id());
		});
		final Presentation templatePresentation = getTemplatePresentation();
		templatePresentation.setIcon(Run);
		templatePresentation.setText("Run Remote Process");
		templatePresentation.setDescription("Run remote process");
	}

	@Override
	public void actionPerformed(@NotNull AnActionEvent e) {
		inProcess = true;
		update(e);
		ApplicationManager.getApplication().runWriteAction(() -> {
			status = cesarAccessor.processStatus(selectedProcess.server().name(), selectedProcess.id());
			try {
				Boolean success = cesarAccessor.accessor().postProcessStatus(selectedProcess.server().name(), selectedProcess.id(), !status.running(), false);
				if (success) status.running(!status.running());
			} catch (BadRequest | InternalServerError bd) {
			}
			inProcess = false;
		});

	}

	@Override
	public void update(@NotNull AnActionEvent e) {
		super.update(e);
		if (inProcess) {
			e.getPresentation().setVisible(true);
			e.getPresentation().setEnabled(false);
			e.getPresentation().setIcon(AllIcons.Process.Step_passive);
			e.getPresentation().setText("");
		} else {
			status = cesarAccessor.processStatus(selectedProcess.server().name(), selectedProcess.id());
			if (status == null) e.getPresentation().setVisible(false);
			else {
				e.getPresentation().setVisible(true);
				e.getPresentation().setIcon(status.running() ? Suspend : Run);
				e.getPresentation().setText(status.running() ? "Stop Process" : "Run Process");
				e.getPresentation().setDescription(status.running() ? "Stop process" : "Run process");
			}
		}
	}
}
