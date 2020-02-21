package io.intino.plugin.actions.dialog;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.StripeTable;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.table.JBTable;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

import static javax.swing.JTable.AUTO_RESIZE_LAST_COLUMN;

public class UpdateVersionDialog extends DialogWrapper {
	private static final Object[] FIELDS = {"identifier", "version"};
	private JBScrollPane scrollPane;
	private final JPanel mainPanel;
	private JBTable table;
	private Map<String, String> currentSelected;


	public UpdateVersionDialog(Project project, Map<String, List<String>> libraries) {
		super(project, false);
		super.setTitle("Update Versions");
		this.currentSelected = libraries.keySet().stream().collect(Collectors.toMap(k -> k.substring(0, k.lastIndexOf(":")), v -> v.substring(v.lastIndexOf(":"))));
		this.centerRelativeToParent();
		this.setOKButtonText("Update");
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		Dimension dimension = new Dimension((int) (screenSize.getWidth() / 2), (int) (screenSize.getHeight() / 2));
		scrollPane = new JBScrollPane();
		scrollPane.setPreferredSize(dimension);
		scrollPane.setMinimumSize(dimension);
		scrollPane.setMaximumSize(dimension);
		scrollPane.setSize(dimension);

		mainPanel = createTable(libraries);
		mainPanel.setPreferredSize(dimension);
		mainPanel.setMinimumSize(dimension);
		mainPanel.setMaximumSize(dimension);
		mainPanel.setSize(dimension);
//		scrollPane.add(mainPanel);
		for (Map.Entry<String, List<String>> entry : libraries.entrySet())
			((DefaultTableModel) table.getModel()).addRow(new Object[]{entry.getKey(), entry.getKey().split(":")[2]});
		init();
	}

	private JPanel createTable(Map<String, List<String>> libraries) {
		final DefaultTableModel tableModel = new DefaultTableModel(FIELDS, 0);
		tableModel.setColumnIdentifiers(FIELDS);
		table = new StripeTable(tableModel);
		table.setEnableAntialiasing(true);
		table.getEmptyText().setText("No libraries");
		table.setAutoResizeMode(AUTO_RESIZE_LAST_COLUMN);
		table.getColumn(FIELDS[0]).setPreferredWidth(150);
		table.getColumn(FIELDS[1]).setCellEditor(new CustomComboBoxEditor(libraries.values()));
		JPanel tablePanel = ToolbarDecorator.createDecorator(table).disableUpAction().disableDownAction().createPanel();
		tablePanel.setMaximumSize(new Dimension(tablePanel.getWidth(), 200));
		table.setMaximumSize(new Dimension(table.getWidth(), 200));
		table.setPreferredSize(new Dimension(table.getWidth(), 200));
		tablePanel.setPreferredSize(new Dimension(tablePanel.getWidth(), 200));
		return tablePanel;
	}

	@Nullable
	@Override
	protected JComponent createCenterPanel() {
		return mainPanel;
	}

	public Map<String, String> newVersions() {
		TableModel model = table.getModel();
		Map<String, String> map = new LinkedHashMap<>();
		for (int i = 0; i < model.getRowCount(); i++) {
			map.put(model.getValueAt(i, 0).toString(), model.getValueAt(i, 1).toString());
		}
		return map;
	}

	public static class CustomComboBoxEditor extends DefaultCellEditor {
		private final List<List<String>> libraries;
		private DefaultComboBoxModel model;

		public CustomComboBoxEditor(Collection<List<String>> libraries) {
			super(new JComboBox());
			this.libraries = new ArrayList<>(libraries);
			this.model = (DefaultComboBoxModel) ((JComboBox) getComponent()).getModel();
		}

		@Override
		public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
			model.removeAllElements();
			model.addAll(libraries.get(row));
			return super.getTableCellEditorComponent(table, value, isSelected, row, column);
		}
	}
}
