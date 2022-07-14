package io.intino.plugin.toolwindows.remote;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.DumbAware;
import io.intino.alexandria.logger.Logger;
import io.intino.plugin.toolwindows.remote.remoteactions.ListenLogAction;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

import static io.intino.plugin.IntinoIcons.*;

public class FilterLogLevelAction extends AnAction implements DumbAware {
	private final ListenLogAction action;
	private Logger.Level currentLevel;
	private final Icon[] icons = {ERROR_LOG, WARN_LOG, INFO_LOG, DEBUG_LOG, ALL_LOG};

	public FilterLogLevelAction(ListenLogAction action) {
		this.action = action;
		this.currentLevel = Logger.Level.TRACE;
		final Presentation presentation = getTemplatePresentation();
		presentation.setText("Filter Log to Level " + nextLevel());
		presentation.setDescription("Filter log level");
		presentation.setIcon(AllIcons.Actions.ShortcutFilter);
	}

	@Override
	public void actionPerformed(@NotNull AnActionEvent e) {
		increaseLevel();
		action.onLevelChange(currentLevel);
	}

	private void increaseLevel() {
		currentLevel = nextLevel();
	}

	private Logger.Level nextLevel() {
		int newLevel = currentLevel.ordinal() - 1;
		return newLevel < 0 ? Logger.Level.TRACE : Logger.Level.values()[newLevel];
	}

	@Override
	public void update(@NotNull AnActionEvent e) {
		super.update(e);
		Logger.Level level = nextLevel();
		e.getPresentation().setText("Filter Log to Level " + level);
		e.getPresentation().setIcon(icons[currentLevel.ordinal()]);
		e.getPresentation().setVisible(true);
	}
}
