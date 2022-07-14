package io.intino.plugin.toolwindows.remote.remoteactions;

import com.intellij.icons.AllIcons;
import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.DumbAware;
import com.intellij.ui.AnimatedIcon;
import com.intellij.util.ui.ConfirmationDialog;
import io.intino.Configuration;
import io.intino.alexandria.exceptions.BadRequest;
import io.intino.alexandria.exceptions.InternalServerError;
import io.intino.alexandria.logger.Logger;
import io.intino.cesar.box.schemas.ProcessInfo;
import io.intino.cesar.box.schemas.ProcessStatus;
import io.intino.plugin.IntinoIcons;
import io.intino.plugin.cesar.CesarAccessor;
import io.intino.plugin.toolwindows.remote.IntinoConsoleAction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.intellij.openapi.vcs.VcsShowConfirmationOption.STATIC_SHOW_CONFIRMATION;

public class RestartAction extends AnAction implements DumbAware, IntinoConsoleAction {
	@NotNull
	private final List<ProcessInfo> infos;
	private final Configuration.Server.Type serverType;
	private final CesarAccessor cesarAccessor;
	private final DataContext dataContext;
	private ProcessInfo selectedProcess;
	private ProcessStatus status;
	private boolean inProcess = false;

	public RestartAction(List<ProcessInfo> infos, Configuration.Server.Type serverType, CesarAccessor cesarAccessor) {
		this.infos = infos;
		this.serverType = serverType;
		this.selectedProcess = infos.isEmpty() ? null : infos.get(0);
		this.cesarAccessor = cesarAccessor;
		this.status = selectedProcess == null ? null : this.cesarAccessor.processStatus(selectedProcess.server().name(), selectedProcess.id());
		dataContext = dataContext();
		final Presentation presentation = getTemplatePresentation();
		presentation.setText("Rerun Remote Process");
		presentation.setDescription("Rerun remote process");
		presentation.setDisabledIcon(AnimatedIcon.Default.INSTANCE);
		presentation.setIcon(AllIcons.Actions.Restart);
	}

	@Override
	public void onChanging() {
		inProcess = true;
	}

	public void onProcessChange(ProcessInfo newProcess, ProcessStatus newProcessStatus) {
		inProcess = true;
		new Thread(() -> {
			selectedProcess = newProcess;
			status = newProcessStatus;
			inProcess = false;
			update();
		}).start();
	}

	@Override
	public void actionPerformed(@NotNull AnActionEvent e) {
		inProcess = true;
		update(e);
		boolean sure = askAndContinue(e);
		if (!sure) return;
		new Thread(() -> {
			status = cesarAccessor.processStatus(selectedProcess.server().name(), selectedProcess.id());
			try {
				cesarAccessor.accessor().postProcessStatus(selectedProcess.server().name(), selectedProcess.id(), true, false);
			} catch (BadRequest | InternalServerError ignored) {
			}
			inProcess = false;
			update(e);
		}).start();
	}

	private boolean askAndContinue(@NotNull AnActionEvent e) {
		if (!serverType.equals(Configuration.Server.Type.Pro)) return true;
		AtomicBoolean response = new AtomicBoolean(false);
		ApplicationManager.getApplication().invokeAndWait(() -> {
			ConfirmationDialog confirmationDialog = new ConfirmationDialog(e.getData(CommonDataKeys.PROJECT),
					"Are you sure to restart this process?",
					"Restart Process", IntinoIcons.INTINO_80, STATIC_SHOW_CONFIRMATION);
			confirmationDialog.setDoNotAskOption((com.intellij.openapi.ui.DoNotAskOption) null);
			response.set(confirmationDialog.showAndGet());
		});
		return response.get();
	}

	@Override
	public void update(@NotNull AnActionEvent e) {
		super.update(e);
		update(e.getPresentation());
	}

	public void update() {
		update(new AnActionEvent(null, dataContext, ActionPlaces.UNKNOWN, new Presentation(), ActionManager.getInstance(), 0));
	}

	@Nullable
	private DataContext dataContext() {
		DataContext dataContext = null;
		try {
			dataContext = DataManager.getInstance().getDataContextFromFocusAsync().blockingGet(1000);
		} catch (TimeoutException | ExecutionException e) {
			Logger.error(e);
		}
		return dataContext;
	}

	public void update(Presentation p) {
		p.setVisible(true);
		if (selectedProcess == null) {
			p.setVisible(false);
		} else if (inProcess) {
			p.setEnabled(false);
		} else {
			if (status == null) {
				status = cesarAccessor.processStatus(selectedProcess.server().name(), selectedProcess.id());
				p.setVisible(false);
			} else {
				p.setEnabled(true);
				p.setVisible(status.running());
			}
		}
	}
}
