package io.intino.plugin.toolwindows.output;

import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.icons.AllIcons;
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

public class DebugAction extends AnAction implements DumbAware {
	private final CesarAccessor cesarAccessor;
	private ProcessInfo info;
	private ProcessStatus status;
	private RunContentDescriptor myContentDescriptor;

	public DebugAction(ProcessInfo info, RunContentDescriptor contentDescriptor) {
		this.info = info;
		myContentDescriptor = contentDescriptor;
		cesarAccessor = new CesarAccessor(OutputsToolWindow.project);
		status = cesarAccessor.processStatus(this.info.project(), this.info.id());
		final Presentation templatePresentation = getTemplatePresentation();
		templatePresentation.setIcon(AllIcons.Actions.StartDebugger);
		templatePresentation.setText("Debug remote process");
		templatePresentation.setDescription("Debug remote process");
	}

	@Override
	public void actionPerformed(AnActionEvent e) {
		status = cesarAccessor.processStatus(this.info.project(), this.info.id());
		try {
			Boolean success = cesarAccessor.accessor().postProcessStatus(this.info.project(), this.info.id(), status.running(), true);
			if (success) status.running(!status.running());
		} catch (BadRequest | Unknown bd) {
			Logger.error(bd);
		}
	}

	@Override
	public void update(AnActionEvent e) {
		super.update(e);
		e.getPresentation().setVisible(!status.debug());
	}
}
