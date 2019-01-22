package io.intino.plugin.toolwindows.output;

import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.DumbAware;
import io.intino.alexandria.exceptions.BadRequest;
import io.intino.alexandria.exceptions.Unknown;
import io.intino.alexandria.logger.Logger;
import io.intino.cesar.box.schemas.ProcessInfo;
import io.intino.cesar.box.schemas.ProcessStatus;
import io.intino.plugin.project.CesarAccessor;

import static com.intellij.icons.AllIcons.Actions.Suspend;
import static com.intellij.icons.AllIcons.General.Run;

public class StartAction extends AnAction implements DumbAware {
	private final CesarAccessor cesarAccessor;
	private ProcessInfo info;
	private ProcessStatus status;
	private RunContentDescriptor myContentDescriptor;

	public StartAction(ProcessInfo info, RunContentDescriptor contentDescriptor) {
		this.info = info;
		myContentDescriptor = contentDescriptor;
		cesarAccessor = new CesarAccessor(OutputsToolWindow.project);
		status = cesarAccessor.processStatus(this.info.project(), this.info.id());
		final Presentation templatePresentation = getTemplatePresentation();
		templatePresentation.setIcon(Run);
		templatePresentation.setText("Run remote process");
		templatePresentation.setDescription("Run remote process");
	}

	@Override
	public void actionPerformed(AnActionEvent e) {
		status = cesarAccessor.processStatus(this.info.project(), this.info.id());
		try {
			Boolean success = cesarAccessor.accessor().postProcessStatus(this.info.project(), this.info.id(), !status.running(), false);
			if (success) status.running(!status.running());
		} catch (BadRequest | Unknown bd) {
			Logger.error(bd);
		}
	}

	@Override
	public void update(AnActionEvent e) {
		super.update(e);
		e.getPresentation().setIcon(status.running() ? Suspend : Run);
		e.getPresentation().setText(status.running() ? "Stop process" : "Run process");
		e.getPresentation().setDescription(status.running() ? "Stop process" : "Run process");
	}
}
