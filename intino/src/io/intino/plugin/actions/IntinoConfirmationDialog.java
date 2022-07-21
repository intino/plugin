package io.intino.plugin.actions;

import com.intellij.openapi.project.Project;
import com.intellij.util.ui.ConfirmationDialog;
import io.intino.plugin.IntinoIcons;

import javax.swing.*;

import static com.intellij.openapi.vcs.VcsShowConfirmationOption.STATIC_SHOW_CONFIRMATION;

public class IntinoConfirmationDialog extends ConfirmationDialog {
	public IntinoConfirmationDialog(Project project, String message, String title) {
		super(project, message, title, IntinoIcons.INTINO_80, STATIC_SHOW_CONFIRMATION);
	}

	@Override
	protected JComponent createSouthPanel() {
		setDoNotAskOption((com.intellij.openapi.ui.DoNotAskOption) null);
		return super.createSouthPanel();
	}
}
