package io.intino.plugin.actions.dialog;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.StripeTable;
import com.intellij.ui.ScrollPaneFactory;
import com.intellij.ui.table.JBTable;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import java.awt.*;
import java.util.List;
import java.util.*;

import static javax.swing.JTable.AUTO_RESIZE_LAST_COLUMN;

public class UpdateVersionDialog extends DialogWrapper {
	private static final Object[] FIELDS = {"identifier", "last version"};
	private final JScrollPane mainPanel;
	private JBTable table;
	private Map<String, String> currentSelected;

	public UpdateVersionDialog(Project project, String title, Map<String, List<String>> libraries) {
		super(project, false);
		super.setTitle(title);
		this.centerRelativeToParent();
		this.setOKButtonText("Update");
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		Dimension dimension = new Dimension((int) (screenSize.getWidth() / 2), (int) (screenSize.getHeight() / 2));
		mainPanel = createTable(libraries);
		mainPanel.setPreferredSize(dimension);
		mainPanel.setMinimumSize(dimension);
		mainPanel.setMaximumSize(dimension);
		mainPanel.setSize(dimension);
		for (Map.Entry<String, List<String>> entry : libraries.entrySet())
			((DefaultTableModel) table.getModel()).addRow(new Object[]{entry.getKey(), entry.getValue().get(entry.getValue().size() - 1)});
		init();
	}

	private JScrollPane createTable(Map<String, List<String>> libraries) {
		final DefaultTableModel tableModel = new DefaultTableModel(FIELDS, 0);
		tableModel.setColumnIdentifiers(FIELDS);
		table = new StripeTable(tableModel);
		table.setEnableAntialiasing(true);
		table.getEmptyText().setText("No libraries");
		table.setAutoResizeMode(AUTO_RESIZE_LAST_COLUMN);
		table.setFillsViewportHeight(true);
		table.getColumn(FIELDS[0]).setPreferredWidth(250);
		table.getColumn(FIELDS[1]).setCellEditor(new CustomComboBoxEditor(libraries.values()));
		JScrollPane tablePanel = ScrollPaneFactory.createScrollPane(table);
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
		for (int i = 0; i < model.getRowCount(); i++)
			map.put(model.getValueAt(i, 0).toString(), model.getValueAt(i, 1).toString());
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
