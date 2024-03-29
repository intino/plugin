package io.intino.plugin.build;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.ui.StripeTable;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.table.JBTable;
import io.intino.Configuration.Deployment;
import io.intino.plugin.IntinoIcons;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.emptyList;
import static javax.swing.JOptionPane.YES_NO_CANCEL_OPTION;
import static javax.swing.JTable.AUTO_RESIZE_LAST_COLUMN;

public class SelectDestinationsDialog {
	private static final Object[] DeploymentFields = {"Server", "Deploy"};
	private JPanel deploymentsPanel;
	private JBTable table;
	private final String artifact;
	private final Window parent;
	private final List<Deployment> deployments;

	public SelectDestinationsDialog(String artifact, Window parent, List<Deployment> deployments) {
		this.artifact = artifact;
		this.parent = parent;
		this.deployments = deployments;
		createUIComponents();
	}

	List<Deployment> showAndGet() {
		final List<Deployment>[] destinations = new ArrayList[]{new ArrayList<Deployment>()};
		ApplicationManager.getApplication().invokeAndWait(() -> {
			String[] options = new String[]{"Cancel", "Accept"};
			int option = JOptionPane.showOptionDialog(parent, deploymentsPanel, "Select destinations for " + artifact,
					YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, IntinoIcons.INTINO_80, options, options[1]);
			destinations[0] = option == 1 ? selectedDestinations() : emptyList();
		}, ModalityState.any());
		return destinations[0];
	}

	private List<Deployment> selectedDestinations() {
		List<Deployment> destinations = new ArrayList<>();
		for (int i = 0; i < table.getModel().getRowCount(); i++)
			if ((boolean) table.getModel().getValueAt(i, 1))
				destinations.add(findDeployment(table.getModel().getValueAt(i, 0).toString().split(" ")[0]));
		return destinations;
	}

	private Deployment findDeployment(String value) {
		for (Deployment deployment : deployments)
			if (deployment.server().name().equalsIgnoreCase(value)) return deployment;
		return null;
	}

	private void createUIComponents() {
		deploymentsPanel = new JPanel();
		final DefaultTableModel tableModel = new DefaultTableModel(destinationsData(), DeploymentFields) {
			@Override
			public boolean isCellEditable(int row, int column) {
				return column != 0;
			}
		};
		tableModel.setColumnIdentifiers(DeploymentFields);
		table = newTable(tableModel);
		table.setEnableAntialiasing(true);
		table.getEmptyText().setText("No Deployments");
		table.setAutoResizeMode(AUTO_RESIZE_LAST_COLUMN);
		table.getColumn(DeploymentFields[0]).setPreferredWidth(150);
		table.getColumn(DeploymentFields[1]);
		deploymentsPanel = ToolbarDecorator.createDecorator(table).disableUpAction().disableDownAction().createPanel();
		deploymentsPanel.setMinimumSize(new Dimension(500, 200));
		table.setMinimumSize(new Dimension(500, 200));
		table.setMaximumSize(new Dimension(500, 1400));
		deploymentsPanel.setPreferredSize(new Dimension(500, 200));
	}

	private boolean checkExist(String value, int column) {
		for (Deployment deployment : deployments)
			if (deployment.server().name().equalsIgnoreCase(value)) return true;
		return false;
	}

	@NotNull
	private StripeTable newTable(DefaultTableModel tableModel) {
		return new StripeTable(tableModel) {
			private static final long serialVersionUID = 1L;

			public Class getColumnClass(int column) {
				if (column == 0) return String.class;
				return Boolean.class;
			}
		};
	}

	private Object[][] destinationsData() {
		Object[][] objects = new Object[deployments.size()][3];
		for (int i = 0; i < deployments.size(); i++)
			objects[i] = new Object[]{deployments.get(i).server().name() + " (" + deployments.get(i).server().type().name().toUpperCase() + ")", false};
		return objects;
	}
}