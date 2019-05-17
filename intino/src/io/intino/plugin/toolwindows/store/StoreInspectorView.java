package io.intino.plugin.toolwindows.store;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import io.intino.legio.graph.RunConfiguration;
import io.intino.plugin.project.LegioConfiguration;
import io.intino.tara.compiler.shared.Configuration;
import io.intino.tara.plugin.lang.file.StashFileType;
import io.intino.tara.plugin.lang.psi.impl.TaraUtil;

import javax.swing.*;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static io.intino.plugin.project.LegioConfiguration.LegioDeployConfiguration;
import static io.intino.tara.compiler.shared.Configuration.DeployConfiguration;
import static io.intino.tara.plugin.lang.TaraIcons.STASH_16;
import static java.awt.event.ItemEvent.SELECTED;

public class StoreInspectorView extends JPanel {
	private final Project project;
	private JPanel contentPane;
	private JComboBox scope;
	private JComboBox<String> configurations;
	private JTree tree;
	private String selectedDatalake = null;
	private String selectedDatamart = null;


	StoreInspectorView(Project project) {
		this.project = project;
		this.scope.addItemListener(e -> {
			if (e.getStateChange() == SELECTED) {
				configurations.removeAllItems();
				configurations.addItem("");
				for (String conf : runConfigurationsFrom(e.getItem().toString())) configurations.addItem(conf);
				configurations.setEnabled(!e.getItem().toString().isEmpty());
			}
		});
		this.configurations.addItemListener(e -> {
			if (e.getStateChange() == SELECTED) {
				boolean empty = e.getItem().toString().isEmpty();
				selectedDatalake = empty ? null : datalakeParameterOf(e.getItem().toString());
				selectedDatamart = empty ? null : datamartParameterOf(e.getItem().toString());
				reloadTree();
			}
		});
		init();
		tree.addTreeExpansionListener(new TreeExpansionListener() {
			@Override
			public void treeExpanded(TreeExpansionEvent event) {
				renderDirectory((DefaultMutableTreeNode) event.getPath().getLastPathComponent());
			}

			private void renderDirectory(DefaultMutableTreeNode directoryNode) {
				directoryNode.removeAllChildren();
				if (directoryNode.getParent() == null) renderRoots();
				else
					for (Connection.File child : ((Connection.File) directoryNode.getUserObject()).children()) {
						DefaultMutableTreeNode node = new DefaultMutableTreeNode(child);
						directoryNode.add(node);
						if (child.isDirectory()) node.add(new DefaultMutableTreeNode(""));
					}
				tree.updateUI();
			}

			@Override
			public void treeCollapsed(TreeExpansionEvent event) {

			}
		});

		tree.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				int selRow = tree.getRowForLocation(e.getX(), e.getY());
				TreePath selPath = tree.getPathForLocation(e.getX(), e.getY());
				if (selRow != -1 && e.getClickCount() == 2) new TreeFileOpener(project).openFile(selPath);
			}
		});
		tree.updateUI();
	}


	private void reloadTree() {
		((DefaultMutableTreeNode) tree.getModel().getRoot()).removeAllChildren();
		if (selectedDatalake == null && selectedDatamart == null) {
			tree.setVisible(false);
		} else {
			renderRoots();
			tree.setVisible(true);
			tree.setRootVisible(true);
			tree.expandRow(0);
			tree.setRootVisible(false);
		}
	}

	private void renderRoots() {
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getModel().getRoot();
		if (selectedDatalake != null) createRoot(node, new DirectoryWalker(project, selectedDatalake).root());
		if (selectedDatamart != null) createRoot(node, new DirectoryWalker(project, selectedDatamart).root());
		tree.updateUI();
	}

	private void createRoot(DefaultMutableTreeNode node, Connection.File root) {
		DefaultMutableTreeNode newChild = new DefaultMutableTreeNode(root);
		if (root.isDirectory()) newChild.add(new DefaultMutableTreeNode(""));
		node.add(newChild);
	}

	private void init() {
		DefaultTreeModel model = new DefaultTreeModel(new DefaultMutableTreeNode("", true), false);
		tree.setModel(model);
		tree.expandRow(0);
		tree.setRootVisible(false);
		tree.setShowsRootHandles(true);
		((DefaultMutableTreeNode) tree.getModel().getRoot()).removeAllChildren();
		this.tree.setCellRenderer(new StoreTreeCellRenderer());

	}

	private List<String> runConfigurationsFrom(String scope) {
		List<String> configurations = new ArrayList<>();
		for (Module module : ModuleManager.getInstance(project).getModules()) {
			Configuration conf = TaraUtil.configurationOf(module);
			if (!(conf instanceof LegioConfiguration)) continue;
			for (RunConfiguration runConfiguration : ((LegioConfiguration) conf).runConfigurations()) {
				Map<String, String> parameters = runConfiguration.finalArguments();
				if (!parameters.containsKey("datalake") && !parameters.containsKey("datamart")) continue;
				if (scope.equals("pre") && isPre(((LegioConfiguration) conf), runConfiguration))
					add(configurations, module, runConfiguration);
				else if (scope.equals("pro") && isPro(((LegioConfiguration) conf), runConfiguration))
					add(configurations, module, runConfiguration);
				else if (scope.equals("dev")) add(configurations, module, runConfiguration);
			}
		}
		return configurations;
	}

	private String datamartParameterOf(String conf) {
		return findRunConfiguration(conf).finalArguments().getOrDefault("datamart", null);
	}

	private String datalakeParameterOf(String conf) {
		return findRunConfiguration(conf).finalArguments().get("datalake");
	}

	private RunConfiguration findRunConfiguration(String conf) {
		String[] split = conf.split(":");
		Module module = Arrays.stream(ModuleManager.getInstance(project).getModules()).filter(m -> m.getName().equals(split[0].trim())).findFirst().orElse(null);
		return ((LegioConfiguration) TaraUtil.configurationOf(module)).runConfigurations().stream().filter(r1 -> r1.name$().equals(split[1].trim())).findFirst().orElse(null);
	}

	private void add(List<String> configurations, Module module, RunConfiguration runConfiguration) {
		configurations.add(module.getName() + ":" + runConfiguration.name$());
	}

	JPanel contentPane() {
		return contentPane;
	}

	private boolean isPro(LegioConfiguration conf, RunConfiguration runConfiguration) {
		return conf.deployConfigurations().stream().filter(DeployConfiguration::pro).anyMatch((d -> ((LegioDeployConfiguration) d).runConfiguration().equals(runConfiguration)));
	}

	private boolean isPre(LegioConfiguration conf, RunConfiguration runConfiguration) {
		return conf.deployConfigurations().stream().filter(d -> !d.pro()).anyMatch((d -> ((LegioDeployConfiguration) d).runConfiguration().equals(runConfiguration)));
	}

	private static class StoreTreeCellRenderer extends DefaultTreeCellRenderer {

		@Override
		public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
			super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);

			if (value instanceof DefaultMutableTreeNode) {
				DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
				if (node.getUserObject() instanceof String) {
					setIcon(getDefaultClosedIcon());
				} else if (node.getUserObject() instanceof Connection.File) {
					if (((Connection.File) node.getUserObject()).isDirectory()) setIcon(getDefaultClosedIcon());
					else {
						if (node.isLeaf() && node.toString().endsWith(StashFileType.INSTANCE.getDefaultExtension()))
							setIcon(STASH_16);
						else setIcon(getDefaultLeafIcon());
					}
				}
			}
			return this;
		}
	}

}