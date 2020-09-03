package io.intino.plugin.settings;

import com.intellij.openapi.ui.StripeTable;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.table.JBTable;
import org.apache.commons.codec.digest.DigestUtils;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import static javax.swing.JTable.AUTO_RESIZE_LAST_COLUMN;

public class IntinoSettingsPanel {

	private static final Object[] ARTIFACTORY_FIELDS = {"Id", "User", "Password"};
	private JPanel rootPanel;
	private JPanel tracker;
	private JTextField trackerProject;
	private JTextField trackerApi;
	private JScrollPane artifactories;
	private JBTable table;
	private JPanel tablePanel;
	private JTextField cesarToken;
	private JPanel cesar;
	private JTextField cesarUrl;
	private JButton generateButton;

	IntinoSettingsPanel() {
		tracker.setBorder(BorderFactory.createTitledBorder("Issue Tracker"));
		artifactories.setMaximumSize(new Dimension(artifactories.getWidth(), 500));
		generateButton.addActionListener(e -> {
			UserPassword dialog = new UserPassword();
			dialog.pack();
			dialog.setTitle("Put user and password");
			dialog.setLocationRelativeTo(dialog.getParent());
			dialog.show();
			if (dialog.user() != null && dialog.password() != null) {
				Base64.Encoder encoder = Base64.getEncoder();
				cesarToken.setText(encoder.encodeToString((dialog.user() + ":" + encoder.encodeToString(DigestUtils.md5(dialog.password()))).getBytes()));
			}
		});
	}

	void loadConfigurationData(IntinoSettings settings) {
		List<ArtifactoryCredential> artifactories = settings.artifactories();
		for (ArtifactoryCredential artifactory : artifactories) {
			DefaultTableModel model = (DefaultTableModel) table.getModel();
			model.addRow(new Object[]{artifactory.serverId.trim(), artifactory.username.trim(), artifactory.password.trim()});
		}
		if (table.getRowCount() > 0) table.addRowSelectionInterval(0, 0);
		trackerProject.setText(settings.trackerProjectId());
		trackerApi.setText(settings.trackerApiToken());
		cesarToken.setText(settings.cesarToken());
		cesarUrl.setText(settings.cesarUrl());
	}

	void applyConfigurationData(IntinoSettings settings) {
		settings.artifactories(createArtifactories());
		settings.trackerProjectId(trackerProject.getText());
		settings.trackerApiToken(trackerApi.getText());
		settings.cesarUrl(cesarUrl.getText());
		settings.cesarToken(cesarToken.getText());
		settings.saveState();
	}

	private List<ArtifactoryCredential> createArtifactories() {
		List<ArtifactoryCredential> credentials = new ArrayList<>();
		DefaultTableModel model = (DefaultTableModel) table.getModel();
		for (int i = 0; i < model.getRowCount(); i++)
			credentials.add(new ArtifactoryCredential(model.getValueAt(i, 0).toString().trim(), model.getValueAt(i, 1).toString().trim(), model.getValueAt(i, 2).toString().trim()));
		return credentials;
	}


	boolean isModified(IntinoSettings intinoSettings) {
		return true;
	}

	JPanel getRootPanel() {
		return rootPanel;
	}

	private void createUIComponents() {
		createTable();
	}

	private void createTable() {
		final DefaultTableModel tableModel = new DefaultTableModel(ARTIFACTORY_FIELDS, 0);
		tableModel.setColumnIdentifiers(ARTIFACTORY_FIELDS);
		table = new StripeTable(tableModel);
		table.setEnableAntialiasing(true);
		table.getEmptyText().setText("No artifactories");
		table.setAutoResizeMode(AUTO_RESIZE_LAST_COLUMN);
		table.getColumn(ARTIFACTORY_FIELDS[0]).setPreferredWidth(150);
		table.getColumn(ARTIFACTORY_FIELDS[1]).setPreferredWidth(150);
		table.getColumn(ARTIFACTORY_FIELDS[2]).setCellRenderer(new PasswordCellRenderer());
		tablePanel = ToolbarDecorator.createDecorator(table)
				.disableUpAction()
				.disableDownAction()
				.setAddAction(anActionButton -> ((DefaultTableModel) table.getModel()).addRow(new Object[]{"", "", "", ""}))
				.setRemoveAction(anActionButton -> {
					final int selectedRow = table.getSelectedRow();
					((DefaultTableModel) table.getModel()).removeRow(selectedRow);
					if (table.getModel().getRowCount() > 0 && selectedRow < table.getModel().getRowCount())
						table.addRowSelectionInterval(selectedRow, selectedRow);
				}).createPanel();
		tablePanel.setMaximumSize(new Dimension(tablePanel.getWidth(), 200));
		table.setMaximumSize(new Dimension(table.getWidth(), 200));
		table.setPreferredSize(new Dimension(table.getWidth(), 200));
		tablePanel.setPreferredSize(new Dimension(tablePanel.getWidth(), 200));
	}

	class PasswordCellRenderer extends DefaultTableCellRenderer {
		private static final String ASTERISKS = "******";

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			setText(!value.toString().isEmpty() ? ASTERISKS : "");
			return this;
		}
	}
}
