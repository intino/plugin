package io.intino.plugin.build;

import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.ui.StripeTable;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.table.JBTable;
import io.intino.legio.graph.Artifact;
import io.intino.legio.graph.Destination;
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
	private static final Object[] ARTIFACTORY_FIELDS = {"Name", "Dev", "Pro"};
	private JPanel deploymentsPanel;
	private JBTable table;

	private Window parent;
	private final List<Artifact.Deployment> deployments;

	public SelectDestinationsDialog(Window parent, List<Artifact.Deployment> deployments) {
		this.parent = parent;
		this.deployments = deployments;
		createUIComponents();
	}

	List showAndGet() {
		final List[] destinations = new List[]{new ArrayList<>()};
		final Application application = ApplicationManager.getApplication();
		application.invokeAndWait(() -> {
			String[] options = new String[]{"Cancel", "Accept"};
			int option = JOptionPane.showOptionDialog(parent, deploymentsPanel, "Select destinations of deployment",
					YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, IntinoIcons.INTINO_80, options, options[1]);
			destinations[0] = option == 1 ? selectedDestinations() : emptyList();
		}, ModalityState.any());
		return destinations[0];
	}

	private List<Destination> selectedDestinations() {
		List<Destination> destinations = new ArrayList<>();
		for (int i = 0; i < table.getModel().getRowCount(); i++) {
			if ((boolean) table.getModel().getValueAt(i, 1))
				destinations.add(findDestination(table.getModel().getValueAt(i, 0).toString(), false));
			if ((boolean) table.getModel().getValueAt(i, 2))
				destinations.add(findDestination(table.getModel().getValueAt(i, 0).toString(), true));
		}
		return destinations;
	}

	private Destination findDestination(String value, boolean pro) {
		for (Artifact.Deployment deployment : deployments)
			if (deployment.name$().equalsIgnoreCase(value)) return pro ? deployment.pro() : deployment.pre();
		return null;
	}

	private void createUIComponents() {
		deploymentsPanel = new JPanel();
		final DefaultTableModel tableModel = new DefaultTableModel(destinationsData(), ARTIFACTORY_FIELDS) {
			@Override
			public boolean isCellEditable(int row, int column) {
				return column != 0 && checkExist(this.getValueAt(row, 0).toString(), column);
			}
		};
		tableModel.setColumnIdentifiers(ARTIFACTORY_FIELDS);
		table = newTable(tableModel);

		table.setEnableAntialiasing(true);
		table.getEmptyText().setText("No Deployments");
		table.setAutoResizeMode(AUTO_RESIZE_LAST_COLUMN);
		table.getColumn(ARTIFACTORY_FIELDS[0]).setPreferredWidth(150);
		table.getColumn(ARTIFACTORY_FIELDS[2]);
		table.getColumn(ARTIFACTORY_FIELDS[1]);
		deploymentsPanel = ToolbarDecorator.createDecorator(table).disableUpAction().disableDownAction().createPanel();
		deploymentsPanel.setMinimumSize(new Dimension(400, 200));
		table.setMinimumSize(new Dimension(400, 200));
		table.setMaximumSize(new Dimension(400, 1400));
		deploymentsPanel.setPreferredSize(new Dimension(400, 200));
	}

	private boolean checkExist(String value, int column) {
		for (Artifact.Deployment deployment : deployments)
			if (deployment.name$().equalsIgnoreCase(value)) {
				if (column == 1) return deployment.pre() != null;
				return deployment.pro() != null;
			}
		return false;
	}

	@NotNull
	private StripeTable newTable(DefaultTableModel tableModel) {
		return new StripeTable(tableModel) {
			private static final long serialVersionUID = 1L;

			public Class getColumnClass(int column) {
				switch (column) {
					case 0:
						return String.class;
					case 1:
						return Boolean.class;
					default:
						return Boolean.class;
				}
			}
		};
	}

	private Object[][] destinationsData() {
		Object[][] objects = new Object[deployments.size()][3];
		for (int i = 0; i < deployments.size(); i++)
			objects[i] = new Object[]{deployments.get(i).name$(), deployments.get(i).pre() != null, deployments.get(i).pro() != null};
		return objects;
	}
}