package io.intino.plugin.actions.dialog;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

public class SubmitFeedbackDialogPane extends DialogWrapper {

	private JPanel dialogContents;
	private JLabel info;
	private JTextArea reportText;
	private JComboBox reportType;
	private JTextField title;

	public SubmitFeedbackDialogPane(final Project project) {
		super(project, false);
		this.centerRelativeToParent();
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		Dimension dimension = new Dimension((int) (screenSize.getWidth() / 2), (int) (screenSize.getHeight() / 2));
		dialogContents.setPreferredSize(dimension);
		dialogContents.setMinimumSize(dimension);
		dialogContents.setMaximumSize(dimension);
		super.setTitle("Submit Tara Feedback");
		setOKButtonText("Submit");
		init();
	}

	@Nullable
	@Override
	protected JComponent createCenterPanel() {
		return dialogContents;
	}

	public String getReportTitle() {
		return title.getText();
	}

	public String getReportDescription() {
		return reportText.getText();
	}

	public String getReportType() {
		return reportType.getSelectedItem().toString();
	}
}
